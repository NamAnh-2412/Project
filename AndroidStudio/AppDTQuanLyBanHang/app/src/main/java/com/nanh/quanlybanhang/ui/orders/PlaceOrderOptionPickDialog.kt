package com.nanh.quanlybanhang.ui.orders

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.util.formatVndCompact

internal fun Fragment.showPlaceOrderOptionPickDialog(
    viewModel: PlaceOrderViewModel,
    product: PlaceOrderProductUi,
    preselectedIds: Set<Long> = emptySet(),
    editingCartLineKey: String? = null,
) {
    val ctx = requireContext()
    val pad = (16 * resources.displayMetrics.density).toInt()

    val scroll = ScrollView(ctx)
    val container = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad / 2, pad, pad / 2)
    }
    scroll.addView(container)

    container.addView(
        TextView(ctx).apply {
            text = getString(R.string.place_order_pick_options_optional_hint)
            setPadding(0, 0, 0, pad / 2)
            textSize = 13f
            setTextColor(
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant),
            )
        },
    )

    val radioGroups = mutableListOf<RadioGroup>()

    for (group in product.linkedOptionGroups) {
        container.addView(
            TextView(ctx).apply {
                text = group.groupName
                setPadding(0, pad / 2, 0, pad / 4)
                textSize = 15f
                setTextColor(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface),
                )
            },
        )
        val rg = RadioGroup(ctx).apply { orientation = RadioGroup.VERTICAL }
        for (item in group.items) {
            val label = if (item.extraPrice > 0.0) {
                "${item.name} (+${formatVndCompact(item.extraPrice)})"
            } else {
                item.name
            }
            val rb = RadioButton(ctx).apply {
                text = label
                tag = item.id
                id = View.generateViewId()
            }
            rg.addView(rb)
            if (item.id in preselectedIds) {
                rg.check(rb.id)
            }
        }
        radioGroups.add(rg)
        container.addView(rg)
    }

    val title = if (editingCartLineKey != null) {
        getString(R.string.place_order_pick_options_edit_title, product.name)
    } else {
        getString(R.string.place_order_pick_options_title, product.name)
    }

    MaterialAlertDialogBuilder(ctx)
        .setTitle(title)
        .setView(scroll)
        .setNeutralButton(R.string.place_order_add_skip_options) { dialog, _ ->
            if (editingCartLineKey != null) {
                viewModel.replaceDraftLineOptions(editingCartLineKey, product, emptyList())
            } else {
                viewModel.addProductToSelectedDraft(product, emptyList())
            }
            dialog.dismiss()
        }
        .setPositiveButton(R.string.place_order_pick_options_ok, null)
        .setNegativeButton(android.R.string.cancel, null)
        .create()
        .also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val picked = mutableListOf<Long>()
                    for (rg in radioGroups) {
                        val cid = rg.checkedRadioButtonId
                        if (cid != -1) {
                            val rb = rg.findViewById<RadioButton>(cid)
                            val oid = rb.tag as? Long
                            if (oid != null) picked.add(oid)
                        }
                    }
                    dialog.dismiss()
                    if (editingCartLineKey != null) {
                        viewModel.replaceDraftLineOptions(editingCartLineKey, product, picked)
                    } else {
                        viewModel.addProductToSelectedDraft(product, picked)
                    }
                }
            }
            dialog.show()
        }
}
