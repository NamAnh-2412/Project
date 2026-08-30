package com.nanh.quanlybanhang.ui.orders.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemOrderRowBinding
import com.nanh.quanlybanhang.ui.orders.OrderListUi
import com.nanh.quanlybanhang.ui.orders.OrderRowStatus

class OrdersListAdapter : ListAdapter<OrderListUi, OrdersListAdapter.Vh>(
    object : DiffUtil.ItemCallback<OrderListUi>() {
        override fun areItemsTheSame(a: OrderListUi, b: OrderListUi): Boolean = a.id == b.id
        override fun areContentsTheSame(a: OrderListUi, b: OrderListUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOrderRowBinding.inflate(inflater, parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemOrderRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderListUi) {
            binding.textOrderTitle.text = item.title
            binding.textOrderCustomerLine.text = item.customerLine
            binding.textOrderPaymentLine.text = item.paymentLine
            binding.textOrderDateLine.text = item.dateLine
            binding.textOrderPrice.text = item.price
            val ctx = binding.root.context
            when (item.status) {
                OrderRowStatus.COMPLETED -> {
                    binding.textOrderStatus.setText(R.string.status_completed)
                    binding.textOrderStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_completed))
                    binding.textOrderStatus.setBackgroundResource(R.drawable.bg_chip_completed)
                }
                OrderRowStatus.PENDING -> {
                    binding.textOrderStatus.setText(R.string.status_pending)
                    binding.textOrderStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_pending))
                    binding.textOrderStatus.setBackgroundResource(R.drawable.bg_chip_pending)
                }
                OrderRowStatus.CANCELLED -> {
                    binding.textOrderStatus.setText(R.string.status_cancelled)
                    binding.textOrderStatus.setTextColor(ContextCompat.getColor(ctx, R.color.trend_negative))
                    binding.textOrderStatus.setBackgroundResource(R.drawable.bg_chip_cancelled)
                }
            }
        }
    }
}
