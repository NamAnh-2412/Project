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
import com.nanh.quanlybanhang.databinding.DialogAddCategoryBinding
import com.nanh.quanlybanhang.databinding.FragmentProductCategoriesBinding
import com.nanh.quanlybanhang.ui.products.xml.CategoriesListAdapter
import kotlinx.coroutines.launch

class ProductCategoriesFragment : Fragment(), CategoriesListAdapter.Listener {

    private var _binding: FragmentProductCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesCatalogViewModel by viewModels {
        CategoriesCatalogViewModel.factory(requireActivity().application as QuanLyBanHangApp)
    }

    private val adapter by lazy { CategoriesListAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategories.adapter = adapter

        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { list ->
                    adapter.submitList(list)
                    binding.textCategoriesEmpty.isVisible = list.isEmpty()
                    binding.recyclerCategories.isVisible = list.isNotEmpty()
                }
            }
        }
    }

    override fun onEditCategory(item: CategoryRowUi) {
        showEditCategoryDialog(item)
    }

    override fun onDeleteCategory(item: CategoryRowUi) {
        confirmDeleteCategory(item)
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.category_add_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editCategoryName.text?.toString().orEmpty().trim()
                if (name.isEmpty()) return@setOnClickListener
                viewModel.insertCategory(name)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showEditCategoryDialog(item: CategoryRowUi) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        dialogBinding.editCategoryName.setText(item.name)
        dialogBinding.editCategoryName.setSelection(dialogBinding.editCategoryName.text?.length ?: 0)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.category_edit_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editCategoryName.text?.toString().orEmpty().trim()
                if (name.isEmpty()) return@setOnClickListener
                viewModel.updateCategory(item.id, name)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDeleteCategory(item: CategoryRowUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.category_delete_title)
            .setMessage(getString(R.string.category_delete_message, item.name))
            .setPositiveButton(R.string.category_delete_confirm) { _, _ ->
                viewModel.deleteCategory(item.id) { outcome ->
                    when (outcome) {
                        DeleteCategoryResult.Success -> Unit
                        is DeleteCategoryResult.BlockedByProducts -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.category_delete_blocked, outcome.count),
                                Snackbar.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ProductCategoriesFragment = ProductCategoriesFragment()
    }
}
