package com.nanh.quanlybanhang.ui.warehouse.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentStockCountEditorBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.xml.StockCountEditorAdapter
import kotlinx.coroutines.launch

class StockCountEditorFragment : Fragment() {

    private var _binding: FragmentStockCountEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockCountEditorViewModel by viewModels {
        StockCountEditorViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter = StockCountEditorAdapter { productId, text ->
        viewModel.updateQty(productId, text)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStockCountEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.recyclerLines.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLines.adapter = adapter

        binding.buttonSave.setOnClickListener { save() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lines.collect { adapter.submitList(it) }
            }
        }
    }

    private fun save() {
        val note = binding.editNote.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        viewModel.save(note) { result ->
            when (result) {
                StockCountEditorViewModel.SaveStockCountResult.Success -> {
                    Snackbar.make(binding.root, R.string.stock_count_saved, Snackbar.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                StockCountEditorViewModel.SaveStockCountResult.NoProducts -> {
                    Snackbar.make(binding.root, R.string.stock_products_empty, Snackbar.LENGTH_LONG).show()
                }
                is StockCountEditorViewModel.SaveStockCountResult.InvalidQuantity -> {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.stock_count_invalid_qty_named, result.productName),
                        Snackbar.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): StockCountEditorFragment = StockCountEditorFragment()
    }
}
