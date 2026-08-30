package com.nanh.quanlybanhang.ui.customers.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanh.quanlybanhang.databinding.ItemCustomerManageRowBinding
import com.nanh.quanlybanhang.ui.customers.CustomerManageUi

class CustomersListAdapter(
    private val onEdit: (CustomerManageUi) -> Unit,
    private val onDelete: (CustomerManageUi) -> Unit,
) : ListAdapter<CustomerManageUi, CustomersListAdapter.Vh>(
    object : DiffUtil.ItemCallback<CustomerManageUi>() {
        override fun areItemsTheSame(a: CustomerManageUi, b: CustomerManageUi): Boolean = a.id == b.id
        override fun areContentsTheSame(a: CustomerManageUi, b: CustomerManageUi): Boolean = a == b
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemCustomerManageRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(
        private val binding: ItemCustomerManageRowBinding,
        private val onEdit: (CustomerManageUi) -> Unit,
        private val onDelete: (CustomerManageUi) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CustomerManageUi) {
            binding.textCustomerName.text = item.name
            binding.textCustomerPhone.text = item.phoneLine
            binding.buttonEditCustomer.setOnClickListener { onEdit(item) }
            binding.buttonDeleteCustomer.setOnClickListener { onDelete(item) }
        }
    }
}
