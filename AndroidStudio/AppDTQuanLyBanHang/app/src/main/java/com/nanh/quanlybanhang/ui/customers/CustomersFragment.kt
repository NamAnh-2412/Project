package com.nanh.quanlybanhang.ui.customers

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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
import com.nanh.quanlybanhang.databinding.FragmentCustomersBinding
import com.nanh.quanlybanhang.ui.customers.xml.CustomersListAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomersViewModel by viewModels {
        CustomersViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter = CustomersListAdapter(
        onEdit = { showCustomerFormDialog(customer = it) },
        onDelete = { showDeleteConfirm(it) },
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
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

        binding.recyclerCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCustomers.adapter = adapter

        binding.editSearchCustomers.doAfterTextChanged { editable ->
            viewModel.setSearchQuery(editable?.toString().orEmpty())
        }

        binding.fabAddCustomer.setOnClickListener {
            showCustomerFormDialog(customer = null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.customers, viewModel.searchQuery) { list, q -> list to q }
                    .collect { (list, q) ->
                        adapter.submitList(list)
                        val empty = list.isEmpty()
                        binding.textCustomersEmpty.isVisible = empty
                        binding.recyclerCustomers.isVisible = !empty
                        if (empty) {
                            binding.textCustomersEmpty.setText(
                                if (q.isBlank()) R.string.customers_empty
                                else R.string.customers_search_empty,
                            )
                        }
                    }
            }
        }
    }

    private fun showCustomerFormDialog(customer: CustomerManageUi?) {
        val ctx = requireContext()
        val isEdit = customer != null
        val pad = (16 * resources.displayMetrics.density).toInt()
        val nameInput = EditText(ctx).apply {
            hint = ctx.getString(R.string.place_order_customer_name_hint)
            setText(customer?.name.orEmpty())
        }
        val phoneInput = EditText(ctx).apply {
            hint = ctx.getString(R.string.place_order_customer_phone_hint)
            inputType = InputType.TYPE_CLASS_PHONE
            setText(customer?.phoneRaw.orEmpty())
        }
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, pad)
            addView(nameInput)
            addView(phoneInput)
        }

        MaterialAlertDialogBuilder(ctx)
            .setTitle(if (isEdit) R.string.customers_edit else R.string.customers_add)
            .setView(container)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = nameInput.text?.toString().orEmpty()
                        if (name.isBlank()) {
                            val anchor = dialog.window?.decorView ?: requireView()
                            Snackbar.make(anchor, R.string.place_order_customer_quick_add_error, Snackbar.LENGTH_SHORT)
                                .show()
                            return@setOnClickListener
                        }
                        val phoneText = phoneInput.text?.toString()
                        dialog.dismiss()
                        if (isEdit) {
                            viewModel.updateCustomer(customer!!.id, name, phoneText)
                        } else {
                            viewModel.addCustomer(name, phoneText)
                        }
                    }
                }
                dialog.show()
            }
    }

    private fun showDeleteConfirm(customer: CustomerManageUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.customers_delete)
            .setMessage(getString(R.string.customers_delete_message, customer.name))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.customers_delete) { _, _ ->
                viewModel.deleteCustomer(customer.id)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): CustomersFragment = CustomersFragment()
    }
}
