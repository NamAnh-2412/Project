package com.nanh.quanlybanhang.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.ProductScope
import com.nanh.quanlybanhang.databinding.FragmentProductItemsBinding
import com.nanh.quanlybanhang.ui.products.xml.ProductsListAdapter
import kotlinx.coroutines.launch

class ProductItemsFragment : Fragment(), ProductsListAdapter.Listener {

    private var _binding: FragmentProductItemsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductItemsViewModel by viewModels {
        ProductItemsViewModel.factory(requireActivity().application as QuanLyBanHangApp, ProductScope.SALE)
    }

    private lateinit var productDialogHelper: ProductDialogHelper

    private val adapter by lazy { ProductsListAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productDialogHelper = ProductDialogHelper(this, viewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            val cats = viewModel.categoriesForPicker.value
            if (cats.isEmpty()) {
                Snackbar.make(binding.root, R.string.product_add_need_category, Snackbar.LENGTH_LONG).show()
            } else {
                productDialogHelper.showAddProductDialog(cats)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { list ->
                    adapter.submitList(list)
                    binding.textProductsEmpty.isVisible = list.isEmpty()
                    binding.recyclerProducts.isVisible = list.isNotEmpty()
                }
            }
        }
    }

    override fun onEditProduct(item: ProductListUi) {
        val cats = viewModel.categoriesForPicker.value
        if (cats.isEmpty()) {
            Snackbar.make(binding.root, R.string.product_add_need_category, Snackbar.LENGTH_LONG).show()
            return
        }
        productDialogHelper.showEditProductDialog(item, cats)
    }

    override fun onDeleteProduct(item: ProductListUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.product_delete_title)
            .setMessage(getString(R.string.product_delete_message, item.name))
            .setPositiveButton(R.string.product_delete_confirm) { _, _ ->
                viewModel.deleteProduct(item.id) { outcome ->
                    when (outcome) {
                        DeleteProductResult.Success -> Unit
                        is DeleteProductResult.BlockedByOrders -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.product_delete_blocked_orders, outcome.count),
                                Snackbar.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ProductItemsFragment = ProductItemsFragment()
    }
}
