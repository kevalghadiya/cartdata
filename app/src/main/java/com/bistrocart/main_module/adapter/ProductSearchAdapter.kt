package com.bistrocart.main_module.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterProductSubCatogaryDetailsBinding
import com.bistrocart.main_module.model.res.ProductListRes
import com.bumptech.glide.Glide

class ProductSearchAdapter(
    private var imgPath: String,
    private val itemClickCallback: ItemClickCallback
) : RecyclerView.Adapter<ProductSearchAdapter.ViewHolder>() {

    private val itemList: ArrayList<ProductListRes.Data.Data> =ArrayList()
    interface ItemClickCallback {
        fun onItemClick(item: ProductListRes.Data.Data, type: Int,quantity:Int)
    }

    fun clearData() {
        itemList.clear()
        notifyDataSetChanged()
    }

      @SuppressLint("NotifyDataSetChanged")
      fun addData(newItems: List<ProductListRes.Data.Data>, fileurl: String?) {
          itemList.addAll(newItems)
          if (fileurl != null) {
              imgPath = fileurl
          }
          notifyDataSetChanged()
      }

   fun updateItemValue(position: Int, newValue: Int) {
       Log.e("count", "updateItemValue-->: "+newValue)
      // itemList[position].cart_items
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
                binding.tvMoneyCount.text =item.cart_items.get(0).quantity.toString()
                qtys =item.cart_items.get(0).quantity
            }else{
                qtys ="0"
                binding.tvMoneyCount.text ="0"
            }

            totalPrice =item.products_prices.get(item.products_prices.size-1).price_per_unit!!.toInt()
            item.products_prices.forEach { product ->
                if (qtys!!.toInt() <=product.unit!!.toInt() && !flag) {
                    flag = true
                    totalPrice = product.price_per_unit!!.toInt()
                    Log.e("TAG", "total_is:--->>"+totalPrice )
                }
            }

            binding.tvMoney.text ="₹"+totalPrice.toString()

            binding.txtCategoryName.text =item.pro_name

            if(item.instock.equals("0")){
                binding.ivPlus.isClickable =false
                binding.ivMinus.isClickable =false
                binding.ivMinus.setBackgroundResource(R.drawable.ic_minius_dark);
                binding.ivPlus.setBackgroundResource(R.drawable.ic_plus_dark);
                binding.ivOutOfStock.visibility = View.VISIBLE
            }else{
                binding.ivOutOfStock.visibility = View.GONE
                binding.ivPlus.isClickable =true
                binding.ivMinus.isClickable =true
                binding.ivMinus.setBackgroundResource(R.drawable.ic_minus);
                binding.ivPlus.setBackgroundResource(R.drawable.ic_plus);
            }

            if (item.products_images.isNotEmpty()) {
                Glide.with(itemView)
                    .load("$imgPath${item.products_images[0].pro_image}")
                    .into(binding.imgCategory)
            } else {
                // Handle the case when the products_images list is empty
                // You can display a placeholder image or handle it according to your requirements
            }

        }

    }

}
