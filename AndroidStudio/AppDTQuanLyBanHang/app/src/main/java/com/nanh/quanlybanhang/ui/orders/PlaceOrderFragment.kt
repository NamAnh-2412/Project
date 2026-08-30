package com.nanh.quanlybanhang.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentPlaceOrderBinding
import com.nanh.quanlybanhang.ui.navigation.DashboardTopTabNavigator
import com.nanh.quanlybanhang.ui.orders.xml.DraftTabsAdapter
import com.nanh.quanlybanhang.ui.orders.xml.PlaceOrderCategoryAdapter
import com.nanh.quanlybanhang.ui.orders.xml.PlaceOrderProductAdapter
import kotlinx.coroutines.launch

class PlaceOrderFragment : Fragment() {

    private var _binding: FragmentPlaceOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaceOrderViewModel by activityViewModels {
        PlaceOrderViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val categoryAdapter = PlaceOrderCategoryAdapter { cat ->
        viewModel.selectCategory(cat.id)
    }

    private val productAdapter = PlaceOrderProductAdapter(
        onProductClick = { product ->
            if (product.linkedOptionGroups.isNotEmpty()) {
                showPlaceOrderOptionPickDialog(viewModel = viewModel, product = product)
            } else {
                viewModel.addProductToSelectedDraft(product, emptyList())
            }
        },
    )

    private val draftTabsAdapter = DraftTabsAdapter(
        onSelect = { id -> viewModel.selectDraftTab(id) },
        onClose = { id -> viewModel.closeDraftTab(id) },
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.inflateMenu(R.menu.menu_place_order)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_order_history -> {
                    (activity as? DashboardTopTabNavigator)?.pushOrderHistoryFromPlaceOrder()
                    true
                }
                else -> false
            }
        }

        binding.recyclerDraftTabs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerDraftTabs.adapter = draftTabsAdapter

        binding.buttonAddDraft.setOnClickListener {
            if (!viewModel.tryAddDraft()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.place_order_max_drafts, PlaceOrderViewModel.MAX_DRAFT_SESSIONS),
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
        }

        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategories.adapter = categoryAdapter

        binding.recyclerProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerProducts.adapter = productAdapter

        binding.editSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }

        binding.buttonOpenReview.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.cartLines.isEmpty()) {
                Snackbar.make(binding.root, R.string.place_order_checkout_need_lines, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openReviewScreen()
        }

        binding.buttonSaveCustomerInfo.setOnClickListener {
            showCustomerInfoSaveDialog(viewModel) {
                Snackbar.make(binding.root, R.string.place_order_customer_saved_hint, Snackbar.LENGTH_LONG).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    categoryAdapter.submitList(state.categories) {
                        categoryAdapter.setSelectedIndex(state.selectedCategoryIndex)
                    }
                    productAdapter.submitList(state.products)
                    draftTabsAdapter.submitList(state.draftTabs)

                    binding.textCategoriesEmpty.isVisible = state.categories.isEmpty()
                    binding.recyclerCategories.isVisible = state.categories.isNotEmpty()
                    binding.textProductsEmpty.isVisible =
                        state.categories.isNotEmpty() && state.products.isEmpty()
                    binding.recyclerProducts.isVisible = state.products.isNotEmpty()

                    val hasLines = state.cartLines.isNotEmpty()
                    binding.buttonOpenReview.isEnabled = hasLines
                    binding.buttonOpenReview.alpha = if (hasLines) 1f else 0.45f
                    binding.textOrderSummaryHint.text = if (hasLines) {
                        state.cartSummaryLabel
                    } else {
                        getString(R.string.place_order_cart_empty)
                    }
                }
            }
        }
    }

    private fun openReviewScreen() {
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, PlaceOrderReviewFragment.newInstance())
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): PlaceOrderFragment = PlaceOrderFragment()
    }
}
