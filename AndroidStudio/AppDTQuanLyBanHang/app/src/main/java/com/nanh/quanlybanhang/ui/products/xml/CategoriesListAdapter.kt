package com.nanh.quanlybanhang.ui.products.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemCategoryRowBinding
import com.nanh.quanlybanhang.ui.products.CategoryRowUi
import com.nanh.quanlybanhang.util.CategoryDrawableRes

class CategoriesListAdapter(
    private val listener: Listener,
) : ListAdapter<CategoryRowUi, CategoriesListAdapter.Vh>(
    object : DiffUtil.ItemCallback<CategoryRowUi>() {
        override fun areItemsTheSame(a: CategoryRowUi, b: CategoryRowUi) = a.id == b.id
        override fun areContentsTheSame(a: CategoryRowUi, b: CategoryRowUi) = a == b
    },
) {

    interface Listener {
        fun onEditCategory(item: CategoryRowUi)
        fun onDeleteCategory(item: CategoryRowUi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemCategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, listener)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemCategoryRowBinding,
        private val listener: Listener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryRowUi) {
            binding.textCategoryName.text = item.name
            binding.imageCategoryIcon.setImageResource(CategoryDrawableRes.forCategoryId(item.id))
            binding.buttonEditCategory.setOnClickListener { listener.onEditCategory(item) }
            binding.buttonDeleteCategory.setOnClickListener { listener.onDeleteCategory(item) }
        }
    }
}
