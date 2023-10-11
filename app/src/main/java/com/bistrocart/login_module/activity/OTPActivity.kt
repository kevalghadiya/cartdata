package com.bistrocart.login_module.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType.TYPE_CLASS_NUMBER
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityOtpactivityBinding
import com.bistrocart.login_module.model.req.LoginReq
import com.bistrocart.login_module.model.res.LoginRes
import com.bistrocart.login_module.model.res.SignUpRes
import com.bistrocart.main_module.activity.HomeScreenActivity
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.bistrocart.utils.ImageUtils
import com.goodiebag.pinview.Pinview
import com.google.android.play.core.integrity.e
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS

class OTPActivity : BaseActivity() {
    private val TAG = "OTPActivity"
    private lateinit var binding: ActivityOtpactivityBinding
    private var verificationId: String? = null
    private lateinit var auth: FirebaseAuth

    //sign in
    lateinit var otp: String
    lateinit var num: String

    //signup
    lateinit var name: String
    lateinit var email: String
    lateinit var phone_number: String
    lateinit var outlet_name: String
    lateinit var address: String
    lateinit var location: String
    lateinit var lat: String
    lateinit var long: String
    lateinit var city_id: String
    lateinit var pincode: String
    lateinit var pan_number: String
    lateinit var document: String
    lateinit var doc_number: String
    lateinit var imgPanCardUri: String
    lateinit var imgGSTUri: String
    lateinit var is_sign_up: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        initView()
        startTimer()

