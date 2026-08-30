package com.nanh.quanlybanhang.ui.warehouse.stock.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemStockCountReportRowBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.StockCountReportListRow

class StockCountReportsAdapter : ListAdapter<StockCountReportListRow, StockCountReportsAdapter.Vh>(
    object : DiffUtil.ItemCallback<StockCountReportListRow>() {
        override fun areItemsTheSame(a: StockCountReportListRow, b: StockCountReportListRow): Boolean = a.id == b.id
        override fun areContentsTheSame(a: StockCountReportListRow, b: StockCountReportListRow): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemStockCountReportRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemStockCountReportRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StockCountReportListRow) {
            val ctx = binding.root.context
            binding.textReportTitle.text =
                ctx.getString(R.string.stock_count_report_row_title, item.id, item.dateLabel)
        }
    }
}
