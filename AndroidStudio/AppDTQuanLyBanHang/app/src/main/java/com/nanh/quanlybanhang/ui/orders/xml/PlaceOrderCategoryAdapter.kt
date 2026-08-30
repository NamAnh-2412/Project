package com.nanh.quanlybanhang.ui.orders.xml

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemPlaceOrderCategoryBinding
import com.nanh.quanlybanhang.ui.orders.PlaceOrderCategoryUi
import java.util.Locale

class PlaceOrderCategoryAdapter(
    private val onPick: (PlaceOrderCategoryUi) -> Unit,
) : ListAdapter<PlaceOrderCategoryUi, PlaceOrderCategoryAdapter.Vh>(DIFF) {

    var selectedIndex: Int = 0
        private set

    fun setSelectedIndex(index: Int) {
        val old = selectedIndex
        selectedIndex = index.coerceAtLeast(0)
        notifyItemChanged(old)
        notifyItemChanged(selectedIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaceOrderCategoryBinding.inflate(inflater, parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position), position == selectedIndex)
    }

    inner class Vh(
        private val binding: ItemPlaceOrderCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    setSelectedIndex(pos)
                    onPick(item)
                }
            }
        }

        fun bind(item: PlaceOrderCategoryUi, selected: Boolean) {
            binding.textCategory.text = item.label.uppercase(Locale.getDefault())
            val ctx = binding.root.context
            if (selected) {
                binding.root.setBackgroundResource(R.drawable.bg_pos_category_selected)
                binding.textCategory.setTextColor(ContextCompat.getColor(ctx, R.color.white))
                binding.textCategory.typeface = Typeface.DEFAULT_BOLD
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_pos_category_idle)
                binding.textCategory.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
                binding.textCategory.typeface = Typeface.DEFAULT
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PlaceOrderCategoryUi>() {
            override fun areItemsTheSame(a: PlaceOrderCategoryUi, b: PlaceOrderCategoryUi) = a.id == b.id
            override fun areContentsTheSame(a: PlaceOrderCategoryUi, b: PlaceOrderCategoryUi) = a == b
        }
    }
}
