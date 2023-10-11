package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterMyOrderListBinding
import com.bistrocart.databinding.AdapterTimeSlotBinding
import com.bistrocart.main_module.model.res.OrderListRes
import com.bistrocart.main_module.model.res.ProductListRes
import com.bistrocart.main_module.model.res.TimeSloteRes
import com.bistrocart.utils.DateTimeHelper
import java.time.format.DateTimeFormatter

class OrderListAdapter  (
    private val itemClickCallback: ItemClickCallback
) :
    RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {
    private var itemList = ArrayList<OrderListRes.Data.Data>()

    interface ItemClickCallback {
        fun onItemClick(item: OrderListRes.Data.Data, type: Int)
    }

    // Add a method to clear the existing data in the adapter
    fun clearData() {
        itemList.clear()
        notifyDataSetChanged()
    }

    fun addData(newItems: List<OrderListRes.Data.Data>) {
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding =AdapterMyOrderListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: AdapterMyOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderListRes.Data.Data) {
            binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1) }
            binding.tvOrderId.text = "Order ID:"+item.id
            binding.tvDate.text =item.created_at
            binding.tvStatus.text =item.status
          //  binding.tvAmount.text =item.amount
           //  binding.tvDate.text =DateTimeHelper.convertFormat(item.created_at, "yyyy-MM-dd HH:mm:ss", "dd MMM, yyyy hh:mm a")

        }
    }
}
