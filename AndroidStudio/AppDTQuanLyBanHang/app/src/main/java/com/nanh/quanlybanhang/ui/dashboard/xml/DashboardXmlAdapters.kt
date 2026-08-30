package com.nanh.quanlybanhang.ui.dashboard.xml

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.databinding.ItemDashboardChartBarBinding
import com.nanh.quanlybanhang.databinding.ItemDashboardRecentOrderBinding
import com.nanh.quanlybanhang.databinding.ItemDashboardStatBinding
import com.nanh.quanlybanhang.ui.dashboard.MonthBarUi
import com.nanh.quanlybanhang.ui.dashboard.RecentOrderUi
import com.nanh.quanlybanhang.ui.dashboard.StatCardUi
import com.nanh.quanlybanhang.util.CategoryDrawableRes

class StatCardsAdapter : ListAdapter<StatCardUi, StatCardsAdapter.Vh>(
    object : DiffUtil.ItemCallback<StatCardUi>() {
        override fun areItemsTheSame(a: StatCardUi, b: StatCardUi): Boolean = a.title == b.title
        override fun areContentsTheSame(a: StatCardUi, b: StatCardUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDashboardStatBinding.inflate(inflater, parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemDashboardStatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatCardUi) {
            binding.textStatTitle.setText(item.title)
            binding.textStatValue.text = item.value
            binding.textStatTrend.text = item.trendText
            val c = ContextCompat.getColor(
                binding.root.context,
                if (item.trendPositive) R.color.trend_positive else R.color.trend_negative,
            )
            binding.textStatTrend.setTextColor(c)
        }
    }
}

class ChartBarsAdapter : RecyclerView.Adapter<ChartBarsAdapter.Vh>() {

    private var items: List<MonthBarUi> = emptyList()
    private var maxAmount: Float = 1f

    fun submit(newItems: List<MonthBarUi>) {
        items = newItems
        maxAmount = newItems.maxOfOrNull { it.amount }?.takeIf { it > 0f } ?: 1f
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDashboardChartBarBinding.inflate(inflater, parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(items[position], position, maxAmount)
    }

    class Vh(private val binding: ItemDashboardChartBarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MonthBarUi, position: Int, maxAmount: Float) {
            binding.textMonthLabel.text = item.label
            binding.textMonthValue.text = item.valueLabel
            val pct = ((item.amount / maxAmount) * 100f).toInt().coerceIn(0, 100)
            binding.barRevenue.progress = pct
            val ctx = binding.root.context
            val barColor = chartBarColor(ctx, position)
            binding.barRevenue.progressTintList = ColorStateList.valueOf(barColor)
            binding.barRevenue.progressBackgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.navy_elevated))
        }
    }
}

private fun chartBarColor(context: Context, position: Int): Int {
    val resId = when (position) {
        1 -> R.color.chart_blue
        2 -> R.color.chart_teal
        else -> R.color.gold_accent
    }
    return ContextCompat.getColor(context, resId)
}

class RecentOrdersAdapter : ListAdapter<RecentOrderUi, RecentOrdersAdapter.Vh>(
    object : DiffUtil.ItemCallback<RecentOrderUi>() {
        override fun areItemsTheSame(a: RecentOrderUi, b: RecentOrderUi): Boolean =
            a.title == b.title && a.time == b.time && a.customer == b.customer &&
                a.price == b.price && a.completed == b.completed && a.categoryId == b.categoryId

        override fun areContentsTheSame(a: RecentOrderUi, b: RecentOrderUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDashboardRecentOrderBinding.inflate(inflater, parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemDashboardRecentOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecentOrderUi) {
            binding.textOrderTitle.text = item.title
            binding.textOrderTime.text = item.time
            binding.textOrderCustomer.text = item.customer
            binding.textOrderPrice.text = item.price
            binding.imageCategory.setImageResource(CategoryDrawableRes.forCategoryId(item.categoryId))
            val ctx = binding.root.context
            if (item.completed) {
                binding.textOrderStatus.setText(R.string.status_completed)
                binding.textOrderStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_completed))
                binding.textOrderStatus.setBackgroundResource(R.drawable.bg_chip_completed)
            } else {
                binding.textOrderStatus.setText(R.string.status_pending)
                binding.textOrderStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_pending))
                binding.textOrderStatus.setBackgroundResource(R.drawable.bg_chip_pending)
            }
        }
    }
}
