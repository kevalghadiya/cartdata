package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityMyOrderDetailsBinding
import com.bistrocart.main_module.adapter.MyOrderStatusAdapter
import com.bistrocart.main_module.adapter.OrderDetailsCartListAdapter
import com.bistrocart.main_module.model.req.OrderDetailsReq
import com.bistrocart.main_module.model.res.BasicRes
import com.bistrocart.main_module.model.res.OrderDetailsRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MyOrderDetailsActivity : BaseActivity() {
    private val TAG = "MyOrderDetailsActivity"
    lateinit var binding: ActivityMyOrderDetailsBinding
    private lateinit var orderDetailsCartListAdapter: OrderDetailsCartListAdapter
    private lateinit var myOrderStatusAdapter: MyOrderStatusAdapter
    private val myCartList = ArrayList<OrderDetailsRes.Data.OrderItem>()
    private val myOrderStatusList = ArrayList<OrderDetailsRes.Data.OrderStatu>()
    lateinit var orderId: String
     var isFromNotification:Boolean =false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_order_details)
        initView()
        setOrderCardList()
        setOrderStatus()
    }

    private fun setOrderStatus() {
        myOrderStatusAdapter = MyOrderStatusAdapter(myOrderStatusList)
        binding.rvOrderStatus.adapter = myOrderStatusAdapter
    }

    private fun setOrderCardList() {
        orderDetailsCartListAdapter = OrderDetailsCartListAdapter(myCartList, false)
        binding.rvOrderDetails.adapter = orderDetailsCartListAdapter
        binding.rvOrderDetails.setHasFixedSize(true)
    }

    private fun initView() {
      //  if (intent != null) {
            orderId = intent.getStringExtra("order_id").toString()
           // intent.getBooleanExtra("notification",false)
            isFromNotification =intent.getBooleanExtra("notification",false)
            orderDetails(orderId)
            Log.e(TAG, "initView: "+orderId)
      //  }

        binding.toolbar.imgBack.setOnClickListener(View.OnClickListener {
           onBackPressed()
        })
        binding.toolbar.title.text = "Order Details"


        binding.btnOrderReturn.setOnClickListener(View.OnClickListener {
            orderReturn(this@MyOrderDetailsActivity)
        })

        binding.btnCancelOrder.setOnClickListener(View.OnClickListener {
            orderCancel(this@MyOrderDetailsActivity)
        })

    }

    @SuppressLint("MissingInflatedId")
    private fun orderCancel(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_cancle, null)
        builder.setView(dialogView)

        val ivCancel = dialogView.findViewById<AppCompatImageView>(R.id.ivCancel)
        val btnDone = dialogView.findViewById<AppCompatButton>(R.id.btnDone)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnDone.setOnClickListener {
            cancelOrder()
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("CheckResult")
    private fun orderDetails(orderId: String) {
        myCartList.clear()
        myOrderStatusList.clear()
        if (!isOnline()) {
            return
        }

        showLoading("loading")
        var req = OrderDetailsReq(orderId)
        apiService?.orderDetails(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<OrderDetailsRes> ->
                    AppLog.e(TAG, "OrderDetailsRes: $t")
                    AppLog.e(TAG, "OrderDetailsRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        myCartList.addAll(t.body()!!.data.get(0).order_items)
                        myOrderStatusList.addAll(t.body()!!.data.get(0).order_status)
                        myOrderStatusAdapter!!.notifyDataSetChanged()
                        orderDetailsCartListAdapter!!.notifyDataSetChanged()

                        binding.tvSubTotal.text = t.body()!!.data.get(0).amount
                        binding.tvDiscount.text = t.body()!!.data.get(0).discount
                        binding.tvDeliveryCharge.text = t.body()!!.data.get(0).deliverycharge
                        binding.tvTotal.text = t.body()!!.data.get(0).total
                        if (t.body()!!.data.get(0).payment_method.equals("online")){
                            binding.ivPaymentType.setImageResource(R.drawable.ic_online_bg)
                        }else{
                            binding.ivPaymentType.setImageResource(R.drawable.ic_cod_bg)
                        }
                     //   binding.tvPaytype.text =t.body()!!.data.get(0).payment_method

                        if (t.body()!!.data.get(0).is_cancellable){
                          binding.btnCancelOrder.visibility =View.VISIBLE
                        }else{
                            binding.btnCancelOrder.visibility =View.GONE
                        }

                        if (t.body()!!.data.get(0).is_returnable){
                            binding.btnOrderReturn.visibility =View.VISIBLE
                        }else{
                            binding.btnOrderReturn.visibility =View.GONE
                        }

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

    private fun orderReturn(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_return, null)
        builder.setView(dialogView)

        val ivCancel = dialogView.findViewById<AppCompatImageView>(R.id.ivCancel)
        val btnDone = dialogView.findViewById<AppCompatButton>(R.id.btnDone)
        val editReason = dialogView.findViewById<EditText>(R.id.editReason)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        ivCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDone.setOnClickListener {
            if (!editReason.text.isNullOrEmpty()){
                returnOrder(editReason.text.toString().trim())
                dialog.dismiss()
            }else{
                showToast("Reason not added")
            }
        }
        dialog.show()
    }



    //order return
    @SuppressLint("CheckResult")
    private fun returnOrder(reason: String) {
        if (!isOnline()) {
            return
        }
        showLoading("loading")
        apiService?.returnOrder(orderId, reason, "return")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<BasicRes> ->
                    AppLog.e(TAG, "updateStatus: $t")
                    AppLog.e(TAG, "updateStatus.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        orderDetails(orderId)
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

    //order cancel
    @SuppressLint("CheckResult")
    private fun cancelOrder() {
        if (!isOnline()) {
            return
        }
        showLoading("loading")
        apiService?.cancelOrder(orderId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<BasicRes> ->
                    AppLog.e(TAG, "cancelOrder Status: $t")
                    AppLog.e(TAG, "cancelOrder.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        orderDetails(orderId)
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


    override fun onBackPressed() {
        super.onBackPressed()
        if (isFromNotification == true){
            gotoActivity(HomeScreenActivity::class.java,null,true)
        }else{
           finish()
        }
    }
}