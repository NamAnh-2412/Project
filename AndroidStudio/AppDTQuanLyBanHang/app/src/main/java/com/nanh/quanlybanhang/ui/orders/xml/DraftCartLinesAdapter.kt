package com.nanh.quanlybanhang.ui.orders.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemDraftCartLineBinding
import com.nanh.quanlybanhang.ui.orders.CartLineUi

class DraftCartLinesAdapter(
    private val onLineContentClick: (CartLineUi) -> Unit,
    private val onRemoveLine: (CartLineUi) -> Unit,
    private val onDecreaseQty: (CartLineUi) -> Unit,
    private val onIncreaseQty: (CartLineUi) -> Unit,
) : ListAdapter<CartLineUi, DraftCartLinesAdapter.Vh>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemDraftCartLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, onLineContentClick, onRemoveLine, onDecreaseQty, onIncreaseQty)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemDraftCartLineBinding,
        private val onLineContentClick: (CartLineUi) -> Unit,
        private val onRemoveLine: (CartLineUi) -> Unit,
        private val onDecreaseQty: (CartLineUi) -> Unit,
        private val onIncreaseQty: (CartLineUi) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartLineUi) {
            binding.layoutLineContent.setOnClickListener { onLineContentClick(item) }
            binding.buttonRemoveLine.setOnClickListener { onRemoveLine(item) }
            binding.buttonQtyMinus.setOnClickListener { onDecreaseQty(item) }
            binding.buttonQtyPlus.setOnClickListener { onIncreaseQty(item) }

            binding.textLineName.text = item.name
            binding.textQtyValue.text = item.quantity.toString()
            binding.textLineTotal.text = item.lineTotalLabel

            val sum = item.selectionSummary
            val ctx = binding.root.context
            binding.textLineOptions.isVisible = !sum.isNullOrBlank()
            if (!sum.isNullOrBlank()) {
                binding.textLineOptions.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
                binding.textLineOptions.text = ctx.getString(R.string.place_order_line_requirements_format, sum)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CartLineUi>() {
            override fun areItemsTheSame(a: CartLineUi, b: CartLineUi) =
                a.cartLineKey == b.cartLineKey

            override fun areContentsTheSame(a: CartLineUi, b: CartLineUi) = a == b
        }
    }
}
