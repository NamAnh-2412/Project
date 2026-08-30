package com.nanh.quanlybanhang.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
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

        if (binding.tabProductsSections.tabCount == 0) {
            binding.tabProductsSections.addTab(
                binding.tabProductsSections.newTab().setText(getString(R.string.products_tab_categories)),
            )
            binding.tabProductsSections.addTab(
                binding.tabProductsSections.newTab().setText(getString(R.string.products_tab_items)),
            )
            binding.tabProductsSections.addTab(
                binding.tabProductsSections.newTab().setText(getString(R.string.products_tab_choice_groups)),
            )
        }

        binding.tabProductsSections.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    showSection(tab?.position ?: 0)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            },
        )

        val restored = savedInstanceState?.getInt(KEY_SELECTED_TAB)?.coerceIn(0, TAB_COUNT - 1) ?: 0
        showSection(restored)
        binding.tabProductsSections.post {
            binding.tabProductsSections.getTabAt(restored)?.select()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB, binding.tabProductsSections.selectedTabPosition)
    }

    private fun showSection(position: Int) {
        val fragment = when (position) {
            TAB_CATEGORIES -> ProductCategoriesFragment.newInstance()
            TAB_PRODUCTS -> ProductItemsFragment.newInstance()
            TAB_CHOICE_GROUPS -> ChoiceGroupsFragment.newInstance()
            else -> ProductCategoriesFragment.newInstance()
        }
        childFragmentManager.commit {
            replace(R.id.containerProductsSections, fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_SELECTED_TAB = "products_selected_tab"
        private const val TAB_COUNT = 3
        private const val TAB_CATEGORIES = 0
        private const val TAB_PRODUCTS = 1
        private const val TAB_CHOICE_GROUPS = 2

        fun newInstance(): ProductsFragment = ProductsFragment()
    }
}
