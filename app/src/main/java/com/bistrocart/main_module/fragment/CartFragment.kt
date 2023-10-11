package com.bistrocart.main_module.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.base.BaseFragment
import com.bistrocart.databinding.DialogUnitPriceBinding
import com.bistrocart.databinding.FragmentCartBinding
import com.bistrocart.main_module.activity.HomeScreenActivity
import com.bistrocart.main_module.activity.OrderSummaryActivity
import com.bistrocart.main_module.activity.ProductActivity
import com.bistrocart.main_module.adapter.MyCartListAdapter
import com.bistrocart.main_module.adapter.UniPriceAdapterCard
import com.bistrocart.main_module.model.req.AddToCartReq
import com.bistrocart.main_module.model.res.AddToCartRes
import com.bistrocart.main_module.model.res.MyCartRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class CartFragment : BaseFragment() {
    private val TAG = "CartFragment"
    private lateinit var binding: FragmentCartBinding
    private lateinit var myCartListAdapter: MyCartListAdapter
    private val myCartList = ArrayList<MyCartRes.Data>()
    lateinit var uniPriceAdapterCart: UniPriceAdapterCard
    lateinit var dialogUnitPriceBinding: DialogUnitPriceBinding
    var isCallAddToCart: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        // Retrieve data from the bundle
        val bundle = arguments
        if (bundle != null) {
            val data = bundle.getString("FromProduct")
            var id = bundle.getString("id").toString()
            var title = bundle.getString("title").toString()
             if (data.equals("true")){
                 binding.imgBack.setOnClickListener {
                     var bundle =Bundle()
                     bundle.putString("id",id)
                     bundle.putString("title",title)
                     gotoActivity(ProductActivity::class.java,bundle,true)
                 }
             }
        }else{
            binding.imgBack.setOnClickListener {
              //  findNavController().navigate(R.id.homeFragment)
                (activity as HomeScreenActivity).intent =null
                gotoActivity(HomeScreenActivity::class.java,null,true)
            }
        }


        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setMyCartListAdapter()

        binding.btnCheckOut.setOnClickListener(View.OnClickListener {
            var bundle = Bundle()
//            bundle.putString("totalCartPrice", getTotalItemListPrices(myCartList).toString())
            if (myCartList != null && !myCartList.isEmpty()) {
                gotoActivity(OrderSummaryActivity::class.java, bundle, false)
            }
        })
    }

    private fun setMyCartListAdapter() {
        binding.rvCartDetails.layoutManager = LinearLayoutManager(requireContext())
        myCartListAdapter =
            MyCartListAdapter(myCartList, true, object : MyCartListAdapter.ItemClickCallback {
                override fun onItemClick(item: MyCartRes.Data, type: Int, position: Int) {
                    when (type) {
                        4 -> { // plus
                            if (item.quantity != null && item.quantity.isNotEmpty()) {
                                var countPlus: Int = item.quantity.toInt()
                                countPlus++
                                Log.e(TAG, "countPlus: $countPlus")
                                if (isCallAddToCart) {
                                    isCallAddToCart =false
                                    Log.e(TAG, "myCartList123Plus:" + isCallAddToCart)
                                    addToCart(item.product_id!!.toInt(), countPlus.toString(), "1", position)
                                }
                            } else {
                                // addToCart(item.product_id!!.toInt(), "1", "1", position)
                            }
                        }
                        3 -> { // minus
                            if (item.quantity != null && item.quantity.isNotEmpty() && item.quantity != "0") {
                                var countMinus: Int = item.quantity.toInt()
                                countMinus--
                                Log.e(TAG, "countMinus: $countMinus")
                                // if (countMinus.equals(0)) {
                                //     Log.e(TAG, "countMinus_is: "+countMinus)
                                //     myCartList() }
                                //  else {
                                if (isCallAddToCart) {
                                    isCallAddToCart =false
                                    Log.e(TAG, "myCartList123Minus:" + isCallAddToCart)
                                    addToCart(item.product_id.toInt(), countMinus.toString(), "1", position)
                                }
                                // }
                            } else {
                                Log.e(TAG, "onItemClick: " + item.quantity)
                            }
                        }
                        2 -> { // type equals 2
                            if (item.prices != null && item.prices.size > 0) {
                                unitPriceDialog(item.prices, item.quantity)
                            } else {

                            }
                        }
                    }
                    /* Log.e(TAG,
                         "getTotalItemListPrices: " + getTotalItemListPrices(myCartList).toString())*/
                }
            })
        binding.rvCartDetails.adapter = myCartListAdapter
        binding.rvCartDetails.setHasFixedSize(true)
        myCartListAdapter.notifyDataSetChanged()
    }


    private fun unitPriceDialog(
        productsPrices: List<MyCartRes.Data.Price>,
        unitSelect: String?,
    ) {

        val dialog: Dialog
        dialog = Dialog(requireContext(), R.style.appDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogUnitPriceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_unit_price,
            null,
            false
        )

        uniPriceAdapterCart = UniPriceAdapterCard(productsPrices, unitSelect!!,
            object : UniPriceAdapterCard.ItemClickCallback {
                override fun onItemClick(item: MyCartRes.Data.Price, type: Int) {
                }

            })

        dialogUnitPriceBinding.rvUnitPrice.adapter = uniPriceAdapterCart

        dialogUnitPriceBinding.btnAdd.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        dialog.setContentView(dialogUnitPriceBinding.getRoot())
        dialog.show()
    }

    @SuppressLint("CheckResult", "NotifyDataSetChanged")
    private fun myCartList(forTotal: Boolean) {
        if (!isOnline()) {
            return
        }
        isCallAddToCart = false
        myCartList.clear()
        Log.e(TAG, "myCartListStartCallingApi:"+false )
        if (forTotal) {
            showLoading("loading")
        }
        apiService?.myCartList()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<MyCartRes> ->
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        Log.e(TAG, "isSuccess:")
                        if (!t.body()!!.data.isNullOrEmpty()) {
                            if (!t.body()?.data.isNullOrEmpty()){
                                myCartList.addAll(t.body()!!.data)
                                (activity as HomeScreenActivity).getBadge(t.body()?.data!!.size)
                            }else{
                                (activity as HomeScreenActivity).getBadge(0)
                            }

                            binding.tvTotal.text = getString(R.string.money) + t.body()!!.total_Cartprice.toString()
                            myCartListAdapter.notifyDataSetChanged()
                            isCallAddToCart = true
                        }else{
                            myCartListAdapter.clean()
                            binding.tvTotal.text = getString(R.string.money) +"00"
                        }
                    }
                },
                { error ->
                    hideLoading()
                    Log.e(TAG, "isSuccessNot: ", )
                    //onFailure(error)
                }
            )

    }

    override fun onStart() {
        super.onStart()
        myCartListAdapter.notifyDataSetChanged()
        myCartList.clear()
        Log.e(TAG, "onResume123: ")
        myCartList(true)
        //addToCart(679,"37","1",0)
    }

    @SuppressLint("CheckResult")
    private fun addToCart(product_id: Int, quantity: String, s1: String, position: Int) {
        if (!isOnline()) {
            return
        }
        //  showLoading("loading")
        isCallAddToCart = false
        var req = (AddToCartReq(product_id.toString(), quantity, s1))
        Log.e(TAG, "addToCart: " + req)
        BaseActivity.apiService?.addToCartNew(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<MyCartRes> ->
                    AppLog.e(TAG, "add to cart: $t")
                    AppLog.e(TAG, "add to cart:" + Gson().toJson(t.body()))
                    hideLoading()

                    if (isSuccess(t, t.message())) {
                        Log.e(TAG, "addToCartIsSuccess:",)
                        myCartList.clear()
                        if (!t.body()?.data.isNullOrEmpty()){
                            myCartList.addAll(t.body()!!.data)
                            (activity as HomeScreenActivity).getBadge(t.body()?.data!!.size)
                        }else{
                            (activity as HomeScreenActivity).getBadge(0)
                        }
                        binding.tvTotal.text = getString(R.string.money) + t.body()!!.total_Cartprice.toString()
                        myCartListAdapter.notifyDataSetChanged()
                        isCallAddToCart = true
                        //  myCartList(false)
                    } else {
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                    Log.e(TAG, "addToCartIsSuccessNot: ", )
                }
            )
    }

    /* //total cart method
     fun getTotalItemListPrices(myCartList: List<MyCartRes.Data>): Int {
         var totalPrices = 0

         for (position in myCartList.indices) {
             totalPrices += calculateTotalPrice(position, myCartList)
         }

         return totalPrices
     }

     fun calculateTotalPrice(position: Int, myCartList: List<MyCartRes.Data>): Int {
         val qty = myCartList[position].quantity
         var flag = false
         var totalPrice = 0

         myCartList[position].prices.asReversed().forEach { priceData ->
             if (qty >= priceData.unit && !flag) {
                 flag = true
                 totalPrice = qty.toInt() * priceData.price_per_unit.toInt()
             }
         }

         return totalPrice
     }*/

}