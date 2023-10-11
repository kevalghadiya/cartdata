package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.databinding.AdapterCartDetailsRowFileBinding
import com.bistrocart.main_module.model.res.OrderDetailsRes
import com.bumptech.glide.Glide

class OrderDetailsCartListAdapter(
    private var myCartList: ArrayList<OrderDetailsRes.Data.OrderItem>,
    private  var cart: Boolean,
) : RecyclerView.Adapter<OrderDetailsCartListAdapter.MyCartListViewHolder>() {

    inner class MyCartListViewHolder(val binding: AdapterCartDetailsRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartListViewHolder {

        val binding = AdapterCartDetailsRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyCartListViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyCartListViewHolder, position: Int) {
        with(holder) {
            with(myCartList[position]) {
                binding.txtproductName.text = this.product_name
               // binding.tvMoney.text = "₹"+this.price
                binding.txtQty.text = this.quantity
                binding.tvMoney.text ="₹"+this.price +" / "+binding.txtQty.text.toString()+" Qty."

                if (cart){
                    binding.llPlusMinus.visibility =View.VISIBLE
                }else{
                    binding.llPlusMinus.visibility =View.GONE
                }
              //  binding.imgProduct.setImageResource(this.image!!)

                if (!this.image.isNullOrEmpty()) {
                    Glide.with(itemView)
                        .load(this.image)
                        .into(binding.imgProduct)
                } else {
                    // Handle the case when the products_images list is empty
                    // You can display a placeholder image or handle it according to your requirements
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return myCartList.size
    }
}