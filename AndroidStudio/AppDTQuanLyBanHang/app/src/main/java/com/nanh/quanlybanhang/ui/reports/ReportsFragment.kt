package com.nanh.quanlybanhang.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentReportsBinding
import com.nanh.quanlybanhang.ui.reports.xml.ReportsHourlyAdapter
import com.nanh.quanlybanhang.ui.reports.xml.ReportsPaymentAdapter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels {
        ReportsViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val hourlyAdapter = ReportsHourlyAdapter()
    private val paymentAdapter = ReportsPaymentAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.navigationIcon = null

        binding.recyclerHourly.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHourly.adapter = hourlyAdapter

        binding.recyclerPayment.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPayment.adapter = paymentAdapter

        if (binding.tabReports.tabCount == 0) {
            binding.tabReports.addTab(binding.tabReports.newTab().setText(R.string.reports_tab_summary))
            binding.tabReports.addTab(binding.tabReports.newTab().setText(R.string.reports_tab_payment))
        }

        binding.tabReports.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val summary = tab?.position == 0
                binding.scrollSummary.visibility = if (summary) View.VISIBLE else View.GONE
                binding.scrollDetail.visibility = if (summary) View.GONE else View.VISIBLE
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        binding.scrollSummary.visibility = View.VISIBLE
        binding.scrollDetail.visibility = View.GONE

        binding.buttonPickRange.setOnClickListener {
            showRangePicker()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.textRangeLabel.text = state.rangeLabel
                    binding.textStatOrders.text = state.orderCount.toString()
                    binding.textStatRevenue.text = state.totalRevenueLabel
                    binding.textStatItems.text = state.itemsSold.toString()
                    hourlyAdapter.submit(state.hourlyBars, state.hourlyBarMax)
                    paymentAdapter.submitList(state.paymentRows)
                }
            }
        }
    }

    private fun showRangePicker() {
        val zone = ZoneId.systemDefault()
        val state = viewModel.uiState.value
        val start = state.fromDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = state.toDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val today = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val constraints = CalendarConstraints.Builder()
            .setEnd(today)
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setCalendarConstraints(constraints)
            .setTitleText(getString(R.string.reports_pick_range_title))
            .setSelection(Pair.create(start, end))
            .build()
        picker.addOnPositiveButtonClickListener { sel ->
            if (sel == null) return@addOnPositiveButtonClickListener
            val z = ZoneId.systemDefault()
            val from = Instant.ofEpochMilli(sel.first).atZone(z).toLocalDate()
            val to = Instant.ofEpochMilli(sel.second).atZone(z).toLocalDate()
            viewModel.setDateRange(from, to)
        }
        picker.show(parentFragmentManager, "reports_date_range")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ReportsFragment = ReportsFragment()
    }
}
