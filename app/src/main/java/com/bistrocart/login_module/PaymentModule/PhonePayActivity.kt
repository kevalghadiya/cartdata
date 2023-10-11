package com.bistrocart.login_module.PaymentModule

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityPhonePayBinding
import com.bistrocart.main_module.activity.OrderSummaryActivity
import com.bistrocart.utils.AppPref
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.*

class PhonePayActivity : BaseActivity() {
    private val TAG = "PhonePayActivity"
    lateinit var binding: ActivityPhonePayBinding
    var amount: String? = ""
    var m_transaction_id =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_pay)
        initView()
        getPhonePeUrlWeb()
    }


    private fun initView() {
        binding.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        if (intent != null) {
            amount = intent?.extras?.getString("amount")
           // amount = intent.getStringExtra("amount")
            Log.e(TAG, "amount_is: "+amount)
        }
    }


    @SuppressLint("CheckResult")
    private fun getPhonePeUrlWeb() {
        if (!isOnline()) {
            return
        }
        showLoading("loading")
        var phonePeUrlReq = PhonePeUrlReq(
            amount!!.toInt(),
            appPref!!.getString(AppPref.MOBILE_NO)!!,
            appPref!!.getInt(AppPref.USER_ID).toString()
        )
        apiService?.getPhonePeUrlWeb(phonePeUrlReq)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<PhonePeUrlRes> ->
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        if (t.body()!!.status) {
                            var webUrl = t.body()!!.url.toString()
                            val redirectUrl =t.body()!!.redirect_url
                            m_transaction_id =t.body()!!.m_transaction_id
                            setLoadUrl(webUrl,redirectUrl)
                        }
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )
    }

    private fun setLoadUrl(webUrl: String, redirectUrl: String) {
        val webSetting: WebSettings = binding.webView.getSettings()
        webSetting.builtInZoomControls = false
        webSetting.javaScriptEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
      //  binding.webView.addJavascriptInterface(WebAppInterface(this), "android")
       /* binding.webView.webViewClient =object :WebViewClient(){
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
                Log.e(TAG, "shouldOverrideUrlLoading: $url" )
                // Return false to indicate that the WebView should handle the URL loading
                return false
            }
        }*/
        binding.webView.webViewClient = MyWebViewClient(redirectUrl)

        binding.webView.loadUrl(webUrl)

    }

    private inner class MyWebViewClient(redirectUrl: String) : WebViewClient() {
        var redirect =redirectUrl
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            // Check the URL and handle it differently if needed
            Log.e(TAG, "shouldOverrideUrlLoading: $url")
            Log.e(TAG, "shouldOverrideUrlLoadingRedirect:$redirect" )
            if (url != null && url.startsWith(redirect)) {
                // Do something specific for this URL, return true to indicate it's handled
                // For example, you can open the URL in the device's browser
                // or perform any other custom action
                Log.e(TAG, "shouldOverrideUrlLoadingSuccess:"+url )
                initiatePayment()
                return true
            }
            // Return false to indicate that the WebView should handle the URL loading
            return false
        }
    }

   class WebAppInterface(private val mActivity: PhonePayActivity) {
        companion object {
            private const val TAG = "WebAppInterface"
        }
        /**
         * Show a toast from the web page
         */
        @android.webkit.JavascriptInterface
        fun showSuccess(toast: String) {
            showToast(toast)
            mActivity.initiatePayment()
            Log.e(TAG, "showSuccess123:" + toast)
        }

       @android.webkit.JavascriptInterface
        fun showCancel(toast: String) {
            showToast(toast)
        }

        @android.webkit.JavascriptInterface
        fun showError(toast: String) {
            showToast(toast)
        }
        private fun showToast(message: String) {
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    public fun initiatePayment() {
        Log.e(TAG, "initiatePayment: "+m_transaction_id )
       //  val resultData = "Result data from SecondActivity!" // Replace this with the data you want to send back to FirstActivity.
        val intent = Intent().apply {
            putExtra("m_transaction_id", m_transaction_id)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}