package com.bistrocart.main_module.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterCartDetailsRowFileBinding
import com.bistrocart.main_module.model.res.MyCartRes
import com.bumptech.glide.Glide

class MyCartListAdapter(
    private var myCartList: ArrayList<MyCartRes.Data>,
    private  var cart: Boolean,
    private val itemClickCallback: ItemClickCallback
) : RecyclerView.Adapter<MyCartListAdapter.MyCartListViewHolder>() {
   // private var totalPrice: Int = 0 // Total price variable
  //  var previousValue =0

    inner class MyCartListViewHolder(val binding: AdapterCartDetailsRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

/*    fun updateItemValue(position: Int, newValue: Int) {
        Log.e("count", "updateItemValue0--->>>: " + position + "new valus is-->>" + newValue)
        if (newValue==0){
          myCartList.removeAt(position)
          notifyDataSetChanged()
        }else {
           // previousValue = myCartList[position].quantity.toInt()
            myCartList[position].quantity = newValue.toString()
            Log.e("count", "updateItemValue1--->>>: " + position + "new valus is-->>" + newValue)
            Log.e("count", "updateItemValue2--->>>: " + position + "new valus is-->>" + newValue)
            notifyItemChanged(position)
        }
    }*/
    public fun clean(){
         myCartList.clear()
        notifyDataSetChanged()
    }

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
              //  if (0<myCartList.size ||myCartList.isNotEmpty()) {
                    binding.txtproductName.text = this.product_name
                    binding.txtQty.text = this.quantity

                    if (cart) {
                        binding.llPlusMinus.visibility = View.VISIBLE
                    } else {
                        binding.llPlusMinus.visibility = View.GONE
                    }

                    /*  var qty = myCartList[position].quantity
                var flag:Boolean =false
                var totalPrice = 0
                myCartList[position].prices.asReversed().forEach{
                    if (qty >=it.unit &&!flag){
                        flag =true
                        totalPrice = qty.toInt() * it.price_per_unit.toInt()
                    }
                }*/

                    binding.tvMoney.text = "₹" + this.price + " / " + this.quantity + " Qty."

                    //  binding.tvMoney.text ="₹"+totalPrice +" / "+binding.txtQty.text.toString()+" Qty."

                    //  this@MyCartListAdapter.totalPrice += totalPrice

                    binding.tvMoney.setOnClickListener {
                        itemClickCallback.onItemClick(
                            myCartList[position],
                            2,
                            position
                        )
                    }

                    binding.imgDecrement.setOnClickListener {
                        if (myCartList.size !=0){
                            itemClickCallback.onItemClick(myCartList[position], 3, position)
                        }
                    }
                    binding.imgIncrement.setOnClickListener {
                        if (myCartList.size !=0){
                            itemClickCallback.onItemClick(
                                myCartList[position],
                                4,
                                position
                            )
                        }
                    }


                    if (!this.image.equals(null)) {
                        Glide.with(itemView)
                            .load(this.image)
                            .into(binding.imgProduct)
                    } else {
                        // Handle the case when the products_images list is empty
                        // You can display a placeholder image or handle it according to your requirements
                    }

               // }
            }
        }
    }

    override fun getItemCount(): Int {
        return myCartList.size
    }

    interface ItemClickCallback {
        fun onItemClick(item: MyCartRes.Data, type: Int, position: Int)
    }
}