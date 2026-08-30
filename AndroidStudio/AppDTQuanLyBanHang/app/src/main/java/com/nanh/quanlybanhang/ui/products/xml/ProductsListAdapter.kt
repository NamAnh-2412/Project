package com.nanh.quanlybanhang.ui.products.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemProductRowBinding
import com.nanh.quanlybanhang.ui.products.ProductListUi
import com.nanh.quanlybanhang.util.setProductThumbnail

class ProductsListAdapter(
    private val listener: Listener,
) : ListAdapter<ProductListUi, ProductsListAdapter.Vh>(
    object : DiffUtil.ItemCallback<ProductListUi>() {
        override fun areItemsTheSame(a: ProductListUi, b: ProductListUi): Boolean = a.id == b.id
        override fun areContentsTheSame(a: ProductListUi, b: ProductListUi): Boolean = a == b
    },
) {

    interface Listener {
        fun onEditProduct(item: ProductListUi)
        fun onDeleteProduct(item: ProductListUi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductRowBinding.inflate(inflater, parent, false)
        return Vh(binding, listener)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemProductRowBinding,
        private val listener: Listener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductListUi) {
            binding.textProductName.text = item.name
            binding.textProductCategory.text = item.categoryName
            binding.textProductPrice.text = item.priceLabel
            binding.imageCategory.setProductThumbnail(item.imageLocalPath, item.categoryId)
            binding.buttonEditProduct.setOnClickListener { listener.onEditProduct(item) }
            binding.buttonDeleteProduct.setOnClickListener { listener.onDeleteProduct(item) }
        }
    }
}
