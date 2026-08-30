package com.nanh.quanlybanhang.ui.orders

import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.DialogPickCustomerBinding
import com.nanh.quanlybanhang.ui.orders.xml.CustomerPickAdapter
import kotlinx.coroutines.launch

/**
 * Lưu khách (tên + SĐT) vào DB — dùng từ màn Lên đơn hoặc Thêm khách ở bước thanh toán.
 */
fun Fragment.showCustomerInfoSaveDialog(
    viewModel: PlaceOrderViewModel,
    onSaved: ((Long) -> Unit)? = null,
) {
    val ctx = requireContext()
    val pad = (16 * resources.displayMetrics.density).toInt()
    val nameInput = EditText(ctx).apply {
        hint = ctx.getString(R.string.place_order_customer_name_hint)
    }
    val phoneInput = EditText(ctx).apply {
        hint = ctx.getString(R.string.place_order_customer_phone_hint)
        inputType = InputType.TYPE_CLASS_PHONE
    }
    val container = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad / 2, pad, pad)
        addView(nameInput)
        addView(phoneInput)
    }

    MaterialAlertDialogBuilder(ctx)
        .setTitle(R.string.place_order_customer_info_save_title)
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
                    dialog.dismiss()
                    viewLifecycleOwner.lifecycleScope.launch {
                        runCatching {
                            val id = viewModel.quickAddCustomer(
                                name,
                                phoneInput.text?.toString(),
                            )
                            onSaved?.invoke(id)
                        }.onFailure {
                            Snackbar.make(
                                requireView(),
                                R.string.place_order_customer_quick_add_error,
                                Snackbar.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
            dialog.show()
        }
}

/** Dialog danh sách + ô tìm theo tên/SĐT. */
fun Fragment.showCustomerPickDialog(
    customers: List<CustomerPickUi>,
    onPick: (Long) -> Unit,
) {
    if (customers.isEmpty()) {
        Snackbar.make(requireView(), R.string.place_order_customer_pick_empty, Snackbar.LENGTH_LONG).show()
        return
    }
    val ctx = requireContext()
    val dialogBinding = DialogPickCustomerBinding.inflate(LayoutInflater.from(ctx))

    var alertDialog: AlertDialog? = null
    val adapter = CustomerPickAdapter { customer ->
        onPick(customer.id)
        alertDialog?.dismiss()
    }
    dialogBinding.recyclerCustomers.layoutManager = LinearLayoutManager(ctx)
    dialogBinding.recyclerCustomers.adapter = adapter

    fun applyFilter(raw: String) {
        val q = raw.trim().lowercase()
        val filtered = if (q.isEmpty()) {
            customers
        } else {
            customers.filter { c ->
                c.name.lowercase().contains(q) ||
                    (c.phone?.contains(q, ignoreCase = true) == true) ||
                    c.label.lowercase().contains(q)
            }
        }
        adapter.submitList(filtered)
        dialogBinding.textPickEmpty.isVisible = filtered.isEmpty()
    }

    applyFilter("")
    dialogBinding.editSearchCustomer.doAfterTextChanged { text ->
        applyFilter(text?.toString().orEmpty())
    }

    alertDialog = MaterialAlertDialogBuilder(ctx)
        .setTitle(R.string.place_order_pick_customer_dialog_title)
        .setView(dialogBinding.root)
        .setNegativeButton(android.R.string.cancel, null)
        .create()

    alertDialog?.show()
}
