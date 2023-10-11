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
import com.bistrocart.main_module.adapter.SavedListAdapter
import com.bistrocart.base.BaseFragment
import com.bistrocart.databinding.DialogUnitPriceBinding
import com.bistrocart.databinding.FragmentLikeBinding
import com.bistrocart.main_module.activity.HomeScreenActivity
import com.bistrocart.main_module.adapter.UniPriceAdapterLikeList
import com.bistrocart.main_module.model.req.AddToCartReq
import com.bistrocart.main_module.model.res.*
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class LikeFragment : BaseFragment() {
    private  val TAG = "LikeFragment"
    private lateinit var binding: FragmentLikeBinding
    private lateinit var savedListAdapter: SavedListAdapter
    private val savedOrUnsavedList = ArrayList<ProductListRes.Data.Data>()
    lateinit var uniPriceAdapterLikeList: UniPriceAdapterLikeList
    lateinit var dialogUnitPriceBinding: DialogUnitPriceBinding
  //  var path:String? =""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favouriteList()
        setSavedOrUnSavedListAdapter()

        binding.imgBack.setOnClickListener {
          //  findNavController().navigate(R.id.homeFragment)
            gotoActivity(HomeScreenActivity::class.java,null,true)
        }
    }

    private fun setSavedOrUnSavedListAdapter(){
        binding.rvCartDetails.layoutManager = LinearLayoutManager(requireContext())
        savedListAdapter = SavedListAdapter(savedOrUnsavedList,/*path!!,*/object:SavedListAdapter.ItemClickCallback{
            override fun onItemClick(item: ProductListRes.Data.Data, type: Int, position: Int) {
                if (type.equals(1)) {

                } else if (type.equals(2)) {
                    if (item.cart_items != null && item.cart_items.size > 0) {
                        unitPriceDialog(item.products_prices, item.products_prices.get(0).unit)
                    } else {
                        unitPriceDialog(item.products_prices, "")
                    }
                } else if (type.equals(4)) {
                    //plus
                    if (item.cart_items != null && item.cart_items.isNotEmpty() && !item.cart_items.get(0).quantity.equals("0")) {
                        // showToast("you can't add less then 0 quantity")
                        var countPlus: Int = item.cart_items.get(0).quantity!!.toInt()
                        countPlus++
                        Log.e(TAG, "countPlus: $countPlus")
                        addToCart(item.id!!, countPlus.toString(), "1",position)
                    } else {
                        addToCart(item.id!!, "1", "1", position)
                    }

                } else if (type.equals(3)) {
                    //minus
                    if (item.cart_items != null && item.cart_items.isNotEmpty() && !item.cart_items.get(0).quantity.equals("0")) {
                        var countMinus: Int = item.cart_items.get(0).quantity!!.toInt()
                        countMinus--
                        Log.e(TAG, "countMinus: $countMinus")
                        addToCart(item.id!!, countMinus.toString(), "1", position)
                    } else {
                        showToast("Can't add less then 0 quantity")
                        //   addToCart(item.id!!,"1", "1")
                    }
                }else if (type.equals(5)){
                    favourAddEdit(item.id.toString(),position)
                } else {
                    /*  Log.e(TAG, "onItemClick: "+item.id )
                      val itemId = item.id!!
                      val quantityStr = quantity.toString()
                      addToCart(itemId, quantityStr, "1")*/
                }
                /* else if (type.equals(4)) {
                     //plus
                     addToCart(item.id!!, "1", "1")
                 }else if (type.equals("3")){
                     //minus
                     addToCart(item.id!!, "0", "0")
                 }*/
            }

        })
        binding.rvCartDetails.adapter = savedListAdapter
        binding.rvCartDetails.setHasFixedSize(true)
    }


/*    override fun onItemClicked(position: Int, textview: AppCompatTextView, priceAndQty: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .create()
        val view = layoutInflater.inflate(R.layout.dialog_unit_price, null)
        view.apply {

         //   val btnBack = findViewById<AppCompatButton>(R.id.btnBack)
//            val unitOnePriceClick = findViewById<LinearLayout>(R.id.llOneUnits)
//            val txtOneUnits = findViewById<AppCompatTextView>(R.id.txtUnits)
//            val txtOneUnitsPrice = findViewById<AppCompatTextView>(R.id.txtUnitsPrice)
//            val unitTwoPriceClick = findViewById<LinearLayout>(R.id.llTwoUnits)
//            val unitThreePriceClick = findViewById<LinearLayout>(R.id.llThreeUnits)
//            val unitFourPriceClick = findViewById<LinearLayout>(R.id.llFourUnits)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
*//*
            btnBack.setOnClickListener {
                dialog.dismiss()
            }*//*

            dialog.show()
        }
    }*/

    @SuppressLint("CheckResult")
    private fun favouriteList() {
        if (!isOnline()) {
            return
        }
        showLoading("loading")
        BaseActivity.apiService?.favouriteList()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<ProductListRes> ->
                    AppLog.e(TAG, "FavouriteRes: $t")
                    AppLog.e(TAG, "FavouriteRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
//                        path = t.body()!!.fileurl
                        savedListAdapter.updateImagePath(t.body()!!.fileurl.toString())
                        savedOrUnsavedList.addAll(t.body()!!.data!!.data!!)
                        savedListAdapter.notifyDataSetChanged()
                    } else {
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )
    }

 @SuppressLint("CheckResult")
    private fun favourAddEdit(id: String, position: Int) {
        if (!isOnline()) {
            return
        }
       // showLoading("loading")
        BaseActivity.apiService?.favourAddEdit(id,"false")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<FavouriteAddEditRes> ->
                    AppLog.e(TAG, "favourAddEdit: $t")
                    AppLog.e(TAG, "favourAddEdit.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                      savedListAdapter.updateFavourite(position)
                    } else {
                     //   showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )
    }


    private fun unitPriceDialog(
        productsPrices: List<ProductListRes.Data.Data.ProductsPrice>,
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

        uniPriceAdapterLikeList = UniPriceAdapterLikeList(productsPrices,
            unitSelect!!,
            object : UniPriceAdapterLikeList.ItemClickCallback {
                override fun onItemClick(item: ProductListRes.Data.Data.ProductsPrice, type: Int) {
                    //  addToCartReq = (AddToCartReq(item.pro_id,item.unit,item.unit))
                    //   product_id =item.pro_id
                    //  quantity =item.unit
                    //   unit=item.unit
                }
            })
        dialogUnitPriceBinding.rvUnitPrice.adapter = uniPriceAdapterLikeList

        dialogUnitPriceBinding.btnAdd.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            /* if (product_id!=null &&product_id!="") {
                 addToCart(dialog)
             }*/
        })
        dialog.setContentView(dialogUnitPriceBinding.getRoot())
        dialog.show()

    }


    @SuppressLint("CheckResult")
    private fun addToCart(product_id: Int, quantity: String, s1: String, position: Int) {
        if (!isOnline()) {
            return
        }
        // showLoading("loading")

        var req = (AddToCartReq(product_id.toString(), quantity, s1))
        Log.e(TAG, "addToCart: " + req)
        BaseActivity.apiService?.addToCartNew(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<MyCartRes> ->
                    AppLog.e(TAG, "add to cart: $t")
                    AppLog.e(TAG, "add to cart:" + Gson().toJson(t.body()))
                   // hideLoading()

                    if (isSuccess(t, t.message())) {
                        (activity as HomeScreenActivity).getBadge(t.body()?.data!!.size)
                        savedListAdapter!!.updateItemValue(position, quantity.toInt())
                    } else {
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                   // showToast(error.message)
                }
            )
    }

}