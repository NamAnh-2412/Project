package com.nanh.quanlybanhang.ui.orders.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemCustomerPickRowBinding
import com.nanh.quanlybanhang.ui.orders.CustomerPickUi

class CustomerPickAdapter(
    private val onPick: (CustomerPickUi) -> Unit,
) : RecyclerView.Adapter<CustomerPickAdapter.VH>() {

    private val items = mutableListOf<CustomerPickUi>()

    fun submitList(list: List<CustomerPickUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCustomerPickRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(
        private val binding: ItemCustomerPickRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onPick(items[pos])
            }
        }

        fun bind(item: CustomerPickUi) {
            binding.textName.text = item.name
            val p = item.phone?.trim().orEmpty()
            binding.textPhone.isVisible = p.isNotEmpty()
            binding.textPhone.text = p
        }
    }
}
