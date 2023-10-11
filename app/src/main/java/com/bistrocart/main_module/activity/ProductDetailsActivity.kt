package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityProductDetailsBinding
import com.bistrocart.main_module.adapter.UniPriceAdapterProductDetails
import com.bistrocart.main_module.model.req.AddToCartReq
import com.bistrocart.main_module.model.res.BasicRes
import com.bistrocart.main_module.model.res.FavouriteAddEditRes
import com.bistrocart.main_module.model.res.ProductListRes
import com.bistrocart.utils.AppLog
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class ProductDetailsActivity : BaseActivity() {
    private val TAG = "ProductDetailsActivity"
    lateinit var binding: ActivityProductDetailsBinding
    private val itemList: ArrayList<ProductListRes.Data.Data> = ArrayList()
    lateinit var uniPriceAdapter: UniPriceAdapterProductDetails
    var count: Int = 0
    lateinit var productId: String
    private var isFavourite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_details)

        if (intent != null) {
            itemList.add(intent.getSerializableExtra("product_details") as ProductListRes.Data.Data)
            Log.e(TAG, "onCreate: " + itemList)
            binding.tvTitle.text = itemList.get(0).pro_name
            binding.imgBack.setOnClickListener(View.OnClickListener {
                onBackPressed()
                finish()
            })
        }

        initView()
    }

    private fun initView() {
        val imageList: MutableList<SlideModel> = mutableListOf()
        val productsImages: ArrayList<ProductListRes.Data.Data.ProductsImage> =
            itemList.get(0).products_images as ArrayList<ProductListRes.Data.Data.ProductsImage>

        for (productImage in productsImages) {
            val slideModel = SlideModel(intent.getStringExtra("imgPath").toString() + productImage.pro_image)
            imageList.add(slideModel)
            Log.e(TAG, "imageList-->>: " + Gson().toJson(imageList))
            binding.imageSlider.setImageList(imageList, ScaleTypes.CENTER_INSIDE)
        }

        binding.tvProductName.text = itemList.get(0).pro_name
        productId = itemList.get(0).id.toString()
        binding.tvProductDesc.text = itemList.get(0).pro_description

//        binding.tvProductPrice.text = itemList.get(0).products_prices.get(0).price_per_unit

        if (!itemList.get(0).cart_items.isNullOrEmpty()) {
            binding.tvMoneyCount.text = itemList.get(0).cart_items.get(0).quantity
            count = itemList.get(0).cart_items.get(0).quantity!!.toInt()
        }
        getPrice()

        uniPriceAdapter = UniPriceAdapterProductDetails(itemList.get(0).products_prices, ""!!,
            object : UniPriceAdapterProductDetails.ItemClickCallback {
                override fun onItemClick(item: ProductListRes.Data.Data.ProductsPrice, type: Int) {
                }
            })

        binding.rvProductPrice.adapter = uniPriceAdapter

        binding.ivPlus.setOnClickListener(View.OnClickListener {
            count++
            addToCart(count.toString())
        })

        binding.ivMinus.setOnClickListener(View.OnClickListener {
            if (count != 0) {
                count--
                addToCart(count.toString())
            } else {
                showToast("Can't add less then 0 quantity")
            }
        })


        binding.btnDone.setOnClickListener(View.OnClickListener {
            var bundle = Bundle()
            bundle.putString("ProductActivity", "1")
            gotoActivity(HomeScreenActivity::class.java, bundle, false)
        })

        if (itemList.get(0).favourite_products.isNullOrEmpty()) {
            isFavourite = false
            Log.e(TAG, "isFavourite: " + false)
            binding.ivFavourite.setBackgroundResource(R.drawable.unsaved_icon);
        } else {
            Log.e(TAG, "isFavourite: " + true)
            isFavourite = true
            binding.ivFavourite.setBackgroundResource(R.drawable.saved_icon);
        }

        binding.ivFavourite.setOnClickListener(View.OnClickListener {
            if (isFavourite) {
                isFavourite = false
                Log.e(TAG, "isFavourite: " + false)
                binding.ivFavourite.setBackgroundResource(R.drawable.unsaved_icon);
                favourAddEdit()
            } else {
                isFavourite = true
                Log.e(TAG, "isFavourite: " + true)
                binding.ivFavourite.setBackgroundResource(R.drawable.saved_icon);
                favourAddEdit()
            }
        })

        if(itemList.get(0).instock.equals("0")){
            binding.ivPlus.isClickable =false
            binding.ivMinus.isClickable =false
            binding.ivMinus.setBackgroundResource(R.drawable.ic_minius_dark);
            binding.ivPlus.setBackgroundResource(R.drawable.ic_plus_dark);
        }
    }

    @SuppressLint("CheckResult")
    private fun addToCart(count: String) {
        if (!isOnline()) {
            return
        }
        // showLoading("loading")

        var req = (AddToCartReq(itemList.get(0).id.toString(), count, "1"))
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
                        binding.tvMoneyCount.text = count
                        getPrice()
                    } else {
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                    showToast(error.message)
                }
            )
    }

    private fun getPrice() {
        var flag:Boolean =false
        var totalPrice = 0
        totalPrice = itemList.get(0).products_prices.get( itemList.get(0).products_prices.size-1).price_per_unit!!.toInt()
        itemList.get(0).products_prices.forEach { product ->
            if (this.count!!.toInt() <=product.unit!!.toInt() && !flag) {
                flag = true
                totalPrice = product.price_per_unit!!.toInt()
                Log.e("TAG", "total_is:--->>"+totalPrice )
            }
        }
        binding.tvProductPrice.text =getString(R.string.money)+totalPrice.toString()
    }

    @SuppressLint("CheckResult")
    private fun favourAddEdit() {
        if (!isOnline()) {
            return
        }
        Log.e(TAG, "favourAddEditReq: "+productId+isFavourite.toString() )
        apiService?.favourAddEdit(productId, isFavourite.toString())
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<FavouriteAddEditRes> ->
                    AppLog.e(TAG, "favourAddEdit: $t")
                    AppLog.e(TAG, "favourAddEdit.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {

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

}