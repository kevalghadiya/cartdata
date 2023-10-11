package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityMyOrderListBinding
import com.bistrocart.databinding.AdapterMyOrderListBinding
import com.bistrocart.main_module.adapter.OrderListAdapter
import com.bistrocart.main_module.model.res.OrderListRes
import com.bistrocart.main_module.model.res.TimeSloteRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MyOrderListActivity : BaseActivity() {
    private  val TAG = "MyOrderListActivity"
    lateinit var binding:ActivityMyOrderListBinding
    private var orderListAdapter: OrderListAdapter?=null

    var page = 1
    var isLoading: Boolean = false
    var isLastPage: Boolean = false
    private lateinit var lm: LinearLayoutManager
    var perPage = 10 // Number of items per page
    var totalItems = 7 // Total number of items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_my_order_list)
        initView()
        setPagination()
    }

    private fun setPagination() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvOrderList.layoutManager = layoutManager

        binding.rvOrderList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int,
            ) {
                super.onScrollStateChanged(recyclerView, newState)

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = recyclerView.layoutManager!!.childCount
                val totalItemCount: Int = layoutManager.itemCount
                val firstVisibleItemPosition: Int = layoutManager.findFirstVisibleItemPosition()

                Log.e(TAG, "onScrolled: visibleItemCount is  ${visibleItemCount}")
                Log.e(TAG, "onScrolled: totalItemCount is  ${totalItemCount}")
                Log.e(TAG, "onScrolled: firstVisibleItemPosition is  ${firstVisibleItemPosition}")

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

                        orderList(page)
                    }
                }
            }
        })

    }

    private fun initView() {
        orderListAdapter = OrderListAdapter(object : OrderListAdapter.ItemClickCallback {
            override fun onItemClick(item: OrderListRes.Data.Data, type: Int) {
                val bundle = Bundle()
                bundle.putString("order_id", item.id.toString())
                gotoActivity(MyOrderDetailsActivity::class.java, bundle, false)
            }
        })
        binding.rvOrderList.adapter = orderListAdapter


        binding.toolbar.title.text = "Order List"
        binding.toolbar.imgBack.setOnClickListener(View.OnClickListener {
           onBackPressed()
        })

    }


    override fun onStart() {
        super.onStart()
        orderList(1)
    }

    @SuppressLint("CheckResult")
    private fun orderList(page: Int) {
        if (!isOnline()) {
            return
        }

        if (page == 1) {
            showLoading("loading")
            orderListAdapter!!.clearData()
        }
        apiService?.myOrderList(page.toString())
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<OrderListRes> ->
                    AppLog.e(TAG, "myOrderList: $t")
                    AppLog.e(TAG, "myOrderList.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        val newItems = t.body()?.data?.data
                        if (newItems != null) {
                            totalItems = t.body()!!.data?.total!!
                            perPage = t.body()!!.data?.per_page!!
                            orderListAdapter!!.addData(newItems)
                            isLoading = false
                        }
                    } else {
                        isLoading = false
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                    isLoading = false
                }
            )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent?.let {
            val isOrderSummary = it.getStringExtra("isOrderSummary")
            when (isOrderSummary) {
                "true" -> gotoActivity(HomeScreenActivity::class.java, null, true)
                else -> finish()
            }
        } ?: finish()
    }
}