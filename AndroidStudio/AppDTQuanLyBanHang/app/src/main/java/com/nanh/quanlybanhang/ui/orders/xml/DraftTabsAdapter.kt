package com.nanh.quanlybanhang.ui.orders.xml

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemDraftTabBinding
import com.nanh.quanlybanhang.ui.orders.DraftTabUi

class DraftTabsAdapter(
    private val onSelect: (Long) -> Unit,
    private val onClose: (Long) -> Unit,
) : ListAdapter<DraftTabUi, DraftTabsAdapter.Vh>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemDraftTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Vh(
        private val binding: ItemDraftTabBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onSelect(getItem(pos).id)
            }
            binding.buttonCloseTab.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onClose(getItem(pos).id)
            }
        }

        fun bind(item: DraftTabUi) {
            binding.textTabTitle.text = item.label
            binding.buttonCloseTab.isVisible = item.showClose
            val ctx = binding.root.context

            binding.buttonCloseTab.imageTintList =
                ContextCompat.getColorStateList(ctx, R.color.text_secondary)
            if (item.selected) {
                binding.root.setBackgroundResource(R.drawable.bg_draft_tab_selected)
                binding.textTabTitle.setTextColor(ContextCompat.getColor(ctx, R.color.gold_accent))
                binding.textTabTitle.typeface = Typeface.DEFAULT_BOLD
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_draft_tab_idle)
                binding.textTabTitle.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
                binding.textTabTitle.typeface = Typeface.DEFAULT
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DraftTabUi>() {
            override fun areItemsTheSame(a: DraftTabUi, b: DraftTabUi) = a.id == b.id
            override fun areContentsTheSame(a: DraftTabUi, b: DraftTabUi) = a == b
        }
    }
}
