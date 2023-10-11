package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityNotificationBinding
import com.bistrocart.databinding.AdapterNotificationBinding
import com.bistrocart.main_module.adapter.NotificationAdapter
import com.bistrocart.main_module.adapter.OrderListAdapter
import com.bistrocart.main_module.model.res.MyCartRes
import com.bistrocart.main_module.model.res.NotificationRes
import com.bistrocart.main_module.model.res.OrderListRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class NotificationActivity : BaseActivity() {
    private  val TAG = "NotificationActivity"
    lateinit var binding:ActivityNotificationBinding
    private var notificationAdapter: NotificationAdapter?=null
    private var notificationList = ArrayList<NotificationRes.Data>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_notification)
        initView()
        notificationList()
        binding.toolbar.title.text ="Notification"
        binding.toolbar.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
            finish()
        })
    }

    private fun initView() {
        notificationAdapter = NotificationAdapter(notificationList,object : NotificationAdapter.ItemClickCallback {
            override fun onItemClick(item: NotificationRes.Data, type: Int) {
                if (!item.order_id.isNullOrEmpty()){
                    val bundle = Bundle()
                    bundle.putString("order_id", item.order_id.toString())
                    gotoActivity(MyOrderDetailsActivity::class.java, bundle, false)
                }
            }
        })
        binding.rvNotification.adapter = notificationAdapter
    }


    @SuppressLint("CheckResult")
    private fun notificationList() {
        notificationList.clear()
        if (!isOnline()) {
            return
        }

        showLoading("loading")
        apiService?.notificationList()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<NotificationRes> ->
                    AppLog.e(TAG, "cartRes: $t")
                    AppLog.e(TAG, "Category.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        notificationList.addAll(t.body()!!.data)
                        notificationAdapter!!.notifyDataSetChanged()
                    } else {
                        // showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )

    }
}