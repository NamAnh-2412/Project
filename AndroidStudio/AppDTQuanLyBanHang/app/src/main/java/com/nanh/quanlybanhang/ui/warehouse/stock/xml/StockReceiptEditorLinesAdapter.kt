package com.nanh.quanlybanhang.ui.warehouse.stock.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemStockReceiptEditorLineBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.ReceiptLineDraft

class StockReceiptEditorLinesAdapter(
    private val onRemove: (Int) -> Unit,
) : ListAdapter<ReceiptLineDraft, StockReceiptEditorLinesAdapter.Vh>(
    object : DiffUtil.ItemCallback<ReceiptLineDraft>() {
        override fun areItemsTheSame(a: ReceiptLineDraft, b: ReceiptLineDraft): Boolean =
            a.productId == b.productId

        override fun areContentsTheSame(a: ReceiptLineDraft, b: ReceiptLineDraft): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemStockReceiptEditorLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, onRemove)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position), position)
    }

    class Vh(
        private val binding: ItemStockReceiptEditorLineBinding,
        private val onRemove: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReceiptLineDraft, position: Int) {
            binding.textLineProduct.text = item.productName
            binding.textLineQty.text = "× ${item.quantity}"
            binding.buttonRemoveLine.setOnClickListener { onRemove(position) }
        }
    }
}
