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
import com.nanh.quanlybanhang.databinding.FragmentStockCountReportsBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.xml.StockCountReportsAdapter
import kotlinx.coroutines.launch

class StockCountReportsListFragment : Fragment() {

    private var _binding: FragmentStockCountReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockCountReportsListViewModel by viewModels {
        StockCountReportsListViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter = StockCountReportsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStockCountReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerStockCountReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStockCountReports.adapter = adapter

        binding.fabNewStockCount.setOnClickListener {
            requireParentFragment().childFragmentManager.commit {
                replace(R.id.containerWarehouse, StockCountEditorFragment.newInstance())
                addToBackStack(null)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reports.collect { list ->
                    adapter.submitList(list)
                    val empty = list.isEmpty()
                    binding.textStockCountReportsEmpty.isVisible = empty
                    binding.recyclerStockCountReports.isVisible = !empty
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): StockCountReportsListFragment = StockCountReportsListFragment()
    }
}
