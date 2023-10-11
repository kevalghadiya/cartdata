package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterTimeSlotBinding
import com.bistrocart.main_module.model.res.TimeSloteRes

class TimeSlotAdapter(
    private val itemList: List<TimeSloteRes.Data>,
    private val itemClickCallback: ItemClickCallback
) :
    RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {
    private var lastClickedPosition = -1

    interface ItemClickCallback {
        fun onItemClick(item: TimeSloteRes.Data, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterTimeSlotBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: AdapterTimeSlotBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimeSloteRes.Data) {
        //    binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1) }
            binding.tvText.text = item.slot_msg
            binding.tvDate.text =item.delivery_date+"("+item.start_time+"-"+item.end_time+")"

            if (adapterPosition == lastClickedPosition) {
                binding.llMain.setBackgroundResource(R.color.yellow)
            } else {
                binding.llMain.setBackgroundResource(R.color.white)
            }

            binding.llMain.setOnClickListener(View.OnClickListener {
                lastClickedPosition = adapterPosition
                notifyDataSetChanged()
                itemClickCallback.onItemClick(item, 1)
            })
        }

    }


}
