package com.bistrocart.base

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.Network.RetrofitClient
import com.bistrocart.R
import com.bistrocart.login_module.activity.SignInActivity
import com.bistrocart.login_module.activity.Sign_in_or_up_Activity
import com.bistrocart.utils.AppDialog
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.google.android.material.snackbar.Snackbar
import com.retailer.Network.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import java.io.InputStream
import java.util.concurrent.TimeUnit

open class BaseFragment : Fragment() {
    companion object {
        var apiService: ApiService? = null
        var appPref: AppPref? = null
    }
    private val TAG: String = "BaseFragment"
    private var inputStream: InputStream? = null
    var progressdialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = RetrofitClient.getApiService(httpClient)
        appPref = AppPref.getInstance(activity)!!
    }

    val httpClient: OkHttpClient
        get() {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                // Log the request and response messages
                AppLog.d("OkHttp", message)
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

                    AppLog.e("Authorization", "Bearer " + appPref?.getString(AppPref.API_KEY))
                    val newRequest = builder.build()
                    chain.proceed(newRequest)
                }
                .build()
        }


    fun showToast(msg: String?) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
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
        snackBar.setActionTextColor(Color.RED)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(Color.LTGRAY)
        val textView =
            snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.BLUE)
        textView.textSize = 28f
        snackBar.show()
    }

    fun gotoActivity(className: Class<*>?, bundle: Bundle?, isClearStack: Boolean) {
        val intent = Intent(context, className)
        if (bundle != null) intent.putExtras(bundle)
        if (isClearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    open fun isOnline(): Boolean {
        val manager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            isAvailable = true
        }
        if (!isAvailable) {
            AppDialog.showNoNetworkDialog(requireContext())
        }
        return isAvailable
    }

    open fun showLoading(msg: String) {
        requireActivity().runOnUiThread {
            if (progressdialog != null) hideLoading()
            if (progressdialog == null) {
                progressdialog = ProgressDialog(requireContext())
            }
            progressdialog!!.setMessage(if (!TextUtils.isEmpty(msg)) msg else resources.getString(R.string.loading))
            if (!progressdialog!!.isShowing()) progressdialog!!.show()
        }
    }

    fun hideLoading() {
        if (progressdialog != null && progressdialog!!.isShowing()) {
            progressdialog!!.dismiss()
        }
    }

    protected open fun isSuccess(res: Response<*>, baseRes: String): Boolean {
        if (res.code() == 200) {
            return true
        } else if (res.code() == 401) {
            showToast("Login Again")
            logout()
        } else {
            //   showToast("is success else")
            showToast(if (baseRes != null) baseRes else baseRes)
        }
        return false
    }

    open fun logout() {
        appPref?.clearSession()
        gotoActivity(SignInActivity::class.java, null, true)
        requireActivity().finish()
    }


    open fun onFailure(msg: Throwable) {
        AppLog.e("TAG", "onFailure->$msg")
      //  showToast(resources.getString(R.string.server_error))

        // showToast("onfailure")
    }

    fun getVersionName(context: Context): String {
        var versionName = ""
        try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }

}