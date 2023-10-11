package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterProductPriceBinding
import com.bistrocart.databinding.AdapterProductPriceProductDetailsBinding
import com.bistrocart.main_module.model.res.ProductListRes

class UniPriceAdapterProductDetails(
    private val itemList: List<ProductListRes.Data.Data.ProductsPrice>,
   public  var unitSelect: String,
    private val itemClickCallback: ItemClickCallback
) :
    RecyclerView.Adapter<UniPriceAdapterProductDetails.ViewHolder>() {
    private var lastClickedPosition = -1

    interface ItemClickCallback {
        fun onItemClick(item: ProductListRes.Data.Data.ProductsPrice, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterProductPriceProductDetailsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item,position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: AdapterProductPriceProductDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductListRes.Data.Data.ProductsPrice, position: Int) {
            binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1) }
          //  binding.txtUnits.text = item.unit
            binding.txtUnitsPrice.text = "₹" + item.price_per_unit +"/Per Unit"

            if (position==0){
                binding.txtUnits.text ="1-"+item.unit
            }else{
                binding.txtUnits.text = "${itemList.get(position-1).unit!!.toInt() + 1}-${item.unit}"
            }
            binding.llMain.setOnClickListener(View.OnClickListener {
                lastClickedPosition = adapterPosition
                notifyDataSetChanged()
                itemClickCallback.onItemClick(item, 1)
            })

//            if(item.unit.equals(unitSelect)){
//                lastClickedPosition = adapterPosition
//            }

//            if (adapterPosition == lastClickedPosition) {
//
//                binding.llMain.setBackgroundResource(R.drawable.bg_select)
//
//            } else {
//                binding.llMain.setBackgroundResource(R.drawable.bg_un_select)
//            }
        }

    }

}