        if (intent != null) {
            //sign in
            num = intent.getStringExtra("phone_number").toString()
            otp = intent.getStringExtra("otp").toString()
            Log.e(TAG, "onCreate:$otp$num")

//            val phoneNumber = intent.getStringExtra("phone_number") ?: ""
            sendOtp("+91"+num)
            Log.e(TAG, "number1: "+"+91"+num)

            binding.txtOTPNumber.text = num

            //signup
            name = intent.getStringExtra("name").toString()
            email = intent.getStringExtra("email").toString()
            phone_number = intent.getStringExtra("phone_number").toString()
            outlet_name = intent.getStringExtra("outlet_name").toString()
            address = intent.getStringExtra("address").toString()
            location = intent.getStringExtra("location").toString()
            lat = intent.getStringExtra("lat").toString()
            long = intent.getStringExtra("long").toString()
            city_id = intent.getStringExtra("city_id").toString()
            pincode = intent.getStringExtra("pincode").toString()
            pan_number = intent.getStringExtra("pan_number").toString()
            document = intent.getStringExtra("document").toString()
            doc_number = intent.getStringExtra("doc_number").toString()
            imgPanCardUri = intent.getStringExtra("pan_image").toString()
            imgGSTUri = intent.getStringExtra("gst_image").toString()
            is_sign_up = intent.getStringExtra("is_sign_up").toString()
        }
    }



    private fun initView() {
//        binding.layoutOtpView.pinViewt.setInputType(Pinview.InputType.NUMBER);
      /*  binding.btnContinue.setOnClickListener {
            if (otp.equals(binding.layoutOtpView.pinViewt.value) && otp != null) {
                if (is_sign_up.equals("true")) {
                    signUp()
                } else {
                    gotoActivity(HomeScreenActivity::class.java, null, true)
                }
            } else {
                showSnackBar(binding.root, "OTP does not match")
            }

        }*/

        binding.btnContinue.setOnClickListener {
            val otp = binding.layoutOtpView.pinViewt.value
            if (otp.isNotEmpty() && verificationId != null) {
                verifyOtp(otp)
            } else {
                showSnackBar(binding.root, "Something went wrong, maybe the verificationId did not come.")
            }
        }

        binding.otpResent.setOnClickListener(View.OnClickListener {
            sendOtp("+91"+num)
            startTimer()
        })
    }

    private fun sendOtp(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
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
                    this@OTPActivity.verificationId = verificationId
                    Log.e(TAG, "onCodeSent: "+verificationId)
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
                    Log.e(TAG, "signInWithPhoneAuthCredential: "+user!!.phoneNumber)
                    Log.e(TAG, "signInWithPhoneAuthCredential: "+user!!.displayName)
                    Log.e(TAG, "signInWithPhoneAuthCredential: "+user!!.toString())

                    Log.e(TAG, "otp: "+otp)
                    Log.e(TAG, "otp2: "+binding.layoutOtpView.pinViewt.value)

                //    if (otp.equals(binding.layoutOtpView.pinViewt.value) && otp != null) {
                        if (is_sign_up.equals("true")) {
                            signUp()
                        } else {
                            login()
                        }
                   /* } else {
                        showSnackBar(binding.root, "OTP does not match")
                    }*/
                } else {
                    // Verification failed.
                 //   showSnackBar(binding.root, "OTP verification failed. Please try again.")
                    showSnackBar(binding.root, "${task.exception.toString()}")
                    Log.e(TAG, "signInWithPhoneAuthCredentialFail----->>: "+{task.exception!!.message.toString()})
                    Log.e(TAG, "signInWithPhoneAuthCredentialFail----->>: "+{task.exception!!.toString()})
                    Log.e(TAG, "signInWithPhoneAuthCredentialFail----->>: "+{task.exception!!})
                    Log.e(TAG, "signInWithPhoneAuthCredentialFail----->>: "+{task.exception!!.printStackTrace()})
                }
            }
    }

    @SuppressLint("CheckResult")
    private fun signUp() {
        if (!isOnline()) {
            return
        }

        val name: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            name
        )
        val email: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            email
        )
        val phone_number: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            phone_number
        )
        val outlet_name: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            outlet_name
        )
        val address: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            address
        )

         val location: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
             location
        )

        val lat: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), lat)
        val long: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), long)
        val city_id: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            city_id
        )
        val pincode: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            pincode
        )
        val pan_number: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            pan_number
        )
        val document: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), document)
        val doc_number: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            doc_number
        )

        val device_type: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            "Android"
        )

        val device_token: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            appPref!!.getString(AppPref.FMCTOKEN).toString()
        )


        val nameBuffer = Buffer()
        name.writeTo(nameBuffer)
        val nameValue = nameBuffer.readUtf8()

        val emailBuffer = Buffer()
        email.writeTo(emailBuffer)
        val emailValue = emailBuffer.readUtf8()

        val phoneNumberBuffer = Buffer()
        phone_number.writeTo(phoneNumberBuffer)
        val phoneNumberValue = phoneNumberBuffer.readUtf8()

        val outletNameBuffer = Buffer()
        outlet_name.writeTo(outletNameBuffer)
        val outletNameValue = outletNameBuffer.readUtf8()

        val addressBuffer = Buffer()
        address.writeTo(addressBuffer)
        val addressValue = addressBuffer.readUtf8()

        val latBuffer = Buffer()
        lat.writeTo(latBuffer)
        val latValue = latBuffer.readUtf8()

        val longBuffer = Buffer()
        long.writeTo(longBuffer)
        val longValue = longBuffer.readUtf8()

        val cityIdBuffer = Buffer()
        city_id.writeTo(cityIdBuffer)
        val cityIdValue = cityIdBuffer.readUtf8()

        val pincodeBuffer = Buffer()
        pincode.writeTo(pincodeBuffer)
        val pincodeValue = pincodeBuffer.readUtf8()

        val panNumberBuffer = Buffer()
        pan_number.writeTo(panNumberBuffer)
        val panNumberValue = panNumberBuffer.readUtf8()

        val documentBuffer = Buffer()
        document.writeTo(documentBuffer)
        val documentValue = documentBuffer.readUtf8()

        val doc_number1 = Buffer()
        document.writeTo(doc_number1)
        val doc_numberValue = doc_number1.readUtf8()

        
        val tag = "MY_TAG"
        Log.e(tag, "name: $nameValue")
        Log.e(tag, "email: $emailValue")
        Log.e(tag, "phone_number: $phoneNumberValue")
        Log.e(tag, "outlet_name: $outletNameValue")
        Log.e(tag, "address: $addressValue")
        Log.e(tag, "lat: $latValue")
        Log.e(tag, "long: $longValue")
        Log.e(tag, "city_id: $cityIdValue")
        Log.e(tag, "pincode: $pincodeValue")
        Log.e(tag, "pan_number: $panNumberValue")
        Log.e(tag, "document: $documentValue")
        Log.e(tag, "doc_number: $doc_numberValue")


        var pan_image: MultipartBody.Part? = null
        var gst_image: MultipartBody.Part? = null

        try {
            if (imgPanCardUri != null && imgPanCardUri!="") {

                val filefront = File(getPath(Uri.parse(imgPanCardUri)))
                AppLog.e("File_Name", filefront.name)

                val data: ByteArray = readBytes(
                    ImageUtils.getInputStreamByUri(
                        this,
                        Uri.parse(imgPanCardUri)
                    )!!
                )!!
                val fileBodyfront: RequestBody =
                    RequestBody.create("image/*".toMediaTypeOrNull(), data)
                pan_image = MultipartBody.Part.createFormData(
                    "pan_image", filefront.name,
                    fileBodyfront
                )
            } else {
                pan_image = MultipartBody.Part.createFormData(
                    "pan_image", "default_pan_image.jpg",
                    RequestBody.create("image/*".toMediaTypeOrNull(), ByteArray(0))
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }


        try {
            val filefront = File(getPath(Uri.parse(imgGSTUri)))
            AppLog.e("File_Name", filefront.name)

            val data: ByteArray = readBytes(
                ImageUtils.getInputStreamByUri(
                    this,
                    Uri.parse(imgGSTUri)
                )!!
            )!!
            val fileBodyfront: RequestBody =
                RequestBody.create("image/*".toMediaTypeOrNull(), data)
            gst_image = MultipartBody.Part.createFormData(
                "doc_image", filefront.name,
                fileBodyfront
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val panImageRequestBody = pan_image?.body
        val panImageValue = panImageRequestBody?.toString()

        val gstImageRequestBody = gst_image?.body
        val gstImageValue = gstImageRequestBody?.toString()
        Log.d("TAG", "pan_image: $panImageValue")
        Log.d("TAG", "gst_image: $gstImageValue")

        /*    Log.e(TAG, "signUpReq: "+ name+ email+
                    phone_number +"-->/n"+
                    outlet_name  +"-->/n"+
                    address      +"-->/n"+
                    lat          +"-->/n"+
                    long         +"-->/n"+
                    city_id      +"-->/n"+
                    pincode      +"-->/n"+
                    pan_number   +"-->/n"+
                    document     +"-->/n"+
                    gst_image    +"-->/n"+
                    pan_image)*/

        showLoading("loading")
        apiService?.getSignUp(
            name,
            email,
            phone_number,
            outlet_name,
            address,
            location,
            lat,
            long,
            city_id,
            pincode,
            pan_number,
            document,
            doc_number,
            device_type,
            device_token,
            gst_image,
            pan_image
        )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<SignUpRes> ->
                    Log.e(TAG, "signUp3: " + t.message())
                    AppLog.e(TAG, "loginRes: $t")
                    AppLog.e(TAG, "loginRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.body()!!.message)) {
                        showToast(t.body()?.message)
                        setData(t.body()?.data, t.body()!!.otp)
                    } else {
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                    Log.e(TAG, "signUp1: " + error.message)
                    Log.e(TAG, "signUp2: " + error.printStackTrace())
                    AppLog.e(TAG, "loginRes: $error")
                }
            )

    }

    private fun setData(data: SignUpRes.Data?, otp: Int) {
        if (data != null) {

            if (data.token != null && data.token!!.isNotEmpty()) {

                appPref?.set(AppPref.API_KEY, data.token)
                appPref?.set(AppPref.IS_LOGIN, true)
                appPref?.set(AppPref.NAME, data.name)
                appPref?.set(AppPref.USER_ID, data.id)
                appPref?.set(AppPref.ADDRESS, data.address)
                appPref?.set(AppPref.EMAIL, data.email)
                appPref?.set(AppPref.MOBILE_NO, data.phone_number)
                appPref?.set(AppPref.OUTLET_NAME, data.outlet_name)
                appPref?.set(AppPref.LOCATION, data.location)

                gotoActivity(HomeScreenActivity::class.java, null, false)
                finish()
            } else {

            }
        }
    }


    @SuppressLint("CheckResult")
    private fun login() {
        if (!isOnline()) {
            return
        }

        var loginReq = LoginReq(phone_number,"Android",appPref!!.getString(AppPref.FMCTOKEN).toString())
        Log.e(TAG, "loginReq--->: "+Gson().toJson(loginReq))
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
                        setData(t.body()?.data, t.body()!!.otp)
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
                appPref?.set(AppPref.MOBILE_NO, data.phone_number)
                appPref?.set(AppPref.OUTLET_NAME, data.outlet_name)
                appPref?.set(AppPref.LOCATION, data.location)

                gotoActivity(HomeScreenActivity::class.java, null, true)
            } else {

            }
        }
    }


    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startTimer() {
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondText = if ((millisUntilFinished % 60000) / 1000 < 10)
                    "0" + (millisUntilFinished % 60000) / 1000
                else
                    "" + (millisUntilFinished % 60000) / 1000

                binding.tvTiming.text = "Wait On 0" + millisUntilFinished / 60000 + ":" + secondText
                binding.otpResent.isClickable = false
                binding.otpResent.setTextColor(getColor(R.color.darkGray))
            }

            override fun onFinish() {
                //binding.tvTiming.text = resources.getText(R.string.resend_otp)
                binding.otpResent.isClickable = true
                binding.otpResent.visibility = View.VISIBLE
                binding.otpResent.setTextColor(getColor(R.color.green))
            }
        }
        timer.start()
    }
}