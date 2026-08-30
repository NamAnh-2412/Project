package com.nanh.quanlybanhang.ui.warehouse.stock

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
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.ProductScope
import com.nanh.quanlybanhang.databinding.DialogStockInQuantityBinding
import com.nanh.quanlybanhang.databinding.FragmentStockProductsBinding
import com.nanh.quanlybanhang.ui.products.DeleteProductResult
import com.nanh.quanlybanhang.ui.products.ProductDialogHelper
import com.nanh.quanlybanhang.ui.products.ProductItemsViewModel
import com.nanh.quanlybanhang.ui.warehouse.stock.xml.StockProductsAdapter
import kotlinx.coroutines.launch

class StockProductsFragment : Fragment() {

    private var _binding: FragmentStockProductsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockProductsViewModel by viewModels {
        StockProductsViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val productItemsViewModel: ProductItemsViewModel by viewModels {
        ProductItemsViewModel.factory(requireActivity().application as QuanLyBanHangApp, ProductScope.WAREHOUSE)
    }

    /** Khởi tạo trong onCreate: registerForActivityResult cần fragment đã gắn host (tránh crash khi mở Kho). */
    private lateinit var productDialogHelper: ProductDialogHelper

    private val adapter = StockProductsAdapter(
        onStockIn = { showStockInDialog(it) },
        onEdit = { onEditProduct(it) },
        onDelete = { onDeleteProduct(it) },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productDialogHelper = ProductDialogHelper(this, productItemsViewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStockProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerStockProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStockProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            val cats = productItemsViewModel.categoriesForPicker.value
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
                    val empty = list.isEmpty()
                    binding.textStockProductsEmpty.isVisible = empty
                    binding.recyclerStockProducts.isVisible = !empty
                }
            }
        }
    }

    private fun showStockInDialog(row: StockProductRowUi) {
        val ctx = requireContext()
        val dBinding = DialogStockInQuantityBinding.inflate(LayoutInflater.from(ctx))
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.stock_in_dialog_title)
            .setMessage(row.name)
            .setView(dBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.stock_in_apply, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val n = dBinding.editQty.text?.toString()?.toIntOrNull()
                        if (n == null || n <= 0) {
                            Snackbar.make(binding.root, R.string.stock_in_invalid_qty, Snackbar.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        viewModel.addStock(row.id, n)
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
    }

    private fun onEditProduct(row: StockProductRowUi) {
        val cats = productItemsViewModel.categoriesForPicker.value
        if (cats.isEmpty()) {
            Snackbar.make(binding.root, R.string.product_add_need_category, Snackbar.LENGTH_LONG).show()
            return
        }
        val item = productItemsViewModel.products.value.firstOrNull { it.id == row.id }
        if (item == null) {
            Snackbar.make(binding.root, R.string.place_order_edit_product_missing, Snackbar.LENGTH_SHORT).show()
            return
        }
        productDialogHelper.showEditProductDialog(item, cats)
    }

    private fun onDeleteProduct(row: StockProductRowUi) {
        val item = productItemsViewModel.products.value.firstOrNull { it.id == row.id }
        if (item == null) {
            Snackbar.make(binding.root, R.string.place_order_edit_product_missing, Snackbar.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.product_delete_title)
            .setMessage(getString(R.string.product_delete_message, item.name))
            .setPositiveButton(R.string.product_delete_confirm) { _, _ ->
                productItemsViewModel.deleteProduct(item.id) { outcome ->
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
        fun newInstance(): StockProductsFragment = StockProductsFragment()
    }
}
