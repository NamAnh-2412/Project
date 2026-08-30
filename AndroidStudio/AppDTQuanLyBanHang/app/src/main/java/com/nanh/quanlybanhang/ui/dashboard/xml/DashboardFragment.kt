package com.nanh.quanlybanhang.ui.dashboard.xml

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentDashboardBinding
import com.nanh.quanlybanhang.ui.dashboard.DashboardViewModel
import com.nanh.quanlybanhang.ui.navigation.DashboardTopTabNavigator
import com.nanh.quanlybanhang.util.VietnameseDates
import com.nanh.quanlybanhang.util.bindShopAvatarBadge
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val statAdapter = StatCardsAdapter()
    private val chartAdapter = ChartBarsAdapter()
    private val recentAdapter = RecentOrdersAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.textHeaderDate.text = VietnameseDates.todayHeading()

        binding.recyclerStats.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerStats.adapter = statAdapter
        binding.recyclerStats.isNestedScrollingEnabled = false

        binding.recyclerChartBars.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChartBars.adapter = chartAdapter
        binding.recyclerChartBars.isNestedScrollingEnabled = false

        binding.recyclerRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentOrders.adapter = recentAdapter
        binding.recyclerRecentOrders.isNestedScrollingEnabled = false

        bindTopTabs()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                (requireActivity().application as QuanLyBanHangApp).shopProfileRepository.observeProfile().collect { profile ->
                    val name = profile?.name?.takeIf { it.isNotBlank() } ?: getString(R.string.app_name)
                    binding.textAppTitle.text = name
                    bindShopAvatarBadge(
                        binding.imageShopAvatar,
                        binding.textShopInitials,
                        profile?.avatarLocalPath,
                        name,
                        maxSidePx = 384,
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    statAdapter.submitList(state.statCards)
                    chartAdapter.submit(state.monthBars)
                    recentAdapter.submitList(state.recentOrders)
                    val hasRecent = state.recentOrders.isNotEmpty()
                    binding.textRecentEmpty.isVisible = !hasRecent
                    binding.cardRecentOrders.isVisible = hasRecent
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindTopTabs() {
        val inactive = requireContext().getColor(R.color.text_secondary)
        val active = requireContext().getColor(R.color.gold_accent)
        fun selectTab(selected: TextView) {
            listOf(
                binding.tabOverview,
                binding.tabOrdersTop,
                binding.tabProductsTop,
                binding.tabCustomersTop,
                binding.tabSuppliersTop,
                binding.tabWarehouseTop,
                binding.tabReportsTop,
            ).forEach { tv ->
                tv.setTextColor(if (tv === selected) active else inactive)
                tv.setTypeface(null, if (tv === selected) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        val navigator = activity as? DashboardTopTabNavigator

        selectTab(binding.tabOverview)
        binding.tabOverview.setOnClickListener {
            selectTab(binding.tabOverview)
            binding.scrollMain.smoothScrollTo(0, 0)
        }
        binding.tabOrdersTop.setOnClickListener {
            selectTab(binding.tabOrdersTop)
            navigator?.openOrderHistoryFromDashboardTab()
                ?: Toast.makeText(
                    requireContext(),
                    getString(R.string.placeholder_tab, getString(R.string.tab_order_history)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
        binding.tabProductsTop.setOnClickListener {
            selectTab(binding.tabProductsTop)
            navigator?.openProductsFromDashboardTab()
                ?: Toast.makeText(
                    requireContext(),
                    getString(R.string.placeholder_tab, getString(R.string.tab_products)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
        binding.tabCustomersTop.setOnClickListener {
            selectTab(binding.tabCustomersTop)
            navigator?.openCustomersFromDashboardTab()
                ?: Toast.makeText(
                    requireContext(),
                    getString(R.string.placeholder_tab, getString(R.string.tab_customers)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
        binding.tabSuppliersTop.setOnClickListener {
            selectTab(binding.tabSuppliersTop)
            navigator?.openSuppliersFromDashboardTab()
                ?: Toast.makeText(
                    requireContext(),
                    getString(R.string.placeholder_tab, getString(R.string.tab_suppliers)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
        binding.tabWarehouseTop.setOnClickListener {
            selectTab(binding.tabWarehouseTop)
            navigator?.openWarehouseFromDashboardTab()
                ?: Toast.makeText(
                    requireContext(),
                    getString(R.string.placeholder_tab, getString(R.string.tab_warehouse)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
        binding.tabReportsTop.setOnClickListener {
            selectTab(binding.tabReportsTop)
            navigator?.openReportsFromDashboardTab()
                ?: Toast.makeText(
                    requireContext(),
                    getString(R.string.placeholder_tab, getString(R.string.tab_reports)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    companion object {
        fun newInstance(): DashboardFragment = DashboardFragment()
    }
}
