package com.nanh.quanlybanhang.ui.warehouse.stock.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemStockReceiptRowBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.StockReceiptListRow
import com.nanh.quanlybanhang.util.VietnameseDates

class StockReceiptsAdapter : ListAdapter<StockReceiptListRow, StockReceiptsAdapter.Vh>(
    object : DiffUtil.ItemCallback<StockReceiptListRow>() {
        override fun areItemsTheSame(a: StockReceiptListRow, b: StockReceiptListRow): Boolean = a.id == b.id
        override fun areContentsTheSame(a: StockReceiptListRow, b: StockReceiptListRow): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemStockReceiptRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemStockReceiptRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StockReceiptListRow) {
            val ctx = binding.root.context
            binding.textReceiptTitle.text = ctx.getString(R.string.stock_receipt_row_title, item.id, item.supplierName)
            binding.textReceiptSubtitle.text = VietnameseDates.formatOrderDateTime(item.createdAtEpochMillis)
        }
    }
}
