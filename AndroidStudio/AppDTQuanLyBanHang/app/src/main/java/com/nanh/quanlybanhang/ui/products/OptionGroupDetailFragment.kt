package com.nanh.quanlybanhang.ui.products

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
import com.nanh.quanlybanhang.databinding.DialogAddOptionItemBinding
import com.nanh.quanlybanhang.databinding.FragmentOptionGroupDetailBinding
import com.nanh.quanlybanhang.ui.products.xml.OptionItemsListAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class OptionGroupDetailFragment : Fragment(), OptionItemsListAdapter.Listener {

    private var _binding: FragmentOptionGroupDetailBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID).also { check(it > 0L) }
    }

    private val viewModel: OptionGroupDetailViewModel by viewModels {
        OptionGroupDetailViewModel.factory(requireActivity().application as QuanLyBanHangApp, groupId)
    }

    private val adapter by lazy { OptionItemsListAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOptionGroupDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbarOptionGroupDetail.setNavigationOnClickListener {
            parentFragment?.childFragmentManager?.popBackStack()
        }

        binding.recyclerOptionItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOptionItems.adapter = adapter

        binding.fabAddOptionItem.setOnClickListener {
            showOptionItemDialog(modeAdd = true, item = null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.group.collect { entity ->
                        binding.toolbarOptionGroupDetail.title =
                            entity?.name ?: getString(R.string.option_group_detail_title_placeholder)
                    }
                }
                launch {
                    viewModel.items.collect { list ->
                        adapter.submitList(list)
                        binding.textOptionItemsEmpty.isVisible = list.isEmpty()
                        binding.recyclerOptionItems.isVisible = list.isNotEmpty()
                    }
                }
            }
        }
    }

    override fun onEditItem(item: OptionItemRowUi) {
        showOptionItemDialog(modeAdd = false, item = item)
    }

    override fun onDeleteItem(item: OptionItemRowUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.option_item_delete_title)
            .setMessage(getString(R.string.option_item_delete_message, item.name))
            .setPositiveButton(R.string.option_group_delete_confirm) { _, _ ->
                viewModel.deleteItem(item.id)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showOptionItemDialog(modeAdd: Boolean, item: OptionItemRowUi?) {
        val dialogBinding = DialogAddOptionItemBinding.inflate(layoutInflater)
        if (!modeAdd && item != null) {
            dialogBinding.editOptionItemName.setText(item.name)
            dialogBinding.editOptionItemExtraPrice.setText(formatExtraPriceInput(item.extraPrice))
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (modeAdd) R.string.option_item_add_title else R.string.option_item_edit_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editOptionItemName.text?.toString().orEmpty().trim()
                val extra = parseNonNegativeDouble(dialogBinding.editOptionItemExtraPrice.text?.toString().orEmpty())
                if (name.isEmpty()) return@setOnClickListener
                if (extra == null) {
                    Snackbar.make(binding.root, R.string.option_item_extra_price_invalid, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (modeAdd) {
                    viewModel.insertItem(name, extra)
                } else if (item != null) {
                    viewModel.updateItem(item.id, name, extra)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun parseNonNegativeDouble(raw: String): Double? {
        val s = raw.trim().replace(',', '.').replace(Regex("\\s+"), "")
        if (s.isEmpty()) return 0.0
        val v = s.toDoubleOrNull() ?: return null
        return if (v >= 0.0) v else null
    }

    private fun formatExtraPriceInput(value: Double): String {
        if (value % 1.0 == 0.0) return value.toLong().toString()
        return value.toString().trimEnd('0').trimEnd('.')
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_GROUP_ID = "option_group_id"

        fun newInstance(groupId: Long): OptionGroupDetailFragment {
            val f = OptionGroupDetailFragment()
            f.arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
            return f
        }
    }
}
