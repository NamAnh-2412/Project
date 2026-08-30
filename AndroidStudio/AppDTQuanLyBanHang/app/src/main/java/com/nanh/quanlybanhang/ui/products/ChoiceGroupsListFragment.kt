package com.nanh.quanlybanhang.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.DialogAddOptionGroupBinding
import com.nanh.quanlybanhang.databinding.FragmentChoiceGroupsListBinding
import com.nanh.quanlybanhang.ui.products.xml.OptionGroupsManageAdapter
import kotlinx.coroutines.launch

class ChoiceGroupsListFragment : Fragment(), OptionGroupsManageAdapter.Listener {

    private var _binding: FragmentChoiceGroupsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChoiceGroupsCatalogViewModel by viewModels {
        ChoiceGroupsCatalogViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter by lazy { OptionGroupsManageAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChoiceGroupsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerChoiceGroups.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChoiceGroups.adapter = adapter

        binding.fabAddChoiceGroup.setOnClickListener {
            showAddOptionGroupDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.choiceGroups.collect { list ->
                    adapter.submitList(list)
                    binding.textChoiceGroupsEmpty.isVisible = list.isEmpty()
                    binding.recyclerChoiceGroups.isVisible = list.isNotEmpty()
                }
            }
        }
    }

    override fun onOpenGroup(item: OptionGroupRowUi) {
        requireParentFragment().childFragmentManager.commit {
            replace(R.id.containerChoiceGroupsFlow, OptionGroupDetailFragment.newInstance(item.id))
            addToBackStack(null)
        }
    }

    override fun onEditGroup(item: OptionGroupRowUi) {
        val dialogBinding = DialogAddOptionGroupBinding.inflate(layoutInflater)
        dialogBinding.editOptionGroupName.setText(item.name)
        dialogBinding.editOptionGroupName.setSelection(dialogBinding.editOptionGroupName.text?.length ?: 0)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.option_group_edit_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editOptionGroupName.text?.toString().orEmpty().trim()
                if (name.isEmpty()) return@setOnClickListener
                viewModel.updateOptionGroup(item.id, name)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDeleteGroup(item: OptionGroupRowUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.option_group_delete_title)
            .setMessage(getString(R.string.option_group_delete_message, item.name))
            .setPositiveButton(R.string.option_group_delete_confirm) { _, _ ->
                viewModel.deleteOptionGroup(item.id)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showAddOptionGroupDialog() {
        val dialogBinding = DialogAddOptionGroupBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.option_group_add_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editOptionGroupName.text?.toString().orEmpty().trim()
                if (name.isEmpty()) return@setOnClickListener
                viewModel.insertOptionGroup(name)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ChoiceGroupsListFragment = ChoiceGroupsListFragment()
    }
}
