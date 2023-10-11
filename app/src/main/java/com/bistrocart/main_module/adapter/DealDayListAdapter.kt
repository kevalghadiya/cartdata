package com.bistrocart.main_module.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterDealDayRowFileBinding
import com.bistrocart.main_module.model.res.ProductListRes
import com.bumptech.glide.Glide
import okhttp3.internal.commonEmptyHeaders

class DealDayListAdapter(
    var imgPath: String,
    private var dealDayList: ArrayList<ProductListRes.Data.Data>,
    private val itemClickCallback: ItemClickCallback
) : RecyclerView.Adapter<DealDayListAdapter.DealDayViewHolder>() {


    inner class DealDayViewHolder(val binding: AdapterDealDayRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface ItemClickCallback {
        fun onItemClick(item: ProductListRes.Data.Data, type: Int, position: Int)
    }

    fun updateItemValue(position: Int, newValue: Int) {
        if (dealDayList[position].cart_items.isNullOrEmpty()){
            var qty :ProductListRes.Data.Data.CartItem = ProductListRes.Data.Data.CartItem("",0,0,"","","","1","","","")
            dealDayList[position].cart_items.add(qty)
        }else {
            dealDayList[position].cart_items.get(0).quantity = newValue.toString()
        }
        Log.e("count", "updateItemValue1--->>>: " + position + "new valus is-->>" + newValue)
        Log.e("count", "updateItemValue2--->>>: " + position + "new valus is-->>" + newValue)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealDayViewHolder {

        val binding = AdapterDealDayRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DealDayViewHolder(binding)

    }

    override fun onBindViewHolder(holder: DealDayViewHolder, position: Int) {
        with(holder) {
            with(dealDayList[position]) {
                binding.txtProductName.text = this.pro_name
       //         binding.tvMoney.text = "₹"+this.price
//                binding.tvMoney.text ="₹"+this.price +" / "+binding.tvMoneyCount.text.toString()+" Qty."

                //  binding.imgProduct.setImageResource(this.productImage!!)
                binding.llMain.setOnClickListener { itemClickCallback.onItemClick(dealDayList[position], 1,position) }
                binding.tvMoney.setOnClickListener {
                        itemClickCallback.onItemClick(dealDayList[position], 2, position)
                }

                binding.ivMinus.setOnClickListener {
                    if (dealDayList.size !=0) {
                        itemClickCallback.onItemClick(dealDayList[position], 3, position)
                    }
                }

                binding.ivPlus.setOnClickListener {
                    if (dealDayList.size !=0) {
                        itemClickCallback.onItemClick(dealDayList[position], 4, position)
                    }
                }

             /*   if (this.cart_items!=null && this.cart_items.isNotEmpty()) {
                    binding.tvMoneyCount.text =this.cart_items.get(0).quantity
                }else{
                    binding.tvMoneyCount.text ="0"
                }
                */

                //var qty = this.cart_items.get(0).quantity
                var flag:Boolean =false
                var totalPrice = 0
                var qtys: String?="0"
                if (this.cart_items!=null && this.cart_items.isNotEmpty()) {
                    binding.tvMoneyCount.text =this.cart_items.get(0).quantity
                    qtys =this.cart_items.get(0).quantity
                }else{
                    qtys =binding.tvMoneyCount.text.toString()
                 //   binding.tvMoneyCount.text ="0"
                    //totalPrice =dealDayList[position].products_prices[0].price_per_unit!!.toInt()
                    //totalPrice =price.toInt()
                }

              /*  dealDayList[position].products_prices.asReversed().forEach{
                    Log.e("TAG", "onBindViewHolder: "+qtys)
                    if (qtys!! >= it.unit!! &&!flag){
                        Log.e("TAG", "qtys: "+qtys)
                        Log.e("TAG", "qtys: "+it.unit!!)
                        Log.e("TAG", "qtys: "+it.price_per_unit!!)
                        flag =true
                        totalPrice = qtys.toInt() * it.price_per_unit!!.toInt()
                    }
                }*/

                Log.e("TAG", "total_is:--->"+totalPrice)
                Log.e("TAG", "qty: "+qtys)

                totalPrice =dealDayList[position].products_prices[dealDayList[position].products_prices.size-1].price_per_unit!!.toInt()

                dealDayList[position].products_prices.forEach { product ->
                    if (qtys!!.toInt() <=product.unit!!.toInt() && !flag) {
                        flag = true
                        totalPrice = product.price_per_unit!!.toInt()
                        Log.e("TAG", "total_is:--->>"+totalPrice )
                    }
                }


                /*dealDayList[position].products_prices.asReversed().forEach{
                    if (qtys!! >= it.unit!! &&!flag){
                        flag =true
                        totalPrice = qtys!!.toInt() * it.price_per_unit!!.toInt()
                    }
                }*/

                binding.tvMoney.text ="₹"+totalPrice +" / "+"1 Qty."

                if(this.instock.equals("0")){
                    binding.ivPlus.isClickable =false
                    binding.ivMinus.setBackgroundResource(R.drawable.ic_minius_dark);
                    binding.ivPlus.setBackgroundResource(R.drawable.ic_plus_dark);
                    binding.ivOutOfStock.visibility = View.VISIBLE
                }else{
                    binding.ivPlus.isClickable =true
                    binding.ivOutOfStock.visibility = View.GONE
                    binding.ivMinus.setBackgroundResource(R.drawable.ic_minus);
                    binding.ivPlus.setBackgroundResource(R.drawable.ic_plus);
                }

               // binding.tvMoney.text ="₹"+this.price +" / "+"1"+" Qty."
                if (this.products_images.isNotEmpty()) {
                    Glide.with(itemView)
                        .load("$imgPath${this.products_images[0].pro_image}")
                        .into(binding.imgProduct)
                } else {
                    // Handle the case when the products_images list is empty
                    // You can display a placeholder image or handle it according to your requirements
                }
            }
        }
    }



    override fun getItemCount(): Int {
        return dealDayList.size
    }
}