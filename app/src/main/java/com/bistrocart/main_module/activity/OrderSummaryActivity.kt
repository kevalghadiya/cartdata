package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.bistrocart.BuildConfig
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityOrderSummaryBinding
import com.bistrocart.databinding.DialogSelectTimeSlotBinding
import com.bistrocart.databinding.DialogSuccessfulPaymentBinding
import com.bistrocart.databinding.DialogUnitPriceBinding
import com.bistrocart.login_module.PaymentModule.PhonePayActivity
import com.bistrocart.main_module.adapter.MyCartListAdapter
import com.bistrocart.main_module.adapter.TimeSlotAdapter
import com.bistrocart.main_module.adapter.UniPriceAdapterCard
import com.bistrocart.main_module.model.req.PlaceOrderReq
import com.bistrocart.main_module.model.res.BasicRes
import com.bistrocart.main_module.model.res.MyCartRes
import com.bistrocart.main_module.model.res.PlaceOrderRes
import com.bistrocart.main_module.model.res.TimeSloteRes
import com.bistrocart.utils.AppLog
import com.bistrocart.utils.AppPref
import com.bistrocart.utils.LocationActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.*

class OrderSummaryActivity : BaseActivity() {
    private val TAG = "OrderSummaryActivity"
    lateinit var binding: ActivityOrderSummaryBinding
    private val myCartList = ArrayList<MyCartRes.Data>()
    private val timeSlotList = ArrayList<TimeSloteRes.Data>()
    private lateinit var myCartListAdapter: MyCartListAdapter
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private var timeSlotMsg: String? = null
    private var timeSlotID: String? = null
    private var paymentIs: String? = ""
    private var transactionId: String? = ""
    lateinit var placesClient: PlacesClient
    lateinit var dialogSelectTimeSlotBinding: DialogSelectTimeSlotBinding
    lateinit var dialogSuccessfulPaymentBinding: DialogSuccessfulPaymentBinding
    var location: String? = "location"

    var lat: Double = 0.0
    var lng: Double = 0.0

    var subTotal: Int = 0
    var total: Int = 0
    var discount: Int = 0
    var deliveryCharge: Int = 0

