package com.nanh.quanlybanhang.ui.products

import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.CategoryEntity
import com.nanh.quanlybanhang.data.local.OptionGroupEntity
import com.nanh.quanlybanhang.databinding.DialogAddProductBinding
import com.nanh.quanlybanhang.util.ProductImageStorage
import com.nanh.quanlybanhang.util.setProductThumbnail
import kotlinx.coroutines.launch

/**
 * Form thêm/sửa sản phẩm dùng chung (màn Sản phẩm và tab Kho).
 */
class ProductDialogHelper(
    private val fragment: Fragment,
    private val viewModel: ProductItemsViewModel,
) {

    private var activeProductDialog: DialogAddProductBinding? = null
    private var dialogImagePath: String? = null
    private var dialogImageRemovedExplicitly: Boolean = false

    private val pickProductImage =
        fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val dialogBinding = activeProductDialog ?: return@registerForActivityResult
            if (uri == null) return@registerForActivityResult
            fragment.viewLifecycleOwner.lifecycleScope.launch {
                runCatching {
                    ProductImageStorage.copyFromPickerUri(fragment.requireContext(), uri)
                }.onSuccess { path ->
                    dialogImagePath = path
                    dialogImageRemovedExplicitly = false
                    dialogBinding.buttonClearProductImage.isEnabled = true
                    refreshPreview(dialogBinding, categoriesSnapshot())
                }.onFailure {
                    Snackbar.make(snackbarAnchor(), R.string.product_image_pick_failed, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    private fun snackbarAnchor(): android.view.View = fragment.requireView()

    private fun categoriesSnapshot(): List<CategoryEntity> = viewModel.categoriesForPicker.value

    private fun refreshPreview(dialogBinding: DialogAddProductBinding, cats: List<CategoryEntity>) {
        if (cats.isEmpty()) return
        val label = dialogBinding.autoCompleteCategory.text?.toString()?.trim().orEmpty()
        val cid = cats.firstOrNull { it.name == label }?.id ?: cats.first().id
        val path =
            when {
                dialogImageRemovedExplicitly -> null
                else -> dialogImagePath
            }
        dialogBinding.imageProductPreview.setProductThumbnail(path, cid)
    }

    private fun wireCategoryPreviewUpdates(dialogBinding: DialogAddProductBinding, cats: List<CategoryEntity>) {
        dialogBinding.autoCompleteCategory.setOnItemClickListener { _, _, _, _ ->
            refreshPreview(dialogBinding, cats)
        }
    }

    private fun bindLinkedOptionGroupsSection(
        dialogBinding: DialogAddProductBinding,
        allGroups: List<OptionGroupEntity>,
        selectedIds: Set<Long>,
    ) {
        val container = dialogBinding.containerProductOptionGroups
        container.removeAllViews()
        if (allGroups.isEmpty()) {
            dialogBinding.textLinkedGroupsLabel.isVisible = false
            dialogBinding.textLinkedGroupsHint.isVisible = false
            container.isVisible = false
            return
        }
        dialogBinding.textLinkedGroupsLabel.isVisible = true
        dialogBinding.textLinkedGroupsHint.isVisible = true
        container.isVisible = true
        val ctx = fragment.requireContext()
        for (g in allGroups) {
            container.addView(
                CheckBox(ctx).apply {
                    text = g.name
                    tag = g.id
                    isChecked = selectedIds.contains(g.id)
                    setTextColor(ContextCompat.getColor(ctx, R.color.white))
                },
            )
        }
    }

    private fun collectLinkedGroupIds(dialogBinding: DialogAddProductBinding): List<Long> {
        val container = dialogBinding.containerProductOptionGroups
        val out = mutableListOf<Long>()
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i)
            if (v is CheckBox && v.isChecked) {
                val gid = v.tag as? Long ?: continue
                out.add(gid)
            }
        }
        return out.distinct()
    }

    fun showAddProductDialog(categories: List<CategoryEntity>) {
        val inflater = fragment.layoutInflater
        val dialogBinding = DialogAddProductBinding.inflate(inflater)
        activeProductDialog = dialogBinding
        dialogImagePath = null
        dialogImageRemovedExplicitly = false

        attachCategoryDropdownAdapter(dialogBinding, categories)
        dialogBinding.autoCompleteCategory.setText(categories.first().name, false)
        dialogBinding.buttonClearProductImage.isEnabled = false
        refreshPreview(dialogBinding, categories)
        wireCategoryPreviewUpdates(dialogBinding, categories)
        bindLinkedOptionGroupsSection(
            dialogBinding,
            viewModel.optionGroupsForPicker.value,
            emptySet(),
        )

        dialogBinding.buttonPickProductImage.setOnClickListener {
            pickProductImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        dialogBinding.buttonClearProductImage.setOnClickListener {
            dialogImagePath = null
            dialogImageRemovedExplicitly = true
            dialogBinding.buttonClearProductImage.isEnabled = false
            refreshPreview(dialogBinding, categories)
        }

        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.product_add_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnDismissListener {
            activeProductDialog = null
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editProductName.text?.toString().orEmpty().trim()
                val price = parsePositiveDouble(dialogBinding.editProductPrice.text?.toString().orEmpty())
                val pickedName = dialogBinding.autoCompleteCategory.text?.toString().orEmpty().trim()
                val resolvedId = categories.firstOrNull { it.name == pickedName }?.id
                if (name.isEmpty()) return@setOnClickListener
                if (resolvedId == null || pickedName.isEmpty()) {
                    Snackbar.make(snackbarAnchor(), R.string.product_add_pick_category, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (price == null) {
                    Snackbar.make(snackbarAnchor(), R.string.product_add_invalid_price, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val imgPath =
                    when {
                        dialogImageRemovedExplicitly -> null
                        else -> dialogImagePath?.trim()?.takeIf { it.isNotEmpty() }
                    }
                val linkedIds = collectLinkedGroupIds(dialogBinding)
                viewModel.insertProduct(name, price, resolvedId, imgPath, linkedIds)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun showEditProductDialog(item: ProductListUi, categories: List<CategoryEntity>) {
        val inflater = fragment.layoutInflater
        val dialogBinding = DialogAddProductBinding.inflate(inflater)
        activeProductDialog = dialogBinding
        dialogImagePath = item.imageLocalPath
        dialogImageRemovedExplicitly = false

        attachCategoryDropdownAdapter(dialogBinding, categories)
        val catName = categories.firstOrNull { it.id == item.categoryId }?.name ?: categories.first().name
        dialogBinding.autoCompleteCategory.setText(catName, false)
        dialogBinding.editProductName.setText(item.name)
        dialogBinding.editProductPrice.setText(formatPriceInput(item.unitPrice))

        dialogBinding.buttonClearProductImage.isEnabled = !item.imageLocalPath.isNullOrBlank()
        refreshPreview(dialogBinding, categories)
        wireCategoryPreviewUpdates(dialogBinding, categories)
        bindLinkedOptionGroupsSection(
            dialogBinding,
            viewModel.optionGroupsForPicker.value,
            item.linkedOptionGroupIds.toSet(),
        )

        dialogBinding.buttonPickProductImage.setOnClickListener {
            pickProductImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        dialogBinding.buttonClearProductImage.setOnClickListener {
            dialogImagePath = null
            dialogImageRemovedExplicitly = true
            dialogBinding.buttonClearProductImage.isEnabled = false
            refreshPreview(dialogBinding, categories)
        }

        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.product_edit_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnDismissListener {
            activeProductDialog = null
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editProductName.text?.toString().orEmpty().trim()
                val price = parsePositiveDouble(dialogBinding.editProductPrice.text?.toString().orEmpty())
                val pickedName = dialogBinding.autoCompleteCategory.text?.toString().orEmpty().trim()
                val resolvedId = categories.firstOrNull { it.name == pickedName }?.id
                if (name.isEmpty()) return@setOnClickListener
                if (resolvedId == null || pickedName.isEmpty()) {
                    Snackbar.make(snackbarAnchor(), R.string.product_add_pick_category, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (price == null) {
                    Snackbar.make(snackbarAnchor(), R.string.product_add_invalid_price, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val imageForDb =
                    when {
                        dialogImageRemovedExplicitly -> null
                        else -> dialogImagePath?.trim()?.takeIf { it.isNotEmpty() }
                    }
                val linkedIds = collectLinkedGroupIds(dialogBinding)
                viewModel.updateProduct(item.id, name, price, resolvedId, imageForDb, linkedIds)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun attachCategoryDropdownAdapter(dialogBinding: DialogAddProductBinding, categories: List<CategoryEntity>) {
        val names = categories.map { it.name }
        dialogBinding.autoCompleteCategory.setAdapter(
            ArrayAdapter(fragment.requireContext(), android.R.layout.simple_dropdown_item_1line, names),
        )
    }

    private fun formatPriceInput(unitPrice: Double): String {
        if (unitPrice % 1.0 == 0.0) {
            return unitPrice.toLong().toString()
        }
        return unitPrice.toString().trimEnd('0').trimEnd('.')
    }

    private fun parsePositiveDouble(raw: String): Double? {
        val s = raw.trim().replace(',', '.').replace(Regex("\\s+"), "")
        if (s.isEmpty()) return null
        val v = s.toDoubleOrNull() ?: return null
        return if (v > 0) v else null
    }
}
