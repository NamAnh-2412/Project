package com.nanh.quanlybanhang.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.FragmentOrdersBinding
import com.nanh.quanlybanhang.ui.navigation.DashboardTopTabNavigator
import com.nanh.quanlybanhang.ui.orders.xml.OrdersListAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdersListViewModel by viewModels {
        OrdersListViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter = OrdersListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.adapter = adapter

        if (arguments?.getBoolean(ARG_FROM_DASHBOARD, false) == true) {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        (activity as? DashboardTopTabNavigator)?.returnToDashboardFromOrderHistory()
                    }
                },
            )
        }

        binding.editSearchOrders.doAfterTextChanged { editable ->
            viewModel.setSearchQuery(editable?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.orders, viewModel.searchQuery) { list, q -> list to q }
                    .collect { (list, q) ->
                        adapter.submitList(list)
                        val empty = list.isEmpty()
                        binding.textOrdersEmpty.isVisible = empty
                        binding.recyclerOrders.isVisible = !empty
                        if (empty) {
                            binding.textOrdersEmpty.setText(
                                if (q.isBlank()) R.string.orders_list_empty
                                else R.string.orders_search_empty,
                            )
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FROM_DASHBOARD = "from_dashboard"

        fun newInstance(fromDashboard: Boolean = false): OrdersFragment = OrdersFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_FROM_DASHBOARD, fromDashboard)
            }
        }
    }
}
