package com.bistrocart.login_module.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivitySignInBinding
import com.bistrocart.login_module.model.req.LoginReq
import com.bistrocart.login_module.model.res.LoginRes
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class SignInActivity : BaseActivity() {
    private val TAG = "SignInActivity"

    private lateinit var binding: ActivitySignInBinding
    var token: String = "ANDROID FCM"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

        initView()
    }

    private fun initView() {

        binding.btnLogin.setOnClickListener {
            if (binding.edtPhoneNumber.text!!.isNullOrEmpty() || binding.edtPhoneNumber.text!!.length<10) {
                showSnackBar(binding.root, resources.getString(R.string.enter_phone_number))
            } else {
                if (appPref!!.getString(AppPref.FMCTOKEN).isNullOrEmpty()) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener<String?> { task ->
                        if (task.isSuccessful) {
                            Log.e("Firebase",
                                "Fetching FCM registration token isSuccessful" + task.result.toString())
                            appPref?.set(AppPref.FMCTOKEN, task.result.toString())
                            login()
                        } else {
                            Log.e("Firebase",
                                "Fetching FCM registration token failed",
                                task.exception)
                        }
                    })
                } else {
                    login()
                }
            }
        }

    }

    @SuppressLint("CheckResult")
    private fun login() {
        if (!isOnline()) {
            return
        }

        var loginReq = LoginReq(binding.edtPhoneNumber.text.toString(),
            "Android",
            appPref!!.getString(AppPref.FMCTOKEN).toString())
        Log.e(TAG, "loginReq--->: " + Gson().toJson(loginReq))
        showLoading("loading")
        apiService?.login(loginReq)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<LoginRes> ->
                    AppLog.e(TAG, "loginRes: $t")
                    AppLog.e(TAG, "loginRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        Log.e(TAG, "login: "+t.body()!!.status )
                        if (t.body()!!.status){

                            if (t.body()!!.user_status.equals("block")){
                              showToast(t.body()!!.message)
                            }else {
                                Log.e(TAG, "login1: " + t.body()!!.status)
                                var bundle = Bundle()
                                bundle.putString("phone_number", binding.edtPhoneNumber.text.toString())
                                gotoActivity(OTPActivity::class.java, bundle, false)
                            }
                        }else{
                            Log.e(TAG, "login2: "+t.body()!!.status )
                            gotoActivity(SignUpActivity::class.java, null, false)
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

    private fun setData(data: LoginRes.Data?, otp: Int) {
        if (data != null) {

            if (data.token != null && data.token!!.isNotEmpty()) {

                appPref?.set(AppPref.API_KEY, data.token)
                appPref?.set(AppPref.IS_LOGIN, true)
                appPref?.set(AppPref.NAME, data.name)
                appPref?.set(AppPref.USER_ID, data.id)
                appPref?.set(AppPref.ADDRESS, data.address)
                appPref?.set(AppPref.EMAIL, data.email)
                appPref?.set(AppPref.OUTLET_NAME, data.outlet_name)
                Log.e(TAG, "outlet_name--: "+data.outlet_name )

                var bundle = Bundle()
                bundle.putString("phone_number", binding.edtPhoneNumber.text.toString())
                bundle.putString("otp", otp.toString())
                gotoActivity(OTPActivity::class.java, bundle, false)
            } else {

            }
        }
    }

}