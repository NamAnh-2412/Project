package com.nanh.quanlybanhang.ui.warehouse.stock.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemStockProductRowBinding
import com.nanh.quanlybanhang.ui.warehouse.stock.StockProductRowUi

class StockProductsAdapter(
    private val onStockIn: (StockProductRowUi) -> Unit,
    private val onEdit: (StockProductRowUi) -> Unit,
    private val onDelete: (StockProductRowUi) -> Unit,
) : ListAdapter<StockProductRowUi, StockProductsAdapter.Vh>(
    object : DiffUtil.ItemCallback<StockProductRowUi>() {
        override fun areItemsTheSame(a: StockProductRowUi, b: StockProductRowUi): Boolean = a.id == b.id
        override fun areContentsTheSame(a: StockProductRowUi, b: StockProductRowUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemStockProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, onStockIn, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemStockProductRowBinding,
        private val onStockIn: (StockProductRowUi) -> Unit,
        private val onEdit: (StockProductRowUi) -> Unit,
        private val onDelete: (StockProductRowUi) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StockProductRowUi) {
            val ctx = binding.root.context
            binding.textProductName.text = item.name
            binding.textProductCategory.text = item.categoryName
            binding.textStockQty.text = ctx.getString(R.string.stock_qty_fmt, item.stockQuantity)
            binding.buttonStockIn.setOnClickListener { onStockIn(item) }
            binding.buttonEditProduct.setOnClickListener { onEdit(item) }
            binding.buttonDeleteProduct.setOnClickListener { onDelete(item) }
        }
    }
}
