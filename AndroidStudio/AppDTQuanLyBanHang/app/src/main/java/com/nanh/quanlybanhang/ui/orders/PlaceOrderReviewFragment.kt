package com.nanh.quanlybanhang.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentPlaceOrderReviewBinding
import com.nanh.quanlybanhang.ui.orders.xml.DraftCartLinesAdapter
import com.nanh.quanlybanhang.ui.orders.xml.DraftTabsAdapter
import kotlinx.coroutines.launch

class PlaceOrderReviewFragment : Fragment() {

    private var _binding: FragmentPlaceOrderReviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaceOrderViewModel by activityViewModels {
        PlaceOrderViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private var syncingChips = false

    private val draftTabsAdapter = DraftTabsAdapter(
        onSelect = { id -> viewModel.selectDraftTab(id) },
        onClose = { id -> viewModel.closeDraftTab(id) },
    )

    private val cartLinesAdapter = DraftCartLinesAdapter(
        onLineContentClick = { line ->
            val product = viewModel.productUiForEdit(line.productId)
            if (product == null) {
                Snackbar.make(binding.root, R.string.place_order_edit_product_missing, Snackbar.LENGTH_SHORT).show()
                return@DraftCartLinesAdapter
            }
            if (product.linkedOptionGroups.isEmpty()) {
                Snackbar.make(binding.root, R.string.place_order_line_no_option_groups, Snackbar.LENGTH_SHORT).show()
                return@DraftCartLinesAdapter
            }
            showPlaceOrderOptionPickDialog(
                viewModel = viewModel,
                product = product,
                preselectedIds = line.selectedOptionItemIds.toSet(),
                editingCartLineKey = line.cartLineKey,
            )
        },
        onRemoveLine = { line -> viewModel.removeDraftLine(line.cartLineKey) },
        onDecreaseQty = { line -> viewModel.changeDraftLineQuantity(line.cartLineKey, -1) },
        onIncreaseQty = { line -> viewModel.changeDraftLineQuantity(line.cartLineKey, 1) },
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceOrderReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
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

        binding.recyclerCartLines.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCartLines.adapter = cartLinesAdapter

        binding.chipGroupCustomer.setOnCheckedStateChangeListener(
            ChipGroup.OnCheckedStateChangeListener { _, checkedIds ->
                if (syncingChips) return@OnCheckedStateChangeListener
                val id = checkedIds.firstOrNull() ?: return@OnCheckedStateChangeListener
                when (id) {
                    R.id.chipCustomerRetail -> viewModel.setCustomerKind(PlaceOrderCustomerKind.RETAIL)
                    R.id.chipCustomerRegular -> viewModel.setCustomerKind(PlaceOrderCustomerKind.REGULAR)
                }
            },
        )

        binding.buttonPickCustomer.setOnClickListener {
            val state = viewModel.uiState.value
            showCustomerPickDialog(state.customers) { customerId ->
                viewModel.setSelectedCustomerId(customerId)
            }
        }

        binding.buttonAddCustomer.setOnClickListener {
            showCustomerInfoSaveDialog(viewModel) { id ->
                viewModel.setSelectedCustomerId(id)
            }
        }

        parentFragmentManager.setFragmentResultListener(
            CheckoutPaymentDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            if (bundle.getBoolean(CheckoutPaymentDialogFragment.EXTRA_CONFIRMED, false)) {
                val methodIndex = bundle.getInt(CheckoutPaymentDialogFragment.EXTRA_METHOD_INDEX, 0)
                val customerPay = bundle.getLong(CheckoutPaymentDialogFragment.EXTRA_CUSTOMER_PAY, 0L)
                viewLifecycleOwner.lifecycleScope.launch {
                    when (val r = viewModel.completeCheckoutAfterPayment(
                        paymentMethodIndex = methodIndex,
                        customerPayDong = customerPay,
                    )) {
                        CheckoutPersistResult.Success -> {
                            Snackbar.make(binding.root, R.string.checkout_order_saved, Snackbar.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        is CheckoutPersistResult.Error ->
                            Snackbar.make(binding.root, r.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            viewModel.clearSelectedDraftCart()
        }

        binding.buttonCheckout.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.cartLines.isEmpty()) {
                Snackbar.make(binding.root, R.string.place_order_checkout_need_lines, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (state.customerKind == PlaceOrderCustomerKind.REGULAR) {
                if (state.customers.isEmpty()) {
                    Snackbar.make(binding.root, R.string.checkout_no_customers, Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (state.selectedCustomerId == null) {
                    Snackbar.make(binding.root, R.string.checkout_pick_customer, Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            CheckoutPaymentDialogFragment.newInstance(state.cartTotalMoney)
                .show(parentFragmentManager, CheckoutPaymentDialogFragment.TAG)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    draftTabsAdapter.submitList(state.draftTabs)
                    cartLinesAdapter.submitList(state.cartLines)

                    val hasLines = state.cartLines.isNotEmpty()
                    binding.recyclerCartLines.isVisible = hasLines
                    binding.textCartEmpty.isVisible = !hasLines
                    binding.textCartSummary.text = state.cartSummaryLabel

                    syncingChips = true
                    when (state.customerKind) {
                        PlaceOrderCustomerKind.RETAIL -> binding.chipGroupCustomer.check(R.id.chipCustomerRetail)
                        PlaceOrderCustomerKind.REGULAR -> binding.chipGroupCustomer.check(R.id.chipCustomerRegular)
                    }
                    syncingChips = false

                    bindCustomerSelection(state)
                }
            }
        }
    }

    private fun bindCustomerSelection(state: PlaceOrderUiState) {
        val isRegular = state.customerKind == PlaceOrderCustomerKind.REGULAR
        binding.layoutCustomerPick.isVisible = isRegular
        if (!isRegular) return

        if (state.customers.isNotEmpty() && state.selectedCustomerId == null) {
            viewModel.setSelectedCustomerId(state.customers.first().id)
            return
        }

        val selected = state.selectedCustomerId?.let { sid ->
            state.customers.firstOrNull { it.id == sid }
        }
        binding.textCustomerSelected.text = selected?.label
            ?: getString(R.string.place_order_customer_none_selected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): PlaceOrderReviewFragment = PlaceOrderReviewFragment()
    }
}
