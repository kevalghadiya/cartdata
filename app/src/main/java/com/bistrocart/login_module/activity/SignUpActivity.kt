package com.bistrocart.login_module.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivitySignUpBinding
import com.bistrocart.login_module.model.req.LoginReq
import com.bistrocart.login_module.model.res.SignUpRes
import com.bistrocart.main_module.model.res.BasicRes
import com.bistrocart.main_module.model.res.CityListRes
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.bistrocart.utils.ImageUtils
import com.bistrocart.utils.ImageUtils.getInputStreamByUri
import com.bistrocart.utils.LocationActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SignUpActivity : BaseActivity() {
    private val TAG = "SignUpActivity"
    private lateinit var binding: ActivitySignUpBinding
    var imgPanCardUri: String? = ""
    var imgGSTUri: String? = ""
    private var imageCaptureFile: Uri? = null
    lateinit var type: String
    var provideDocIs: String = "GSTIN"
    var otp: String? = null
    private var CityID: String? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
    var token:String ="ANDROID FCM"

    val cityDataArrayList: ArrayList<CityListRes.Data> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        getCityList()
        setPanCardImage()

        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }

    private fun initView() {
        binding.btnSignUp.setOnClickListener {
            if (isValid()) {
                if (appPref!!.getString(AppPref.FMCTOKEN).isNullOrEmpty()) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener<String?> { task ->
                        if (task.isSuccessful) {
                            Log.e("Firebase", "Fetching FCM registration token isSuccessful"+ task.result.toString())
                            appPref?.set(AppPref.FMCTOKEN, task.result.toString())
                            getSignUpOTP()
                        }else{
                            Log.e("Firebase", "Fetching FCM registration token failed", task.exception)
                        }
                    })
                }else{
                    getSignUpOTP()
                }

            }
        }

        // Set a listener for the RadioGroup
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGST -> {
                    // radioGST is selected
                    provideDocIs = binding.radioGST.text.toString()
                    binding.btnUploadGstinCF.text = getString(R.string.upload_gstin_certificate)
                    binding.edtGstinorFssaiNumber.hint ="GSTIN*"
                    // Use the selectedValue variable as needed
                }
                R.id.radioFssai -> {
                    // radioFssai is selected
                    provideDocIs = binding.radioFssai.text.toString()
                    binding.btnUploadGstinCF.text = getString(R.string.upload_fssai_certificate)
                    binding.edtGstinorFssaiNumber.hint ="FSSAI*"

                    // Use the selectedValue variable as needed
                }
            }
        }

        binding.edtLocation.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
           // intent.putExtra("isRegister","true")
            startActivityForResult(intent, 200)
        })

        binding.cityNameLayout.setOnClickListener(View.OnClickListener {
            if (cityDataArrayList != null) showPopup()
        })

        binding.ivClearPanCard.setOnClickListener(View.OnClickListener {
            binding.imgPanCard.setImageDrawable(null)
            binding.cardPanCard.visibility =View.GONE
            imgPanCardUri =""
        })

        binding.ivClearGST.setOnClickListener(View.OnClickListener {
            binding.imgGstinOrFssai.setImageDrawable(null)
            binding.CardGST.visibility =View.GONE
            imgGSTUri = ""
        })
    }


    private fun showPopup() {
        val menu = PopupMenu(this@SignUpActivity, binding.txtCityName)
        for (i in cityDataArrayList!!.indices) {
            menu.menu.add(cityDataArrayList!!.get(i).city_name)
        }
        menu.setOnMenuItemClickListener { item: MenuItem ->
            Log.e(TAG, "item" + item.title)
            for (i in cityDataArrayList!!.indices) {
                if (cityDataArrayList.get(i).city_name === item.title) {
                    CityID = java.lang.String.valueOf(cityDataArrayList.get(i).id)
                }
            }
            Log.e(TAG, "CityID$CityID")
            binding.edtCityName.setText(item.title.toString())
            true
        }
        menu.show()
    }


    @SuppressLint("CheckResult", "NotifyDataSetChanged")
    private fun getSignUpOTP() {
        if (!isOnline()) {
            return
        }

        showLoading("loading")
     //   var req = (LoginReq(binding.edtPhoneNumber.text.toString()))
        apiService?.getSignUpOTP(binding.edtPhoneNumber.text.toString())?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t: Response<BasicRes> ->
                AppLog.e(TAG, "getSignUpOTP: $t")
                AppLog.e(TAG, "getSignUpOTP.body(): " + Gson().toJson(t.body()))
                hideLoading()
                if (isSuccess(t, t.message())) {

                    if (t.body()!!.status) {
                        var bundle = Bundle()
                        bundle.putString("name", binding.edtName.text.toString())
                        bundle.putString("email", binding.edtEmail.text.toString())
                        bundle.putString("phone_number", binding.edtPhoneNumber.text.toString())
                        bundle.putString("outlet_name", binding.edtOutletName.text.toString())
                        bundle.putString("address",binding.edtAddress.text.toString())
                        bundle.putString("location",binding.edtLocation.text.toString())
                        bundle.putString("lat", lat.toString())
                        bundle.putString("long", lng.toString())
                        bundle.putString("city_id", CityID)
                        bundle.putString("pincode", binding.edtPincode.text.toString())
                        bundle.putString("pan_number", binding.edtPanNumber.text.toString())
                        bundle.putString("document", provideDocIs)
                        bundle.putString(
                            "doc_number",
                            binding.edtGstinorFssaiNumber.text.toString()
                        )
                        bundle.putString("pan_image", imgPanCardUri)
                        bundle.putString("gst_image", imgGSTUri)
                        bundle.putString("is_sign_up", "true")
                        bundle.putString("otp", t.body()!!.otp.toString())

                        gotoActivity(OTPActivity::class.java, bundle, false)
                    }else{
                        showToast(t.body()!!.message)
                    }
                } else {
                    showToast(t.body()?.message)
                    Log.e(TAG, "OTP::: "+t.body()?.message )
                }
            }, { error ->
                hideLoading()
                onFailure(error)
            })
    }

    private fun isValid(): Boolean {

        if (binding.edtName.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter Name")
            return false
        }

        if (binding.edtPhoneNumber.text!!.isNullOrEmpty() || binding.edtPhoneNumber.text!!.length<10) {
            showSnackBar(binding.root, "Enter Phone Number")
            return false
        }

        /*    if (binding.edtPhoneNumber.text.toString().length != 10) {
                showSnackBar(binding.root,"Enter Valid Phone Number")
                return false
            }*/

        if (binding.edtEmail.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter Email")
            return false
        }

        if (!isValidEmail(binding.edtEmail)) {
            showSnackBar(binding.root, "Enter Valid Email")
            return false
        }

        if (binding.edtOutletName.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter OutletName")
            return false
        }

        if (binding.edtLocation.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter location")
            return false
        }

        if (binding.edtAddress.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter Address")
            return false
        }

        if (binding.edtCityName.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter City Name")
            return false
        }

        if (binding.edtPincode.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter Pin code")
            return false
        }

        /*if (binding.edtPincode.text.toString().length != 6) {
            showSnackBar(binding.root,"Enter Valid Pin code")
            return false
        }*/

     /*   if (binding.edtPanNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter Pan Number")
            return false
        }*/

        /*if (binding.edtPanNumber.text.toString().length != 10) {
            showSnackBar(binding.root,"Enter Valid Pan Number")
            return false
        }*/

        if (binding.edtGstinorFssaiNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Enter GST Number")
            return false
        }
      /*  if (imgPanCardUri.equals("")) {
            showSnackBar(binding.root, "Add PAN card image")
            return false
        }*/

        if (imgGSTUri.equals("")) {
            showSnackBar(binding.root, "Add GST certificate")
            return false
        }

        /*  if (binding.edtGstinorFssaiNumber.text.toString().length != 15) {
              showSnackBar(binding.root,"Enter Valid GST Number")
              return false
          }*/

        return true
    }

    private fun setPanCardImage() {
        binding.btnUploadPancard.setOnClickListener {
            type = "1"
            selectImage()
        }

        binding.btnUploadGstinCF.setOnClickListener(View.OnClickListener {
            type = "2"
            selectImage()
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101, 102 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            }
        }
    }

    private fun selectImage() {
        val items =
            arrayOf<CharSequence>(getString(R.string.lbl_camera), getString(R.string.lbl_gallery))
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Documents")
        builder.setItems(items, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, item: Int) {
                if (items[item] == getString(R.string.lbl_camera)) {
                    cameraIntent()
                } else if (items[item] == getString(R.string.lbl_gallery)) {
                    gallryIntent()
                }
            }
        })
        builder.show()
    }


    fun cameraIntent() {
        if (hasPermission(arrayOf(Manifest.permission.CAMERA))) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            imageCaptureFile = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                FileProvider.getUriForFile(this, getPackageName() + ".provider", outputMediaFile)
            } else {
                Uri.fromFile(outputMediaFile)
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureFile)
            startActivityForResult(intent, CAMERA_PIC_REQUEST)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_CAMERA)
            } else {
                Toast.makeText(this, "Camera11", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gallryIntent() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.setType("image/*")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(galleryIntent,
            getString(R.string.select_picture)), GALLERY_PIC_REQUEST)
    }

    val outputMediaFile: File
        get() {
            val mediaStorageDir: File = getCacheDir()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            var mediaFile: File? = null
            mediaFile = File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
            return mediaFile
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PIC_REQUEST && resultCode == RESULT_OK) {
            val selectedImage: Uri = data?.getData()!!
            val clipData: ClipData? = data?.getClipData()
            if (clipData == null) {
                cropImage(selectedImage)
            } else {
                for (i in 0 until clipData.getItemCount()) {
                    val item: ClipData.Item = clipData.getItemAt(i)
                    val uri1: Uri = item.getUri()
                    cropImage(uri1)
                }
            }
        } else if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            cropImage(imageCaptureFile)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            Log.e("TAG", "result Uri Crop Image " + result.getUri())
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.getUri()
                val bitmap: Bitmap = BitmapFactory.decodeFile(resultUri.path)
                val tempUri: Uri = ImageUtils.getImageUri(bitmap, getApplicationContext())!!
                if (type == "1") {
                    imgPanCardUri = tempUri.toString()
                    Log.e("TAG", "imgPanCardUri-->>: " + tempUri.toString())
                    binding.cardPanCard.visibility = View.VISIBLE
                    binding.imgPanCard.setImageBitmap(bitmap)
                } else if (type == "2") {
                    imgGSTUri = tempUri.toString()
                    Log.e("TAG", "imgGSTUri-->>: " + tempUri.toString())
                    binding.CardGST.visibility = View.VISIBLE
                    binding.imgGstinOrFssai.setImageBitmap(bitmap)
                }
                Log.e("TAG", "result Uri Crop Image $tempUri")
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error: Exception = result.getError()
                Log.e("TAG", "error$error")
            }
        } else if (requestCode == 200 && data != null) {
            binding.edtAddress.setText( data.getStringExtra("address"))
            binding.edtLocation.text = data.getStringExtra("location")
            binding.edtPincode.setText(data.getStringExtra("pincode"))
            lat = data.getDoubleExtra("lat", 0.0)
            lng = data.getDoubleExtra("lng", 0.0)


            Log.e(TAG, "onActivityResult: "+ data.getStringExtra("city") )
            CityID = compareArrayToString(cityDataArrayList,data.getStringExtra("city").toString())

            if (CityID.equals("")){
                showToast("City not available")
            }
            Log.e(TAG, "onActivityResult_cityDataArrayList: $cityDataArrayList")
            Log.e(TAG, "onActivityResult: "+CityID )
        }
    }

    fun compareArrayToString(array: ArrayList<CityListRes.Data>, targetString: String): String {
        for (i in array.indices) {
            if (array[i].city_name.equals(targetString, ignoreCase = true)) {
                binding.edtCityName.text =array[i].city_name.toString()
                return array[i].id.toString()
            }else{
               // showToast("City not available")
                //binding.edtCityName.text.isEmpty()
            }
        }
        return String()
    }



    fun cropImage(imageUri: Uri?) {
        CropImage.activity(imageUri).start(this)
    }

    companion object {
        var PERMISSION_CAMERA = 102
        var GALLERY_PIC_REQUEST = 111
        var CAMERA_PIC_REQUEST = 112
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

    @SuppressLint("CheckResult")
    private fun getCityList() {
        if (!isOnline()) {
            return
        }
        apiService?.getCityList()?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t: Response<CityListRes> ->
                Log.e(TAG, "getCityList.body(): " + Gson().toJson(t.body()))
                AppLog.e(TAG, "getCityList.body(): " + Gson().toJson(t.body()))

                hideLoading()
                if (isSuccess(t, t.message())) {
                    Log.e(TAG, "getCityList: " + Gson().toJson(t.body()))
                    cityDataArrayList!!.addAll(t.body()!!.data)
                } else {
                    showToast(t.body()?.message)
                }
            }, { error ->
                hideLoading()
                onFailure(error)
                Log.e(TAG, "getCityListError: " + error.message)
                Log.e(TAG, "getCityListError: " + error)
            })
    }

    /*  @SuppressLint("CheckResult")
      private fun getCityList() {
          if (!isOnline()) {
              return
          }
          apiService?.getCityList()
              ?.subscribeOn(Schedulers.io())
              ?.observeOn(AndroidSchedulers.mainThread())
              ?.subscribe(
                  { t: Response<CityListRes> ->
                       Log.e(TAG, "getCityList.body(): " + Gson().toJson(t.body()))
                      AppLog.e(TAG, "getCityList.body(): " + Gson().toJson(t.body()))

                      if (isSuccess(t,t.message())) {
                          Log.e(TAG, "getCityList: " + Gson().toJson(t.body()))
                          cityDataArrayList!!.addAll(t.body()!!.data)
                      } else {
                          showToast(t.body()?.message)
                      }
                  },
                  { error ->
                      hideLoading()
                      onFailure(error)
                      Log.e(TAG, "getCityListError: "+error.message )
                      Log.e(TAG, "getCityListError: "+error)
                  }
              )

      }*/
}