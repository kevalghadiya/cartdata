package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterMyOrderListBinding
import com.bistrocart.databinding.AdapterMyOrderStatusBinding
import com.bistrocart.databinding.AdapterTimeSlotBinding
import com.bistrocart.main_module.model.res.OrderDetailsRes
import com.bistrocart.main_module.model.res.OrderListRes
import com.bistrocart.main_module.model.res.TimeSloteRes
import com.bistrocart.utils.DateTimeHelper

class MyOrderStatusAdapter  (
    private val itemList: ArrayList<OrderDetailsRes.Data.OrderStatu>,
//    private val itemClickCallback: ItemClickCallback
) :
    RecyclerView.Adapter<MyOrderStatusAdapter.ViewHolder>() {

    interface ItemClickCallback {
        fun onItemClick(item: OrderDetailsRes.Data.OrderStatu, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding =AdapterMyOrderStatusBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: AdapterMyOrderStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderDetailsRes.Data.OrderStatu) {
//            binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1) }
            binding.tvOrderHeader.text = item.status_text
          //  binding.tvOrderTime.text = item.Datetime
            binding.tvOrderTime.text = DateTimeHelper.convertFormat(item.Datetime, "yyyy-MM-dd HH:mm:ss", "dd MMM, yyyy hh:mm a")

            if (item.Datetime.isNullOrEmpty()){
                binding.ivImageView.setImageResource(R.drawable.ic_order_unsuccess)
            }else{
                binding.ivImageView.setImageResource(R.drawable.ic_order_status_success)
            }
        }
    }
}
