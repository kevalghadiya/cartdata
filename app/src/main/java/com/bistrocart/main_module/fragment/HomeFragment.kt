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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.base.BaseFragment
import com.bistrocart.databinding.DialogUnitPriceBinding
import com.bistrocart.databinding.FragmentHomeBinding
import com.bistrocart.login_module.activity.SignInActivity
import com.bistrocart.main_module.activity.*
import com.bistrocart.main_module.adapter.CategoryListAdapter
import com.bistrocart.main_module.adapter.DealDayListAdapter
import com.bistrocart.main_module.adapter.UniPriceAdapterDealOfTheDay
import com.bistrocart.main_module.model.req.AddToCartReq
import com.bistrocart.main_module.model.res.*
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class HomeFragment : BaseFragment(), CategoryListAdapter.OnItemListener {
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var categoryListAdapter: CategoryListAdapter
    private lateinit var deaDayListAdapter: DealDayListAdapter
    private val categoryList = ArrayList<CategoryRes.Data>()
    private val dealDayList = ArrayList<ProductListRes.Data.Data>()
    lateinit var dialogUnitPriceBinding: DialogUnitPriceBinding
    lateinit var uniPriceAdapterDealOfTheDay: UniPriceAdapterDealOfTheDay
    var imgPath: String? = ""
    var isProductDetails = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        categoryData()
        setCategoryAdapter()
        setDealDayAdapter()
        initView()
    }

    private fun initView() {
        binding.tvName.text = "Hi, " + BaseActivity.appPref!!.getString(AppPref.OUTLET_NAME)
        binding.tvAddress.text = appPref!!.getString(AppPref.ADDRESS) + "..."
        binding.rvDealOfDay
        binding.txtViewAll.setOnClickListener {
            gotoActivity(CategoryActivity::class.java, null, false)
        }

        //Now not use navigation drawer
        /* binding.imgNavbar.setOnClickListener(View.OnClickListener {
             (activity as HomeScreenActivity).openDrawer()
         })*/

        binding.ivNotification.setOnClickListener(View.OnClickListener {
            gotoActivity(NotificationActivity::class.java, null, false)
        })

        binding.searchCard.setOnClickListener(View.OnClickListener {
            gotoActivity(ProductSearchActivity::class.java, null, false)
        })
    }

    private fun setCategoryAdapter() {
        binding.rvCategoryList.layoutManager = GridLayoutManager(requireContext(), 3)
        categoryListAdapter = CategoryListAdapter(categoryList, this)
        binding.rvCategoryList.adapter = categoryListAdapter
        binding.rvCategoryList.setHasFixedSize(true)
    }

    private fun setDealDayAdapter() {
        binding.rvDealOfDay.layoutManager = LinearLayoutManager(requireContext())
        deaDayListAdapter = DealDayListAdapter(
            imgPath!!,
            dealDayList,
            object : DealDayListAdapter.ItemClickCallback {
                override fun onItemClick(item: ProductListRes.Data.Data, type: Int, position: Int) {
                    if (type.equals(1)) {
                        // For ProductListRes.Data.Data
                        //  val commonProductData = item.toCommonProductData() // 'item' is of type ProductListRes.Data.Data
                        /*    val intent = Intent(this, ProductDetailsActivity::class.java)
                            intent.putExtra("product_details", commonProductData)
                            intent.putExtra("imgPath", imgPath)

                            val cmm :CommonProductData
                            cmm.cartItems=item.cart_items.
                            startActivity(intent)*/

                        isProductDetails = true
                        var bundle = Bundle()
                        bundle.putSerializable("product_details", item)
                        bundle.putString("imgPath", imgPath)
                        gotoActivity(ProductDetailsActivity::class.java, bundle, false)

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
                            addToCart(item.id!!, countPlus.toString(), "1", position)
                        } else {
                            addToCart(item.id!!, "1", "1", position)
                        }

                    } else if (type.equals(3)) {
                        //minus
                        if (item.cart_items != null && item.cart_items.isNotEmpty() && !item.cart_items.get(
                                0
                            ).quantity.equals("0")
                        ) {
                            var countMinus: Int = item.cart_items.get(0).quantity!!.toInt()
                            countMinus--
                            Log.e(TAG, "countMinus: $countMinus")
                            addToCart(item.id!!, countMinus.toString(), "1", position)
                        } else {
                            showToast("Can't add less then 0 quantity")
                            //   addToCart(item.id!!,"1", "1")
                        }
                    }
                }

            })
        binding.rvDealOfDay.adapter = deaDayListAdapter
    }

    override fun onStart() {
        super.onStart()
        dealOfTheDayList()
    }

    @SuppressLint("CheckResult")
    private fun categoryData() {
        categoryList.clear()
        if (!isOnline()) {
            return
        }

        showLoading("loading")
        BaseActivity.apiService?.getCategoryList()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<CategoryRes> ->
                    AppLog.e(TAG, "CategoryRes: ₹t")
                    AppLog.e(TAG, "Category.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        if (t.body()?.user_status.equals("block")) {
                            showToast("Blocked by admin")
                            logout()
                           /* gotoActivity(SignInActivity::class.java, null, true)
                            requireActivity().finish()*/
                        }
                        var count = 0 // Counter variable to keep track of the number of items added
                        for (item in t.body()!!.data) {
                            categoryList.add(item) // Add the item to the list
                            count++ // Increment the counter
                            if (count == 6) {
                                break // Exit the loop after adding six items
                            }
                        }
                        categoryListAdapter.notifyDataSetChanged()

                        //   showToast(t.body()?.message)
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
    private fun dealOfTheDayList() {
        dealDayList.clear()
        if (!isOnline()) {
            return
        }
        showLoading("loading")

        BaseActivity.apiService?.getDealOfTheDay()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<ProductListRes> ->
                    AppLog.e(TAG, "dealOfTheDayList:$t")
                    AppLog.e(TAG, "dealOfTheDayList:" + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {

                        if (t.body()?.data!! != null) {
                            imgPath = t.body()!!.fileurl.toString()
                            deaDayListAdapter.imgPath = imgPath as String // ISSUE 1

                            dealDayList.addAll(t.body()?.data!!.data!!)
                        }

                        deaDayListAdapter.notifyDataSetChanged()
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

    override fun onItemClicked(position: Int, list: ArrayList<CategoryRes.Data>) {
        var bundle = Bundle()
        bundle.putString("id", list.get(position).id.toString())
        bundle.putString("title", list.get(position).main_cat_name.toString())
        gotoActivity(ProductActivity::class.java, bundle, false)
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

        uniPriceAdapterDealOfTheDay = UniPriceAdapterDealOfTheDay(productsPrices,
            unitSelect!!,
            object : UniPriceAdapterDealOfTheDay.ItemClickCallback {
                override fun onItemClick(item: ProductListRes.Data.Data.ProductsPrice, type: Int) {
                    //  addToCartReq = (AddToCartReq(item.pro_id,item.unit,item.unit))
                    //   product_id =item.pro_id
                    //  quantity =item.unit
                    //   unit=item.unit
                }

            })
        dialogUnitPriceBinding.rvUnitPrice.adapter = uniPriceAdapterDealOfTheDay

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
        //  showLoading("loading")

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
                        if (!t.body()?.data.isNullOrEmpty()) {
                            (activity as HomeScreenActivity).getBadge(t.body()?.data!!.size)
                        } else {
                            (activity as HomeScreenActivity).getBadge(0)
                        }
                        deaDayListAdapter!!.updateItemValue(position, quantity.toInt())
                        // dealOfTheDayList()
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
}
