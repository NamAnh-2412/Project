package com.nanh.quanlybanhang.ui.warehouse.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentStockReceiptsBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.StockReceiptEditorFragment
import com.nanh.quanlybanhang.ui.warehouse.stock.xml.StockReceiptsAdapter
import kotlinx.coroutines.launch

class StockReceiptsListFragment : Fragment() {

    private var _binding: FragmentStockReceiptsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockReceiptsListViewModel by viewModels {
        StockReceiptsListViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter = StockReceiptsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStockReceiptsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerReceipts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReceipts.adapter = adapter

        binding.fabNewReceipt.setOnClickListener {
            requireParentFragment().childFragmentManager.commit {
                replace(R.id.containerWarehouse, StockReceiptEditorFragment.newInstance())
                addToBackStack(null)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.receipts.collect { list ->
                    adapter.submitList(list)
                    val empty = list.isEmpty()
                    binding.textReceiptsEmpty.isVisible = empty
                    binding.recyclerReceipts.isVisible = !empty
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): StockReceiptsListFragment = StockReceiptsListFragment()
    }
}
