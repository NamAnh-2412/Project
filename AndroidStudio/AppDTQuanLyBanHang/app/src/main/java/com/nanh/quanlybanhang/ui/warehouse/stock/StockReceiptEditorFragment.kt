package com.nanh.quanlybanhang.ui.warehouse.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.nanh.quanlybanhang.databinding.DialogStockInQuantityBinding
import com.nanh.quanlybanhang.databinding.FragmentStockReceiptEditorBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.xml.StockReceiptEditorLinesAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StockReceiptEditorFragment : Fragment() {

    private var _binding: FragmentStockReceiptEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockReceiptEditorViewModel by viewModels {
        StockReceiptEditorViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val linesAdapter = StockReceiptEditorLinesAdapter { index ->
        viewModel.removeLineAt(index)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStockReceiptEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.recyclerLines.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLines.adapter = linesAdapter

        binding.buttonPickSupplier.setOnClickListener { showSupplierPick() }
        binding.buttonAddLine.setOnClickListener { showProductPickThenQty() }
        binding.buttonSaveReceipt.setOnClickListener { saveReceipt() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.suppliers, viewModel.supplierId) { sups, sid ->
                    sups to sid
                }.collect { (sups, sid) ->
                    val name = sups.firstOrNull { it.id == sid }?.name
                    binding.buttonPickSupplier.text = name
                        ?: getString(R.string.stock_receipt_supplier_none)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lines.collect { linesAdapter.submitList(it) }
            }
        }
    }

    private fun showSupplierPick() {
        val ctx = requireContext()
        val sups = viewModel.suppliers.value
        if (sups.isEmpty()) {
            Snackbar.make(binding.root, R.string.stock_receipt_no_suppliers, Snackbar.LENGTH_LONG).show()
            return
        }
        val names = sups.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.stock_receipt_pick_title)
            .setItems(names) { _, which ->
                viewModel.setSupplier(sups[which].id)
            }
            .show()
    }

    private fun showProductPickThenQty() {
        val ctx = requireContext()
        val prods = viewModel.products.value
        if (prods.isEmpty()) {
            Snackbar.make(binding.root, R.string.stock_products_empty, Snackbar.LENGTH_LONG).show()
            return
        }
        val names = prods.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.stock_receipt_pick_product_title)
            .setItems(names) { _, which ->
                val p = prods[which]
                showQtyForProduct(p.name) { qty ->
                    viewModel.addLine(p.id, p.name, qty)
                }
            }
            .show()
    }

    private fun showQtyForProduct(productName: String, onQty: (Int) -> Unit) {
        val ctx = requireContext()
        val dBinding = DialogStockInQuantityBinding.inflate(LayoutInflater.from(ctx))
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.stock_receipt_qty_label)
            .setMessage(productName)
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
                        onQty(n)
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
    }

    private fun saveReceipt() {
        viewLifecycleOwner.lifecycleScope.launch {
            val note = binding.editNote.text?.toString()
            val r = viewModel.saveReceipt(note)
            r.fold(
                onSuccess = {
                    Snackbar.make(binding.root, R.string.stock_receipt_saved, Snackbar.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                },
                onFailure = { e ->
                    when (e.message) {
                        "supplier" ->
                            Snackbar.make(binding.root, R.string.stock_receipt_supplier_required, Snackbar.LENGTH_LONG)
                                .show()
                        "lines" ->
                            Snackbar.make(binding.root, R.string.stock_receipt_lines_empty, Snackbar.LENGTH_LONG)
                                .show()
                        else ->
                            Snackbar.make(binding.root, R.string.stock_in_invalid_qty, Snackbar.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): StockReceiptEditorFragment = StockReceiptEditorFragment()
    }
}