    lateinit var uniPriceAdapterCart: UniPriceAdapterCard
    lateinit var dialogUnitPriceBinding: DialogUnitPriceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_summary)
        initView()
        myCartList()
        setMyCartListAdapter()
        timeSlot()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initView() {
        if (!appPref?.getString(AppPref.LOCATION).isNullOrEmpty()) {
            location = appPref?.getString(AppPref.LOCATION)
        }
        if (intent != null) {
            // myCartList.addAll(intent.getSerializableExtra("myCartList") as ArrayList<MyCartRes.Data>)
            // Log.e(TAG, "initView: " + myCartList)
            // subTotal = (intent.getStringExtra("totalCartPrice").toString()).toInt()

        }
        Places.initialize(this, BuildConfig.PLACE_KEY_1 + BuildConfig.PLACE_KEY_2)
        placesClient = Places.createClient(this)

        binding.toolbar.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        binding.toolbar.title.text = "Order Summary"

        binding.llSelectTime.setOnClickListener(View.OnClickListener {
            timeSlotDialog()
        })

        binding.btnPayNow.setOnClickListener(View.OnClickListener {
            if (paymentIs.equals("")) {
                showToast("Select payment Option")
            } else if (paymentIs.equals("online")) {
                val intent = Intent(this, PhonePayActivity::class.java)
                val bundle = Bundle().apply { putString("amount", binding.tvTotal.text.toString()) }
                Log.e(
                    TAG,
                    "amountOnOrderSummary: " + bundle + "----" + binding.tvTotal.text.toString()
                )
                intent.putExtras(bundle)
                startActivityForResult(intent, 100)
            } else
                placeOrder()
               // successFullPayment()
        })

        binding.radioBtnOnline.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.radioBtnCod.isChecked = false
                paymentIs = "online"
               // transactionId = "1"
            }
        }

        binding.radioBtnCod.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.radioBtnOnline.isChecked = false
                paymentIs = "cod"
                transactionId = "0"
            }
        }

        binding.llAddress.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            intent.putExtra("address", binding.tvAddress.text)
            intent.putExtra("location", location)
            startActivityForResult(intent, 200)
            //selectAddress()
        })

        binding.tvAddress.text = appPref!!.getString(AppPref.ADDRESS.toString())

        binding.tvApply.setOnClickListener(View.OnClickListener {
            if (binding.edtPromo.text!!.isNotEmpty()) {
                if (binding.tvApply.text.equals("Apply")) {
                    applyPromoCode()
                } else {
                    binding.edtPromo.text?.clear()
                    binding.tvApply.setText("Apply")
                    binding.edtPromo.isEnabled = true
                    discount = 0
                    calculateFinalTotal(subTotal, discount, deliveryCharge).toString()
                    updateUIValues(subTotal, discount, deliveryCharge)
                }
            }
        })
    }

    private fun selectAddress() {
        val fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setTypeFilter(TypeFilter.ADDRESS)
            .build(this@OrderSummaryActivity)
        startActivityForResult(intent, 109)

    }

    private fun setMyCartListAdapter() {
        myCartListAdapter =
            MyCartListAdapter(myCartList, false, object : MyCartListAdapter.ItemClickCallback {
                override fun onItemClick(item: MyCartRes.Data, type: Int, position: Int) {
                    if (type.equals(2)) {
                        if (item.prices != null && item.prices.size > 0) {
                            //   unitPriceDialog(item.prices.get(0), item.quantity)
                        } else {
                            //   unitPriceDialog(item.products_prices, "")
                        }
                    }
                }
            })
        binding.rvCartDetails.adapter = myCartListAdapter
        //  binding.rvCartDetails.setHasFixedSize(true)
    }

    @SuppressLint("CheckResult")
    private fun myCartList() {
        myCartList.clear()
        if (!isOnline()) {
            return
        }

        showLoading("loading")
        apiService?.myCartList()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<MyCartRes> ->
                    AppLog.e(TAG, "cartRes: $t")
                    AppLog.e(TAG, "Category.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        myCartList.addAll(t.body()!!.data)
                        subTotal = t.body()!!.total_Cartprice

                        deliveryCharge = t.body()!!.config_amount.get(0).delivery_charge.toInt()
                        if (subTotal < t.body()!!.config_amount.get(0).amount.toInt()) {
                            calculateFinalTotal(subTotal, 0, deliveryCharge).toString()
                            updateUIValues(subTotal, discount, deliveryCharge)
                        } else {
                            deliveryCharge = 0
                            calculateFinalTotal(subTotal, 0, deliveryCharge).toString()
                            updateUIValues(subTotal, discount, deliveryCharge)
                        }

                        if (binding.tvSubTotal.text.toString().toInt() < t.body()!!.config_amount.get(0).amount.toInt()
                        ) {
                            binding.tvFreeDeliveryText.text =
                                "Order for ${t.body()!!.config_amount.get(0).amount.toInt()} or more & get free delivery"
                            binding.tvFreeDeliveryText.visibility = View.VISIBLE
                        } else {
                            binding.tvFreeDeliveryText.visibility = View.GONE
                        }

                        myCartListAdapter.notifyDataSetChanged()
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )

    }


    private fun timeSlotDialog() {
        val dialog: Dialog
        dialog = Dialog(this, R.style.appDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogSelectTimeSlotBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_select_time_slot,
            null,
            false
        )

        timeSlotAdapter = TimeSlotAdapter(timeSlotList, object : TimeSlotAdapter.ItemClickCallback {
            override fun onItemClick(item: TimeSloteRes.Data, type: Int) {
                timeSlotMsg =
                    item.slot_msg + "\n" + item.delivery_date + "(" + item.start_time + "-" + item.end_time + ")"
                timeSlotID = item.id.toString()
            }
        })
        dialogSelectTimeSlotBinding.rvTimeSlot.adapter = timeSlotAdapter

        123
        dialogSelectTimeSlotBinding.btnSave.setOnClickListener(View.OnClickListener {
            binding.tvSelectTime.text = timeSlotMsg
            dialog.dismiss()
        })

        dialogSelectTimeSlotBinding.imgCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.setContentView(dialogSelectTimeSlotBinding.getRoot())
        dialog.show()

    }

    @SuppressLint("CheckResult")
    private fun timeSlot() {
        if (!isOnline()) {
            return
        }

        showLoading("loading")
        apiService?.timeSlot()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<TimeSloteRes> ->
                    AppLog.e(TAG, "TimeSloteRes: $t")
                    AppLog.e(TAG, "TimeSloteRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        binding.tvSelectTime.text =
                            t.body()!!.data.get(0).slot_msg + "\n" + t.body()!!.data.get(0).delivery_date + "(" + t.body()!!.data.get(
                                0
                            ).start_time + "-" + t.body()!!.data.get(0).end_time + ")"
                        timeSlotMsg =
                            t.body()!!.data.get(0).slot_msg + "\n" + t.body()!!.data.get(0).delivery_date + "(" + t.body()!!.data.get(
                                0
                            ).start_time + "-" + t.body()!!.data.get(0).end_time + ")"
                        timeSlotID = t.body()!!.data.get(0).id.toString()
                        timeSlotList.addAll(t.body()!!.data)

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

    fun successFullPayment() {
        val dialog: Dialog
        dialog = Dialog(this, R.style.appDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogSuccessfulPaymentBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_successful_payment,
            null,
            false)

        dialogSuccessfulPaymentBinding.btnDone.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            var bundle = Bundle()
            bundle.putString("isOrderSummary", "true")
            gotoActivity(MyOrderListActivity::class.java, bundle, true)
        })

        dialogSuccessfulPaymentBinding.imgCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.setContentView(dialogSuccessfulPaymentBinding.getRoot())
        dialog.show()

    }


    @SuppressLint("CheckResult")
    private fun placeOrder() {
        if (!isOnline()) {
            return
        }

        var req = PlaceOrderReq(
            binding.tvAddress.text.toString(),
            location.toString(),
            binding.tvSubTotal.text.toString(),
            binding.tvDeliveryCharge.text.toString(),
            binding.tvDiscount.text.toString(),
            lat.toString(),
            lng.toString(),
            paymentIs.toString(),
            binding.edtPromo.text.toString(),
            timeSlotID.toString(),
            timeSlotMsg.toString(),
            transactionId.toString(),
        )
        showLoading("loading")
        Log.e(TAG, "placeOrder: " + req.toString())
        apiService?.placeOrder(req)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<PlaceOrderRes> ->
                    AppLog.e(TAG, "PlaceOrderRes: $t")
                    AppLog.e(TAG, "PlaceOrderRes.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        showToast(t.body()?.message)
                        appPref?.set(AppPref.ADDRESS, binding.tvAddress.text.toString())
                        successFullPayment()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && data != null) {
            binding.tvAddress.text = data.getStringExtra("address")
            lat = data.getDoubleExtra("lat", 0.0)
            lng = data.getDoubleExtra("lng", 0.0)
            location = data.getStringExtra("location")
            Log.e("TAG123", "onActivityResult: " + lat + lng)
        } else if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e(TAG, "m_transaction_id_is: "+data!!.getStringExtra("m_transaction_id") )
                transactionId =data!!.getStringExtra("m_transaction_id").toString()
                placeOrder()
//                successFullPayment()
                /* val result = data?.getStringExtra(PhonePayActivity.EXTRA_RESULT)
                  textViewResult.text = result*/
            }
        }
    }

    private fun getAddressFromLocation(lat: String, lng: String) {
        val geocoder = Geocoder(this)
        val addresses: List<Address> =
            geocoder.getFromLocation(lat.toDouble(), lng.toDouble(), 1)!!
        val address = addresses[0].getAddressLine(0)
        val city = addresses[0].locality
        val state = addresses[0].adminArea
        val country = addresses[0].countryName
        val postalCode = addresses[0].postalCode

        // Use the address details as required.
        binding.tvAddress.setText("$address, $city, $state, $country, $postalCode")
        Log.e(TAG, "address: $address, $city, $state, $country, $postalCode ")
    }

    @SuppressLint("CheckResult")
    private fun applyPromoCode() {
        if (!isOnline()) {
            return
        }
        showLoading("loading")
        Log.e(
            TAG, "applyPromoCode: " + binding.edtPromo.text.toString() +
                    subTotal
        )

        apiService?.applyPromoCode(binding.edtPromo.text.toString(), subTotal.toString())
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<BasicRes> ->
                    AppLog.e(TAG, "applyPromoCode: $t")
                    AppLog.e(TAG, "applyPromoCode.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t, t.message())) {
                        showToast(t.body()?.message)
                        if (t.body()!!.status) {
                            binding.tvApply.setText("Remove")
                            binding.edtPromo.isEnabled = false

                            Log.e(TAG, "applyPromoCode_discount1: " + discount)
                            discount = t.body()!!.promocode_discount.toInt()
                            calculateFinalTotal(subTotal, discount, deliveryCharge).toString()
                            updateUIValues(subTotal, discount, deliveryCharge)
                        } else {
                            Log.e(TAG, "applyPromoCode: " + t.body()!!.promocode_discount.toInt())
                            calculateFinalTotal(subTotal, discount, deliveryCharge).toString()
                            binding.edtPromo.isEnabled = true
                            Log.e(TAG, "applyPromoCode_discount2: " + discount)
                        }
                        appPref?.set(AppPref.ADDRESS, binding.tvAddress.text.toString())
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

    private fun unitPriceDialog(
        productsPrices: MyCartRes.Data.Price,
        unitSelect: String?,
    ) {

        val dialog: Dialog
        dialog = Dialog(this, R.style.appDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogUnitPriceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_unit_price,
            null,
            false
        )

        uniPriceAdapterCart = UniPriceAdapterCard(listOf(productsPrices),
            unitSelect!!,
            object : UniPriceAdapterCard.ItemClickCallback {
                override fun onItemClick(item: MyCartRes.Data.Price, type: Int) {
                    //  addToCartReq = (AddToCartReq(item.pro_id,item.unit,item.unit))
                    //   product_id =item.pro_id
                    //  quantity =item.unit
                    //   unit=item.unit
                }

            })
        dialogUnitPriceBinding.rvUnitPrice.adapter = uniPriceAdapterCart

        dialogUnitPriceBinding.btnAdd.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            /* if (product_id!=null &&product_id!="") {
                 addToCart(dialog)
             }*/
        })
        dialog.setContentView(dialogUnitPriceBinding.getRoot())
        dialog.show()

    }

    private fun calculateFinalTotal(subTotal: Int, discount: Int, deliveryCharge: Int): Int {
        val discountedSubTotal = subTotal - discount + deliveryCharge
        //   val finalDeliveryCharge = deliveryCharge
        Log.e(TAG, "calculateFinalTotal: " + discountedSubTotal)
        return discountedSubTotal /*+ finalDeliveryCharge*/
    }

    private fun updateUIValues(subTotal: Int, discount: Int, deliveryCharge: Int) {
        val finalTotal = calculateFinalTotal(subTotal, discount, deliveryCharge)

        Log.e(TAG, "finalTotal: " + finalTotal)
        Log.e(TAG, "discount: " + discount)
        Log.e(TAG, "subTotal: " + subTotal)
        Log.e(TAG, "tvDeliveryCharge:" + deliveryCharge)

        binding.tvTotal.text = finalTotal.toString()
        binding.tvDiscount.text = discount.toString()
        binding.tvSubTotal.text = subTotal.toString()
        binding.tvDeliveryCharge.text = deliveryCharge.toString()
    }
}