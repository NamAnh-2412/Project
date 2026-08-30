package com.nanh.quanlybanhang.ui.warehouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentWarehouseBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.StockCountReportsListFragment
import com.nanh.quanlybanhang.ui.warehouse.stock.StockProductsFragment
import com.nanh.quanlybanhang.ui.warehouse.stock.StockReceiptsListFragment

class WarehouseFragment : Fragment() {

    private var _binding: FragmentWarehouseBinding? = null
    private val binding get() = _binding!!

    private var suppressWarehouseTabCallback = false
    private var warehouseTabListener: TabLayout.OnTabSelectedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWarehouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val showUp = parentFragmentManager.backStackEntryCount > 0
        if (!showUp) {
            binding.toolbar.navigationIcon = null
        } else {
            binding.toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        if (binding.tabWarehouse.tabCount == 0) {
            binding.tabWarehouse.addTab(binding.tabWarehouse.newTab().setText(getString(R.string.warehouse_tab_stock)))
            binding.tabWarehouse.addTab(binding.tabWarehouse.newTab().setText(getString(R.string.warehouse_tab_receipts)))
            binding.tabWarehouse.addTab(binding.tabWarehouse.newTab().setText(getString(R.string.warehouse_tab_stock_count)))
        }

        warehouseTabListener =
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (suppressWarehouseTabCallback) return
                    showSection(tab?.position ?: 0)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            }
        binding.tabWarehouse.addOnTabSelectedListener(warehouseTabListener!!)

        val restored = savedInstanceState?.getInt(KEY_TAB)?.coerceIn(0, 2) ?: 0
        showSection(restored)
        suppressWarehouseTabCallback = true
        try {
            binding.tabWarehouse.getTabAt(restored)?.select()
        } finally {
            suppressWarehouseTabCallback = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, binding.tabWarehouse.selectedTabPosition)
    }

    private fun showSection(position: Int) {
        val fragment = when (position) {
            1 -> StockReceiptsListFragment.newInstance()
            2 -> StockCountReportsListFragment.newInstance()
            else -> StockProductsFragment.newInstance()
        }
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.containerWarehouse, fragment)
        }
    }

    override fun onDestroyView() {
        warehouseTabListener?.let { binding.tabWarehouse.removeOnTabSelectedListener(it) }
        warehouseTabListener = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_TAB = "warehouse_tab"

        fun newInstance(): WarehouseFragment = WarehouseFragment()
    }
}
