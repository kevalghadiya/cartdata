package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterMyOrderListBinding
import com.bistrocart.databinding.AdapterNotificationBinding
import com.bistrocart.databinding.AdapterTimeSlotBinding
import com.bistrocart.main_module.model.res.NotificationRes
import com.bistrocart.main_module.model.res.OrderListRes
import com.bistrocart.main_module.model.res.TimeSloteRes
import com.bistrocart.utils.DateTimeHelper
import java.time.format.DateTimeFormatter

class NotificationAdapter  (
    private val itemList: ArrayList<NotificationRes.Data>,
    private val itemClickCallback: ItemClickCallback
) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    interface ItemClickCallback {
        fun onItemClick(item: NotificationRes.Data, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding =AdapterNotificationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: AdapterNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationRes.Data) {
            binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1) }

            if (item.type.equals("admin")){
                binding.tvOrderId.text ="bistrokart"
            }else {
                binding.tvOrderId.text = "Order ID:" + item.order_id
            }

            binding.tvMessage.text =item.message
            binding.tvDate.text =item.created_at
        }
    }
}
