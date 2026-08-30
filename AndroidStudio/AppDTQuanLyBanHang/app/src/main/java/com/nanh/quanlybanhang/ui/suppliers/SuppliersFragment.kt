package com.nanh.quanlybanhang.ui.suppliers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
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
import com.nanh.quanlybanhang.databinding.DialogAddSupplierBinding
import com.nanh.quanlybanhang.databinding.FragmentSuppliersBinding
import com.nanh.quanlybanhang.ui.suppliers.xml.SuppliersListAdapter
import kotlinx.coroutines.launch

class SuppliersFragment : Fragment() {

    private var _binding: FragmentSuppliersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuppliersViewModel by viewModels {
        SuppliersViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter = SuppliersListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSuppliersBinding.inflate(inflater, container, false)
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

        binding.recyclerSuppliers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSuppliers.adapter = adapter

        binding.fabAddSupplier.setOnClickListener {
            showAddSupplierDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.suppliers.collect { list ->
                    adapter.submitList(list)
                    val empty = list.isEmpty()
                    binding.textSuppliersEmpty.isVisible = empty
                    binding.recyclerSuppliers.isVisible = !empty
                }
            }
        }
    }

    private fun showAddSupplierDialog() {
        val ctx = requireContext()
        val dialogBinding = DialogAddSupplierBinding.inflate(LayoutInflater.from(ctx))
        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.suppliers_add_dialog_title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = dialogBinding.editSupplierName.text?.toString().orEmpty()
                        if (name.isBlank()) {
                            Snackbar.make(
                                dialogBinding.root,
                                R.string.suppliers_name_required,
                                Snackbar.LENGTH_SHORT,
                            ).show()
                            return@setOnClickListener
                        }
                        viewModel.addSupplier(
                            name = name,
                            phone = dialogBinding.editSupplierPhone.text?.toString(),
                            address = dialogBinding.editSupplierAddress.text?.toString(),
                            note = dialogBinding.editSupplierNote.text?.toString(),
                        )
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SuppliersFragment = SuppliersFragment()
    }
}
