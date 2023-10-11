package com.bistrocart.main_module.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterSaveListBinding
import com.bistrocart.main_module.model.res.FavouriteRes
import com.bistrocart.main_module.model.res.ProductListRes
import com.bumptech.glide.Glide

class SavedListAdapter(
    private var savedList: ArrayList<ProductListRes.Data.Data>,
   /* private var imgPath: String,*/
    private val itemClickCallback: ItemClickCallback
) : RecyclerView.Adapter<SavedListAdapter.SavedListViewHolder>() {
    private lateinit var imgPath: String
    inner class SavedListViewHolder(val binding: AdapterSaveListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedListViewHolder {

        val binding = AdapterSaveListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavedListViewHolder(binding)

    }

   /* fun updateItemValued(position: Int, newValue: Int) {
        savedList[position].cart_items.get(0).quantity = newValue.toString()
        Log.e("count", "updateItemValue1--->>>: "+position+"new valus is-->>"+newValue)
        notifyItemChanged(position)
        Log.e("count", "updateItemValue2--->>>: "+position+"new valus is-->>"+newValue)
    }*/

    fun updateItemValue(position: Int, newValue: Int) {
        Log.e("count", "updateItemValue-->: "+newValue)
        // itemList[position].cart_items
        if (!savedList[position].cart_items.isNotEmpty()){
            var cartItem : ProductListRes.Data.Data.CartItem = ProductListRes.Data.Data.CartItem("",0,0,"","","","1","","","")
            savedList[position].cart_items.add(cartItem)
        }else{
            savedList[position].cart_items.get(0).quantity = newValue.toString()
        }
        Log.e("count", "updateItemValue1--->>>: "+position+"new valus is-->>"+newValue)
        notifyItemChanged(position)
        Log.e("count", "updateItemValue2--->>>: "+position+"new valus is-->>"+newValue)
    }

    fun updateFavourite(position: Int){
        Log.e("TAG", "updateFavourite: "+position)
        savedList.removeAt(position)
        notifyDataSetChanged()
    }

    fun updateImagePath(img: String){
        imgPath =img
    }

    override fun onBindViewHolder(holder: SavedListViewHolder, position: Int) {
        with(holder) {
            with(savedList[position]) {
                binding.txtproductName.text = this.pro_name
            //    binding.tvMoney.text = this.price
              //  binding.tvMoney.text ="₹"+this.price +" / "+"1"+" Qty."

                var flag:Boolean =false
                var totalPrice = 0
                var qtys: String?="0"
                if (savedList[position].cart_items!=null && savedList[position].cart_items.isNotEmpty()) {
                    binding.tvMoneyCount.text =savedList[position].cart_items.get(0).quantity
                    qtys =this.cart_items.get(0).quantity
                }else{
                    binding.tvMoneyCount.text ="0"
                    qtys =binding.tvMoneyCount.text.toString()
                }
                binding.ivMinus.setOnClickListener { itemClickCallback.onItemClick(savedList[position], 3,position) }
                binding.ivPlus.setOnClickListener { itemClickCallback.onItemClick(savedList[position], 4,position) }
                binding.tvMoney.setOnClickListener { itemClickCallback.onItemClick(savedList[position], 2,position) }
                binding.CartFavourite.setOnClickListener{itemClickCallback.onItemClick(savedList[position],5,position)}



                Log.e("TAG", "imgPath-->>:$imgPath"+savedList[position].products_images[0].pro_image)
                if (savedList[position].products_images.isNotEmpty()) {
                    Glide.with(itemView)
                        .load(imgPath+savedList[position].products_images[0].pro_image)
                        .into(binding.imgProduct)
                } else {
                    // Handle the case when the products_images list is empty
                    // You can display a placeholder image or handle it according to your requirements
                }

                Log.e("TAG", "total_is:--->"+totalPrice)
                Log.e("TAG", "qty: "+qtys)

                totalPrice =savedList[position].products_prices[savedList[position].products_prices.size-1].price_per_unit!!.toInt()

                savedList[position].products_prices.forEach { product ->
                    if (qtys!!.toInt() <=product.unit!!.toInt() && !flag) {
                        flag = true
                        totalPrice = product.price_per_unit!!.toInt()
                        Log.e("TAG", "total_is:--->>"+totalPrice )
                    }
                }
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

                //     binding.imgProduct.setImageResource(this!!)

              /*  binding.rlPriceAndQty.setOnClickListener {
                   // itemClickListener.onItemClicked(position,binding.txtproductPriceAndQty,this.productPriceAndQty!!)
                }*/

            }
        }
    }

    override fun getItemCount(): Int {
        return savedList.size
    }

    interface ItemClickCallback {
        fun onItemClick(item: ProductListRes.Data.Data, type: Int, position: Int)
    }
}