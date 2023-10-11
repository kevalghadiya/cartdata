package com.bistrocart.main_module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterProductSubCatogaryDetailsBinding
import com.bistrocart.main_module.model.res.ProductListRes
import com.bumptech.glide.Glide

class ProductCatogarySubDetailsAdapter(
    //private val itemList: ArrayList<ProductListRes.Data.Data>,
    private var imgPath: String,
    private val itemClickCallback: ItemClickCallback
) : RecyclerView.Adapter<ProductCatogarySubDetailsAdapter.ViewHolder>() {
    private lateinit var imagePath:String

    private val itemList: ArrayList<ProductListRes.Data.Data> =ArrayList()
    interface ItemClickCallback {
        fun onItemClick(item: ProductListRes.Data.Data, type: Int,quantity:Int)
    }

    fun clearData() {
        itemList.clear()
        imagePath =""
        notifyDataSetChanged()
    }

      fun addData(newItems: List<ProductListRes.Data.Data>, imgPath2: String?) {
          imagePath = imgPath2.toString()
          itemList.addAll(newItems)
         /* if (imgPath2 != null) {
              imgPath= imgPath2
          }*/
          notifyDataSetChanged()
      }

   fun updateItemValue(position: Int, newValue: Int) {
       Log.e("count", "updateItemValue-->: "+newValue)
       if (!itemList[position].cart_items.isNotEmpty()){
           var cartItem : ProductListRes.Data.Data.CartItem = ProductListRes.Data.Data.CartItem("",0,0,"","","","1","","","")
           itemList[position].cart_items.add(cartItem)
       }else{
           itemList[position].cart_items.get(0).quantity = newValue.toString()
       }
      Log.e("count", "updateItemValue1--->>>: "+position+"new valus is-->>"+newValue)
      notifyItemChanged(position)
      Log.e("count", "updateItemValue2--->>>: "+position+"new valus is-->>"+newValue)
  }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterProductSubCatogaryDetailsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList!![position]
        holder.bind(item,position)
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }

    inner class ViewHolder(private val binding: AdapterProductSubCatogaryDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductListRes.Data.Data, position: Int) {
            binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1,0) }
            binding.tvMoney.setOnClickListener { itemClickCallback.onItemClick(item, 2 ,0)}

            binding.ivMinus.setOnClickListener {
                itemClickCallback.onItemClick(item, 3,position)
                Log.e("count", "minus--->>>: "+position)
            }

            binding.ivPlus.setOnClickListener { itemClickCallback.onItemClick(item, 4,position)
                Log.e("count", "plus--->>>: "+position)
            }

            var flag:Boolean =false
            var totalPrice = 0
            var qtys: String?="0"
            if (item.cart_items!=null && item.cart_items.isNotEmpty()) {
                binding.tvMoneyCount.text =item.cart_items.get(0).quantity
                qtys =item.cart_items.get(0).quantity
            }else{
                qtys ="0"
                binding.tvMoneyCount.text ="0"
            }

           totalPrice =item.products_prices.get(item.products_prices.size-1).price_per_unit!!.toInt()
            Log.e("TAG", "products_prices_1: "+totalPrice)
            item.products_prices.forEach { product ->
                Log.e("TAG", "products_prices_2: "+product)
                Log.e("TAG", "products_prices_3_qtys: "+qtys)
                Log.e("TAG", "products_prices_3.1_flag: "+flag)
                Log.e("TAG", "products_prices_3.2: "+product.unit!!)
                if (qtys!!.toInt() <=product.unit.toInt() && !flag) {
                    Log.e("TAG", "products_prices_4_qtys: "+qtys)
                    Log.e("TAG", "products_prices_5: "+flag)
                    Log.e("TAG", "products_prices_6: "+totalPrice)
                    flag = true
                    totalPrice = product.price_per_unit!!.toInt()
                    Log.e("TAG", "total_is:--->>"+totalPrice )
                }
            }
            Log.e("TAG", "products_prices_7: "+totalPrice)
            binding.tvMoney.text ="₹"+totalPrice

            if(item.instock.equals("0")){
                binding.ivPlus.isClickable =false
                binding.ivMinus.setBackgroundResource(R.drawable.ic_minius_dark);
                binding.ivPlus.setBackgroundResource(R.drawable.ic_plus_dark);
                binding.ivOutOfStock.visibility =View.VISIBLE
            }else{
                binding.ivPlus.isClickable =true
                binding.ivOutOfStock.visibility =View.GONE
                binding.ivMinus.setBackgroundResource(R.drawable.ic_minus);
                binding.ivPlus.setBackgroundResource(R.drawable.ic_plus);
            }
          /*  if (item.instock.toInt()<=qtys!!.toInt()){
               // binding.llMain.setAlpha(0.2F);
                binding.ivPlus.isClickable =false
                binding.imgCategory.setBackgroundResource(R.drawable.ic_out_of_stock);

            }else{
                binding.ivPlus.isClickable =true
            }*/


            /*  totalPrice =dealDayList[position].products_prices[dealDayList[position].products_prices.size-1].price_per_unit!!.toInt()

              dealDayList[position].products_prices.forEach { product ->
                  if (qtys!! <=product.unit!! && !flag) {
                      flag = true
                      totalPrice = product.price_per_unit!!.toInt()
                      Log.e("TAG", "total_is:--->>"+totalPrice )
                  }
              }*/

          //  binding.tvMoney.text ="₹"+item.products_prices.get(0).price_per_unit


            binding.txtCategoryName.text =item.pro_name

            if (item.products_images.isNotEmpty()) {
                Glide.with(itemView)
                    .load("$imagePath${item.products_images[0].pro_image}")
                    .into(binding.imgCategory)
            } else {
                // Handle the case when the products_images list is empty
                // You can display a placeholder image or handle it according to your requirements
            }

        }

    }

}
