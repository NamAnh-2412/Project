package com.nanh.quanlybanhang.ui.reports.xml

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemReportHourBarBinding
import com.nanh.quanlybanhang.databinding.ItemReportPaymentRowBinding
import com.nanh.quanlybanhang.ui.reports.HourBarUi
import com.nanh.quanlybanhang.ui.reports.PaymentMethodRowUi

class ReportsHourlyAdapter : RecyclerView.Adapter<ReportsHourlyAdapter.Vh>() {

    private var items: List<HourBarUi> = emptyList()
    private var maxAmount: Double = 1.0

    fun submit(list: List<HourBarUi>, maxForBar: Double) {
        items = list
        maxAmount = maxForBar.takeIf { it > 0.0 } ?: 1.0
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemReportHourBarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(items[position], position, maxAmount)
    }

    class Vh(private val binding: ItemReportHourBarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HourBarUi, position: Int, maxAmount: Double) {
            binding.textHourLabel.text = item.label
            binding.textHourValue.text = item.valueLabel
            val pct = ((item.amount / maxAmount) * 100.0).toInt().coerceIn(0, 100)
            binding.barRevenue.progress = pct
            val ctx = binding.root.context
            val barColor = hourlyBarColor(ctx, position)
            binding.barRevenue.progressTintList = ColorStateList.valueOf(barColor)
            binding.barRevenue.progressBackgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.navy_elevated))
        }
    }
}

private fun hourlyBarColor(context: Context, position: Int): Int {
    val resId = when (position % 3) {
        1 -> R.color.chart_blue
        2 -> R.color.chart_teal
        else -> R.color.gold_accent
    }
    return ContextCompat.getColor(context, resId)
}

class ReportsPaymentAdapter : ListAdapter<PaymentMethodRowUi, ReportsPaymentAdapter.Vh>(
    object : DiffUtil.ItemCallback<PaymentMethodRowUi>() {
        override fun areItemsTheSame(a: PaymentMethodRowUi, b: PaymentMethodRowUi): Boolean = a.title == b.title
        override fun areContentsTheSame(a: PaymentMethodRowUi, b: PaymentMethodRowUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemReportPaymentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemReportPaymentRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaymentMethodRowUi) {
            binding.textPaymentTitle.text = item.title
            binding.textPaymentOrders.text = item.ordersLabel
            binding.textPaymentRevenue.text = item.revenueLabel
        }
    }
}
