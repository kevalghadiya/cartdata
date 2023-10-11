package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityMyProfileBinding
import com.bistrocart.databinding.DialogOtpVerificationBinding
import com.bistrocart.login_module.activity.SignUpActivity
import com.bistrocart.login_module.model.res.ProfileRes
import com.bistrocart.main_module.model.req.UpdateMyProfileReq
import com.bistrocart.main_module.model.res.BasicRes
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.bistrocart.utils.ImageUtils
import com.bistrocart.utils.LocationActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.concurrent.TimeUnit

class MyProfileActivity : BaseActivity() {
    private val TAG = "MyProfileActivity"
    lateinit var binding: ActivityMyProfileBinding
    var phoneNum: String? = null
    private var verificationId: String? = null
    private lateinit var auth: FirebaseAuth
    lateinit var dialogOtpBinding: DialogOtpVerificationBinding
    var lat: Double = 0.0
    var lng: Double = 0.0
    var location:String? =null
    var address:String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_profile)
        auth = FirebaseAuth.getInstance()
        initView()
    }

    private fun initView() {
        binding.toolbar.title.text = "My Profile"
        binding.toolbar.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        binding.btnSave.setOnClickListener(View.OnClickListener {
            if (binding.edtName.text.isNullOrEmpty()) {
                showSnackBar(binding.root, resources.getString(R.string.name))
            } else if (binding.edtPhoneNumber.text!!.isNullOrEmpty() || binding.edtPhoneNumber.text!!.length < 10) {
                showSnackBar(binding.root, resources.getString(R.string.enter_phone_number))
            } else if (binding.edtEmail.text.isNullOrEmpty()) {
                showSnackBar(binding.root, resources.getString(R.string.email_hint))
            } else {
                updateProfileDialog(this)
            }
        })
        getProfile()

        binding.edtAddress.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            intent.putExtra("address",address)
            intent.putExtra("location",location)
            startActivityForResult(intent, 200)
        })
    }

    @SuppressLint("MissingInflatedId")
    private fun updateProfileDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_cancle, null)
        builder.setView(dialogView)

        val ivCancel = dialogView.findViewById<AppCompatImageView>(R.id.ivCancel)
        val btnDone = dialogView.findViewById<AppCompatButton>(R.id.btnDone)
        val tvTitle = dialogView.findViewById<AppCompatTextView>(R.id.tvTitle)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        tvTitle.setText("Are you sure want to update profile?")
        ivCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDone.setOnClickListener {
            if (phoneNum!! == binding.edtPhoneNumber.text.toString()) {
                dialog.dismiss()
                getUpdateProfile()
            } else {
                getOTPDialog()
                sendOtp("+91" + binding.edtPhoneNumber.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @SuppressLint("CheckResult")
    private fun getUpdateProfile() {
        if (!isOnline()) {
            return
        }
        showLoading("loading")

        var updateMyProfile = UpdateMyProfileReq(
            binding.edtEmail.text.toString(),
            binding.edtName.text.toString(),
            binding.edtPhoneNumber.text.toString(),
            binding.edtAddress.text.toString(),
            lat.toString(),
            lng.toString(),
            location.toString()
        )
        apiService?.getUpdateProfile(updateMyProfile)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<BasicRes> ->
                    AppLog.e(TAG, "ProfileRes: $t")
                    AppLog.e(TAG, "ProfileRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        if (t.body()!!.status) {
                            appPref?.set(AppPref.ADDRESS, binding.edtAddress.text.toString())
                            showToast(t.body()!!.message)
                            onBackPressed()
                            finish()
                        } else {
                            getOTPDialog()
                            sendOtp("+91" + binding.edtPhoneNumber.text.toString())
                        }
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )

    }


    @SuppressLint("CheckResult")
    private fun getProfile() {
        if (!isOnline()) {
            return
        }
        showLoading("loading")
        apiService?.getProfile()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<ProfileRes> ->
                    AppLog.e(TAG, "ProfileRes: $t")
                    AppLog.e(TAG, "ProfileRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        var data = t.body()!!.data[0]
                        phoneNum = data.phone_number.toString()
                        binding.edtName.setText(data.name)
                        binding.edtPhoneNumber.setText(data.phone_number)
                        binding.edtEmail.setText(data.email)
                        binding.edtAddress.setText(data.address)
                        location =data?.location
                        address =data?.address
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )

    }


    private fun getOTPDialog() {
        val dialog: Dialog
        dialog = Dialog(this, R.style.AppDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogOtpBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.dialog_otp_verification,
                null,
                false
            )

        dialogOtpBinding.tvMobileNum.text = binding.edtPhoneNumber.text.toString()
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        val timer = object : CountDownTimer(60000, 1000) {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTick(millisUntilFinished: Long) {
                var secondText = ""
                if ((millisUntilFinished % 60000) / 1000 < 10)
                    secondText = "0" + (millisUntilFinished % 60000) / 1000
                else
                    secondText = "" + (millisUntilFinished % 60000) / 1000

                dialogOtpBinding.tvTiming.setText("Wait On 0" + millisUntilFinished / 60000 + ":" + secondText)
                dialogOtpBinding.otpResent.isClickable = false
                dialogOtpBinding.otpResent.setTextColor(getColor(R.color.black))
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onFinish() {
                // dialogOtpBinding.tvTiming.setText(resources.getText(R.string.resend_otp))
                dialogOtpBinding.otpResent.isClickable = true
                dialogOtpBinding.otpResent.visibility = View.VISIBLE
                dialogOtpBinding.otpResent.setTextColor(getColor(R.color.multiDarkGray))
            }
        }
        timer.start()

        dialog.setContentView(dialogOtpBinding.getRoot())


        dialogOtpBinding.tvConform.setOnClickListener {
            val otp = dialogOtpBinding.pinViewt.value
            if (otp.isNotEmpty() && verificationId != null) {
                verifyOtp(otp)
                dialog.dismiss()
            } else {
                showSnackBar(
                    binding.root,
                    "Something went wrong, maybe the verificationId did not come."
                )
            }
        }

        dialogOtpBinding.otpResent.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                sendOtp("+91" + binding.edtPhoneNumber.text.toString())
                timer.start()
            }
        })

        dialog.show()
    }


    private fun sendOtp(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            120,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                    Log.e("OTPActivity", "Verification :")

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("OTPActivity", "Verification failed: ${e.message}")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verificationId, token)
                    this@MyProfileActivity.verificationId = verificationId
                    Log.e(TAG, "onCodeSent: " + verificationId)
                }
            }
        )
    }

    private fun verifyOtp(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.e(TAG, "signInWithPhoneAuthCredential: " + user!!.phoneNumber)
                    Log.e(TAG, "signInWithPhoneAuthCredential: " + user!!.displayName)
                    Log.e(TAG, "signInWithPhoneAuthCredential: " + user!!.toString())
                    getUpdateProfile()
                } else {
                    showToast(task.exception.toString())
                    Log.e(
                        TAG,
                        "signInWithPhoneAuthCredentialFail----->>: " + { task.exception!!.message.toString() })
                    Log.e(
                        TAG,
                        "signInWithPhoneAuthCredentialFail----->>: " + { task.exception!!.toString() })
                    Log.e(TAG, "signInWithPhoneAuthCredentialFail----->>: " + { task.exception!! })
                    Log.e(
                        TAG,
                        "signInWithPhoneAuthCredentialFail----->>: " + { task.exception!!.printStackTrace() })
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
          if (requestCode == 200 && data != null) {
            binding.edtAddress.text = data.getStringExtra("address")
              lat = data.getDoubleExtra("lat", 0.0)
              lng = data.getDoubleExtra("lng", 0.0)
              Log.e(TAG, "onActivityResult:$lat---$lng" )
          }
    }
}

