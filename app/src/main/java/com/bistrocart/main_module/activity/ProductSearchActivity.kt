package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityProductSearchBinding
import com.bistrocart.databinding.DialogUnitPriceBinding
import com.bistrocart.main_module.adapter.ProductSearchAdapter
import com.bistrocart.main_module.adapter.UniPriceAdapter
import com.bistrocart.main_module.model.req.AddToCartReq
import com.bistrocart.main_module.model.res.BasicRes
import com.bistrocart.main_module.model.res.ProductListRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class ProductSearchActivity : BaseActivity() {
    lateinit var binding:ActivityProductSearchBinding
    private val TAG = "ProductSearchActivity"
    private val productSearchList = ArrayList<ProductListRes.Data.Data>()
    lateinit var productSearchAdapter: ProductSearchAdapter
    var imgPath: String? = ""
    var isCallAddToCart:Boolean =true
    lateinit var uniPriceAdapter: UniPriceAdapter
    lateinit var dialogUnitPriceBinding: DialogUnitPriceBinding

    private val handler = Handler()
    private val delay = 1500L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_product_search)
        binding.edtSearch.requestFocus()
        initView()
        setRvView()
    }

    private fun initView() {
        binding.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Check if the text has three digits
                if (s?.length!! >=3) {
                    // Call the first method immediately
                  //  onThreeDigitsEntered(s.toString())

                    // Delay the second method call by 3 seconds
                    handler.removeCallbacksAndMessages(null) // Remove any pending callbacks
                    handler.postDelayed({
                        onDelayedMethodCall(s.toString())
                    }, delay)
                } else {
                    productSearchAdapter.clearData()
                    // If the text is not three digits, cancel any pending callbacks
                    handler.removeCallbacksAndMessages(null)
                }
            }
        })
    }


    /*private fun onThreeDigitsEntered(textSearch: String) {
        getProductSearch(textSearch)
    }*/

    private fun onDelayedMethodCall(textSearch: String) {
        // Second method implementation
        // This method will be called after a 3-second delay
        // Replace this with your desired logic
        getProductSearch(textSearch)
    }

    override fun onStart() {
        super.onStart()
        if (binding.edtSearch.length()>=3){
            getProductSearch(binding.edtSearch.text.toString())
        }
    }

    private fun setRvView() {
        productSearchAdapter =
            ProductSearchAdapter(/*productList,*/ imgPath!!,
                object : ProductSearchAdapter.ItemClickCallback {
                    override fun onItemClick(
                        item: ProductListRes.Data.Data,
                        type: Int,
                        position: Int,
                    ) {
                        if (type.equals(1)) {
                            var bundle =Bundle()
                            bundle.putSerializable("product_details",item)
                            bundle.putString("imgPath",imgPath)
                            gotoActivity(ProductDetailsActivity::class.java,bundle,false)

                        } else if (type.equals(2)) {
                            if (item.cart_items != null && item.cart_items.size > 0) {
                                unitPriceDialog(item.products_prices, item.cart_items.get(0).unit)
                            } else {
                                unitPriceDialog(item.products_prices, "")
                            }
                        } else if (type.equals(4)) {
                            //plus
                            if (item.cart_items != null && item.cart_items.isNotEmpty()) {
                                // showToast("you can't add less then 0 quantity")
                                var countPlus: Int = item.cart_items.get(0).quantity!!.toInt()
                                countPlus++
                                Log.e(TAG, "countPlus: $countPlus")
                                if(isCallAddToCart) {
                                    addToCart(item.id!!, countPlus.toString(), "1", position)
                                }
                            } else {
                                if (isCallAddToCart) {
                                      addToCart(item.id!!, "1", "1", position)
                                }
                            }

                        } else if (type.equals(3)) {
                            //minus
                            if (item.cart_items != null && item.cart_items.isNotEmpty() && !item.cart_items.get(0).quantity.equals("0")) {
                                var countMinus: Int = item.cart_items.get(0).quantity!!.toInt()
                                countMinus--
                                Log.e(TAG, "countMinus: $countMinus")
                                if (isCallAddToCart) {
                                   addToCart(item.id!!, countMinus.toString(), "1", position)
                                }
                            } else {
                                showToast("Can't add less then 0 quantity")
                                  addToCart(item.id!!,"1", "1",position)
                            }
                        } else {
                            /*  Log.e(TAG, "onItemClick: "+item.id )
                              val itemId = item.id!!
                              val quantityStr = quantity.toString()
                              addToCart(itemId, quantityStr, "1")*/
                        }
                    }
                })

        binding.rvCartSubDetails.adapter = productSearchAdapter
    }

    @SuppressLint("CheckResult")
    private fun getProductSearch(textSearch: String) {
       // productSearchList.clear()
        productSearchAdapter.clearData()
        if (!isOnline()) {
            return
        }
        showLoading("loading")

        apiService?.getProductSearch(textSearch)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<ProductListRes> ->
                    AppLog.e(TAG, "dealOfTheDayList:$t")
                    AppLog.e(TAG, "dealOfTheDayList:" + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        productSearchAdapter.addData(t.body()?.data!!.data!! , t.body()!!.fileurl)
                        imgPath =t.body()!!.fileurl
                      //  productSearchList.addAll(t.body()?.data!!.data!!)
                      //  adapterCatogarySubDetailsAdapter.notifyDataSetChanged()
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



    private fun unitPriceDialog(
        productsPrices: List<ProductListRes.Data.Data.ProductsPrice>,
        unitSelect: String?,
    ) {

        val dialog: Dialog
        dialog = Dialog(this, R.style.appDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogUnitPriceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_unit_price,
            null,
            false
        )

        uniPriceAdapter = UniPriceAdapter(productsPrices,
            unitSelect!!,
            object : UniPriceAdapter.ItemClickCallback {
                override fun onItemClick(item: ProductListRes.Data.Data.ProductsPrice, type: Int) {

                }
            })
        dialogUnitPriceBinding.rvUnitPrice.adapter = uniPriceAdapter

        dialogUnitPriceBinding.btnAdd.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
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
        isCallAddToCart = false
        var req = (AddToCartReq(product_id.toString(), quantity, s1))
        Log.e(TAG, "addToCart: " + req)
        apiService?.addToCart(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<BasicRes> ->
                    AppLog.e(TAG, "add to cart: $t")
                    AppLog.e(TAG, "add to cart:" + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        //  getProductList(1)
                        productSearchAdapter!!.updateItemValue(position,quantity.toInt())
                        isCallAddToCart =true
                        Log.e(TAG, "addToCart true1---> ", )
                    } else {
                        showToast(t.body()?.message)
                        Log.e(TAG, "addToCart true---> "+t.body()?.message )
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                    showToast(error.message)
                    Log.e(TAG, "addToCart false---> "+error.message )
                }
            )
    }
}