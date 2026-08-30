package com.nanh.quanlybanhang.ui.warehouse.stock.xml

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemStockCountEditorRowBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.StockCountLineUi

class StockCountEditorAdapter(
    private val onQtyChanged: (Long, String) -> Unit,
) : ListAdapter<StockCountLineUi, StockCountEditorAdapter.Vh>(
    object : DiffUtil.ItemCallback<StockCountLineUi>() {
        override fun areItemsTheSame(a: StockCountLineUi, b: StockCountLineUi): Boolean = a.productId == b.productId
        override fun areContentsTheSame(a: StockCountLineUi, b: StockCountLineUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemStockCountEditorRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, onQtyChanged)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: Vh) {
        holder.clearWatcher()
        super.onViewRecycled(holder)
    }

    class Vh(
        private val binding: ItemStockCountEditorRowBinding,
        private val onQtyChanged: (Long, String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var qtyWatcher: TextWatcher? = null

        fun clearWatcher() {
            qtyWatcher?.let { binding.editQty.removeTextChangedListener(it) }
            qtyWatcher = null
        }

        fun bind(item: StockCountLineUi) {
            val ctx = binding.root.context
            binding.textProductName.text = item.name
            binding.textProductCategory.text = item.categoryName
            binding.textSystemStock.text = ctx.getString(R.string.stock_count_system_before, item.systemStock)

            val et = binding.editQty
            clearWatcher()
            val cur = et.text?.toString() ?: ""
            if (cur != item.qtyInput) {
                et.setText(item.qtyInput)
                et.setSelection(et.text?.length ?: 0)
            }
            qtyWatcher =
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                    override fun afterTextChanged(s: Editable?) {
                        onQtyChanged(item.productId, s?.toString().orEmpty())
                    }
                }
            et.addTextChangedListener(qtyWatcher)
        }
    }
}
