package com.bistrocart.main_module.activity

import ProductCatogaryDetailsAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityProductBinding
import com.bistrocart.databinding.DialogUnitPriceBinding
import com.bistrocart.main_module.adapter.ProductCatogarySubDetailsAdapter
import com.bistrocart.main_module.adapter.UniPriceAdapter
import com.bistrocart.main_module.model.req.AddToCartReq
import com.bistrocart.main_module.model.req.CategoryMainSubReq
import com.bistrocart.main_module.model.req.ProductReq
import com.bistrocart.main_module.model.res.CategoryMainSubRes
import com.bistrocart.main_module.model.res.MyCartRes
import com.bistrocart.main_module.model.res.ProductListRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response


class ProductActivity : BaseActivity() {
    private val TAG = "ProductActivity"
    private lateinit var binding: ActivityProductBinding
    lateinit var adapterCatogaryDetailsAdapter: ProductCatogaryDetailsAdapter
    lateinit var adapterCatogarySubDetailsAdapter: ProductCatogarySubDetailsAdapter
    lateinit var uniPriceAdapter: UniPriceAdapter
    lateinit var id: String
    var ProductListID: String =""
    private val categoryList = ArrayList<CategoryMainSubRes.Data>()
    var imgPath: String? = ""
    var isCallAddToCart:Boolean =true

    lateinit var dialogUnitPriceBinding: DialogUnitPriceBinding

    //for pagination
     var page = 1
     var isLoading: Boolean = false
     var isLastPage: Boolean = false
    private lateinit var lm: LinearLayoutManager
    var perPage = 10 // Number of items per page
    var totalItems = 7 // Total number of items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product)

        Log.e("status-->", "onCreate: ")
        binding.rlCart.setOnClickListener(View.OnClickListener {
            var bundle = Bundle()
            bundle.putString("ProductActivity", "1")
            bundle.putString("id",id)
            bundle.putString("title",binding.txtCategoryName.text.toString())
            gotoActivity(HomeScreenActivity::class.java, bundle, false)
        })

        if (intent != null) {
            id = intent.getStringExtra("id").toString()
            binding.txtCategoryName.text = intent.getStringExtra("title").toString()
            Log.e(TAG, "onCreate: " + id)
        }

