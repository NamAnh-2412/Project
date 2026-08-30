package com.nanh.quanlybanhang.ui.orders.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemPlaceOrderProductBinding
import com.nanh.quanlybanhang.util.formatVndGrouped
import com.nanh.quanlybanhang.ui.orders.PlaceOrderProductUi
import com.nanh.quanlybanhang.util.setProductThumbnail

class PlaceOrderProductAdapter(
    private val onProductClick: (PlaceOrderProductUi) -> Unit,
) : ListAdapter<PlaceOrderProductUi, PlaceOrderProductAdapter.Vh>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaceOrderProductBinding.inflate(inflater, parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Vh(
        private val binding: ItemPlaceOrderProductBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onProductClick(getItem(pos))
            }
        }

        fun bind(item: PlaceOrderProductUi) {
            binding.textName.text = item.name
            binding.textPrice.text = "${formatVndGrouped(item.unitPrice)}đ"
            binding.imageThumb.setProductThumbnail(item.imageLocalPath, item.categoryId, maxSidePx = 256)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PlaceOrderProductUi>() {
            override fun areItemsTheSame(a: PlaceOrderProductUi, b: PlaceOrderProductUi) = a.id == b.id
            override fun areContentsTheSame(a: PlaceOrderProductUi, b: PlaceOrderProductUi) = a == b
        }
    }
}
