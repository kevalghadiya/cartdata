package com.bistrocart.base

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.Network.RetrofitClient
import com.bistrocart.login_module.activity.SignInActivity
import com.bistrocart.login_module.activity.Sign_in_or_up_Activity
import com.bistrocart.main_module.activity.OrderSummaryActivity
import com.bistrocart.main_module.model.res.MyCartRes
import com.bistrocart.utils.AppDialog
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.google.android.material.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.retailer.Network.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import java.security.MessageDigest
import java.util.Base64.getEncoder
import java.util.concurrent.TimeUnit

open class BaseActivity : AppCompatActivity() {

    companion object {
        var apiService: ApiService? = null
        var appPref: AppPref? = null
    }

    var dialog: ProgressDialog? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = RetrofitClient.getApiService(httpClient)
        appPref = AppPref.getInstance(this)!!
    }


   /* val httpClient: OkHttpClient
        get() = OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).addInterceptor { chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + appPref?.getString(AppPref.API_KEY))

                Log.e("Authorization", "Bearer " + appPref?.getString(AppPref.API_KEY))
                val newRequest = builder.build()
                chain.proceed(newRequest)
            }
            .build()*/

    val httpClient: OkHttpClient
        get() {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                // Log the request and response messages
                Log.d("OkHttp", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            return OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor) // Add the logging interceptor
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val builder = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + appPref?.getString(AppPref.API_KEY))

                    Log.e("Authorization", "Bearer " + appPref?.getString(AppPref.API_KEY))
                    val newRequest = builder.build()
                    chain.proceed(newRequest)
                }
                .build()
        }


    fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun isValidEmail(editText: EditText): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(editText.text.toString().trim())
                .matches()
        ) {
            editText.requestFocus()
            return false
        }
        return true
    }

    fun showSnackBar(view: View, msg: String) {
        val snackBar = Snackbar.make(
            view, msg,
            Snackbar.LENGTH_SHORT
        )
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(resources.getColor(com.bistrocart.R.color.green))
        val textView =
            snackBarView.findViewById(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        textView.textSize = 14f
        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackBar.show()
    }

   /*  open fun goToOrderSummary(context: Context){
        var bundle =Bundle()
        bundle.putString("isOnline","true")
        gotoActivity(OrderSummaryActivity::class.java,bundle,true)
    }*/

    fun gotoActivity(className: Class<*>?, bundle: Bundle?, isClearStack: Boolean) {
        val intent = Intent(this, className)
        if (bundle != null) intent.putExtras(bundle)
        if (isClearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    protected open fun isSuccess(res: Response<*>, baseRes: String): Boolean {
        if (res.code() == 200) {
            return true
        } else if (res.code() == 401) {
           // showToast("Login Again")
           // logout()
        } else {
            showToast(if (baseRes != null) baseRes else baseRes)
        }
        return false
    }

    open fun logout() {
        appPref?.clearSession()
        gotoActivity(Sign_in_or_up_Activity::class.java, null, true)
        finish()
    }

    open fun onFailure(msg: Throwable) {
        AppLog.e("TAG", "onFailure->$msg")
      //  showToast(getString(com.bistrocart.R.string.server_error))
        //       hideLoading()
    }

    open fun showLoading(msg: String) {
        runOnUiThread {
            if (dialog != null) hideLoading()
            if (dialog == null) {
                dialog = ProgressDialog(this@BaseActivity)
            }
            dialog!!.setMessage(if (!TextUtils.isEmpty(msg)) msg else "loading")
            if (!dialog!!.isShowing()) dialog!!.show()
        }
    }

    open fun hideLoading() {
        if (dialog != null && dialog!!.isShowing()) {
            dialog!!.dismiss()
        }
    }

    open fun isOnline(): Boolean {
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            isAvailable = true
        }
        if (!isAvailable) {
             AppDialog.showNoNetworkDialog(this)
        }
        return isAvailable
    }



    //For...check permission
    fun hasPermission(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) return false
            }
        }
        return true
    }

    open fun getPath(uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        val cursor: Cursor =
            getContentResolver()
                .query(uri, projection, null, null, null)
                ?: return null
        val column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }



    // open fun getNotification() {
    val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (isNotificationPermissionGranted()) {
                // Notification permission granted
                // Proceed with your logic here
            } else {
                // Notification permission not granted
                // Handle the case where the permission is not granted
            }
        }
    //  }


    /* override fun onResume() {
         super.onResume()
         if (!isNotificationPermissionGranted()) {
             requestNotificationPermission()
         }
     }*/

    fun isNotificationPermissionGranted(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            notificationPermissionRequest.launch(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Settings.ACTION_APPLICATION_DETAILS_SETTINGS.toUri())
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                .putExtra("android.provider.extra.APP_PACKAGE", packageName)
            notificationPermissionRequest.launch(intent)
        }
    }

    private fun openKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }


    @SuppressLint("CheckResult")
    open fun getTotalCount(callback: (Int) -> Unit) {
        try {
            BaseFragment.apiService?.myCartList()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                    { t: Response<MyCartRes> ->
                        if (isSuccess(t, t.message())) {
                            if (t.body()!!.status) {
                                var count =0
                                if (!t.body()!!.data.isNullOrEmpty()) {
                                     count = t.body()!!.data.size
                                    Log.e("TAG", "getTotalCount12: $count")
                                }
                                callback(count)
                            }
                        }
                    },
                    { error ->
                        hideLoading()
                    }
                )
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

/*// Example of how to use the getTotalCount() function:
    getTotalCount { count ->
        // Use the count value here or pass it to another function
        // For example, you can update a UI element with the count:
        updateUIWithCount(count)
    }*/


/*    @SuppressLint("CheckResult")
    open fun getTotalCount():Int{
        var count:Int? =0
            BaseFragment.apiService?.myCartList()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                    { t: Response<MyCartRes> ->
                        if (isSuccess(t, t.message())) {
                            if (t.body()!!.status){
                                 count = t.body()!!.data.size.toInt()
                                Log.e("TAG", "getTotalCount: "+t.body()!!.data.size )
                               // Log.e("TAG", "myCartList: " )
                            }
                        }
                    },
                    { error ->
                        hideLoading()
                    }
                )

        return count!!.toInt()
    }*/

}