        initView()
        setRvView()
        setPagination()
        categoryList.clear()
        page = 1
        getCatogoryList()
    }

    private fun initView() {
        Log.e(TAG, "initView: " + categoryList)
        adapterCatogaryDetailsAdapter = ProductCatogaryDetailsAdapter(categoryList,
            object : ProductCatogaryDetailsAdapter.ItemClickCallback {
                override fun onItemClick(item: CategoryMainSubRes.Data, type: Int) {
                    ProductListID = item.id.toString()
                    isLastPage = false    /// ISSUE 5 , Ye kaha se false ho raha hai ?
                    page =1;   /// ISSUE 6  . YE ALREADY BATAYA THA KI .. SUB CATEGORY CHANGE HOTI HAI TO PAGE 1 KARNA HOGA TUMHE !
                    getProductList(1)
                }
            })

        binding.rvCartDetails.adapter = adapterCatogaryDetailsAdapter

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.llSearch.setOnClickListener(View.OnClickListener {
            gotoActivity(ProductSearchActivity::class.java,null,false)
        })

    }

    private fun setRvView() {
        adapterCatogarySubDetailsAdapter =
            ProductCatogarySubDetailsAdapter(/*productList,*/ imgPath!!,
                object : ProductCatogarySubDetailsAdapter.ItemClickCallback {
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
                                if (isCallAddToCart)
                                addToCart(item.id!!, "1", "1", position)
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
                            }
                        }
                    }
                })

        binding.rvCartSubDetails.adapter = adapterCatogarySubDetailsAdapter
    }

        private fun setPagination() {
            val gridLayoutManager = GridLayoutManager(this, 2)
            binding.rvCartSubDetails.layoutManager = gridLayoutManager
            binding.rvCartSubDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int,
                ) {
                    super.onScrollStateChanged(recyclerView, newState)

                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = recyclerView.layoutManager!!.childCount
                    val totalItemCount: Int = gridLayoutManager.itemCount
                    val firstVisibleItemPosition: Int = gridLayoutManager.findFirstVisibleItemPosition()

                    Log.e(TAG, "onScrolled: visibleItemCount is  ${visibleItemCount}")
                    Log.e(TAG, "onScrolled: totalItemCount is  ${totalItemCount}")
                    Log.e(TAG, "onScrolled: firstVisibleItemPosition is  ${firstVisibleItemPosition}")
                    Log.e(TAG, "onScrolled: isLoading and IslastPage is  ${isLoading} : ${isLastPage}\"")

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {

                        if (!isLoading && !isLastPage) {

                            page++
                            Log.e(TAG, "page number ${page.toString()}");
                            isLoading = true

                            val totalPages = if (totalItems % perPage == 0) {
                                totalItems / perPage // Total pages when the division is exact
                            } else {
                                (totalItems / perPage) + 1 // Total pages when there is a remainder
                            }
                            Log.e(TAG, "Total Pages: " + totalPages)

                            if (page >= totalPages) {
                                isLastPage = true
                                //  showToast(getString(R.string.all_data_fetched))
                            }

                            getProductList(page)
                        }
                    }
                }
            })
        }


    @SuppressLint("CheckResult")
    private fun getCatogoryList() {
        categoryList.clear()
        if (!isOnline()) {
            return
        }

        //   showLoading("loading")

        var req = (CategoryMainSubReq(id))
        apiService?.getSubCategoryList(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<CategoryMainSubRes> ->
                    AppLog.e(TAG, "CategoryRes123: $t")
                    AppLog.e(TAG, "Category.body()123: " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        categoryList.addAll(t.body()!!.data)
                        adapterCatogaryDetailsAdapter.notifyDataSetChanged()
                        Log.e(TAG, "getCatogoryList: " + categoryList)
                        ProductListID = t.body()!!.data.get(0).id.toString()
                        getProductList(1)

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


    @SuppressLint("CheckResult", "NotifyDataSetChanged")
    private fun getProductList(nextPage: Any) {
        Log.e(TAG, "getProductList_page: " + nextPage + " productId : "+ProductListID)
        if (!isOnline()) {
            return
        }

        if (nextPage == 1) {
            showLoading("loading")
            adapterCatogarySubDetailsAdapter!!.clearData()
            //isLastPage = false
        }

        var req = (ProductReq(ProductListID))
        Log.e(TAG, "ProductReq_ProductListID-->: " + req + " , page : "+nextPage.toString())
        apiService?.getProductList(nextPage.toString(), req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<ProductListRes> ->
                    AppLog.e(TAG, "getProductList: $t")
                    AppLog.e(TAG, "getProductList.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        imgPath = t.body()!!.fileurl.toString()
                        adapterCatogarySubDetailsAdapter!!.addData(t.body()!!.data?.data!!,imgPath)
                        totalItems = t.body()!!.data?.total!!
                        perPage = t.body()!!.data?.per_page!!
                        isLoading = false
                    } else {
                        isLoading = false
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    isLoading = false
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
        isCallAddToCart = false
        var req = (AddToCartReq(product_id.toString(), quantity, s1))
        Log.e(TAG, "addToCart: " + req)
        apiService?.addToCartNew(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<MyCartRes> ->
                    AppLog.e(TAG, "add to cart1: $t")
                    AppLog.e(TAG, "add to cart2:" + Gson().toJson(t.body()))
                    hideLoading()

                  /*  getTotalCount { count ->
                        if (count!=0) {
                            binding.cartCount.visibility =View.VISIBLE
                            Log.e(TAG, "addToCart123: " + count)
                            binding.tvCount.text = count.toString()
                        }else{
                            binding.cartCount.visibility =View.GONE
                        }
                    }*/

                    if (isSuccess(t, t.message())) {
                        adapterCatogarySubDetailsAdapter!!.updateItemValue(position,quantity.toInt())
                        isCallAddToCart =true
                         if (t.body()?.data != null && t.body()?.data!!.isNotEmpty()) { /// ISSUE 4 //// WHAT IF DATA IS NULL ?
                            // (this@ProductActivity as? HomeScreenActivity)?.getBadge(t.body()?.data!!.size)
                             //HomeScreenActivity().getBadge(t.body()?.data!!.size)

                           //  HomeScreenActivity().getBadge(t.body()?.data!!.size)
                           //  getTotalCount { count ->
                                 if (t.body()?.data!!.size != 0) {
                                     binding.cartCount.visibility = View.VISIBLE
                                     Log.e(TAG, "addToCart3: " + t.body()?.data!!.size)
                                     binding.tvCount.text = t.body()?.data!!.size.toString()
                                 } else {
                                     binding.cartCount.visibility = View.GONE
                                 }
                            // }
                            // (activity as HomeScreenActivity).getBadge()
                         }/// ISSUE 3   // WHAT IF SIZE IS NOT ZERO!!
                        else
                         {
                             binding.cartCount.visibility = View.GONE
                         }
                        Log.e(TAG, "addToCart4---> ")

                    } else
                    {
                        showToast(t.body()?.message)
                        Log.e(TAG, "addToCart 5---> "+t.body()?.message )
                    }

                },
                { error ->
                    hideLoading()
                    onFailure(error)
                   // showToast(error.message)  //ISSUE 2  // CRASH
                    Log.e(TAG, "addToCart 6---> "+error.message )
                }
            )

    }

    override fun onStart() {
        super.onStart()  // ISSUE 7  /// WHY WE ARE RELOADING CATEGORY AGAIN !! Why setPagination here ? // My Cart again we are calling everytime ..

        isLoading = false
        isLastPage = false

        Log.e("status-->", "onStart: ")

        getTotalCount { count ->
            if (count!=0) {
                binding.cartCount.visibility =View.VISIBLE
                Log.e(TAG, "addToCart123: " + count)
                binding.tvCount.text = count.toString()
            }else{
                binding.cartCount.visibility =View.GONE
            }
        }

        page =1
        if(ProductListID !=null && ProductListID.isNotEmpty())
            getProductList(page)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        gotoActivity(HomeScreenActivity::class.java,null,true)
    }

}