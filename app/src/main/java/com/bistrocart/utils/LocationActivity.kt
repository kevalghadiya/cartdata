package com.bistrocart.utils

import GeoCodeApi
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.bistrocart.BuildConfig
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityLocationBinding
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class LocationActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    lateinit var binding: ActivityLocationBinding
    private var locationCallback: LocationCallback? = null
    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var userLocation: Location? = null
    lateinit var placesClient: PlacesClient
    var latLng: LatLng? = null
    var isfirst = true
    var lat = 0.0
    var lng = 0.0
    private val LOCATION_REQ = 11
    private val LOCATION_ENABLE_REQ_CODE = 12
    var city: String? = null
    var pincode: String? = null
    var location :String? =null
    var address:String?=null
    var isRegister =true

    private val permissionLocation = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location)

        Places.initialize(this, BuildConfig.PLACE_KEY_1 + BuildConfig.PLACE_KEY_2)
        placesClient = Places.createClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /* if (intent!=null){
             isRegister =intent.getStringExtra("isRegister").equals("true")
         }*/

        /* if (isRegister){
             binding.tvFullAddressTxt.visibility =View.VISIBLE
             binding.llEdit.visibility =View.VISIBLE
             binding.tvAdd.setText("Add")
         }*/
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    userLocation = location
                    lat =location.latitude
                    lng =location.longitude

                    mMap!!.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 17f
                        )
                    )

                    stopLocationUpdate()
                    break
                }
            }
        }
        initView()
    }

    private fun initView() {
        if (intent!=null){
             address =intent.getStringExtra("address")
             location = intent.getStringExtra("location")
            Log.e("TAG", "address ====123: "+address)
            Log.e("TAG", "location ====123: "+location)
            binding.etAddress.setText(address)
            binding.ivLocation.setText(location)
        }
        binding.toolbar.title.text = "Select Address"
        binding.toolbar.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        binding.tvAdd.setOnClickListener(View.OnClickListener {
            if (binding.etAddress.text.equals("") && binding.etAddress.text.isNullOrEmpty()){
                showToast("Enter Full Address")
            }else {
              /*  if (isRegister){
                        val intent = Intent()
                        Log.e("TAG", "onActivityResult--location:$lat---$lng")
                        intent.putExtra("lat", lat)
                        intent.putExtra("lng", lng)
                        intent.putExtra("address", binding.etAddress.text.toString())
                        intent.putExtra("location", location)
                        intent.putExtra("city", city.toString())
                        intent.putExtra("pincode", pincode.toString())
                        setResult(200, intent)
                        finish()
                }else{*/
                    updateAddress(this)
               // }
            }
        })

        binding.imgMyLocation.setOnClickListener(View.OnClickListener {
            startLocationUpdate()
        })

       /* binding.etAddress.setOnClickListener(View.OnClickListener {
            selectAddress()
        })
        */
         binding.ivLocation.setOnClickListener(View.OnClickListener {
            selectAddress()
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
            .build(this@LocationActivity)
        startActivityForResult(intent, 109)

    }

    override fun onStart() {
        super.onStart()
        checkLocationRequirement()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setCameraListener()
        startLocationUpdate()

    }

    private fun setCameraListener() {
        mMap!!.setOnCameraIdleListener {
            if (!isGpsOn()) {
                return@setOnCameraIdleListener
            }
            val target = mMap!!.cameraPosition.target
            GlobalScope.launch(Dispatchers.Main) {
                val geoAddress = fetchAddressFromLocation(target.latitude, target.longitude)
                geoAddress?.let {
                    binding.ivLocation.setText(it.address)
                    city = it.city
                    pincode = it.pinCode
                    latLng = LatLng(target.latitude, target.longitude)
                }
            }
        }
    }

    private suspend fun fetchAddressFromLocation(latitude: Double, longitude: Double): GeoCodeApi.GeoAddress? {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
            try {
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0].subLocality + ", " +
                            addresses[0].locality + ", " +
                            addresses[0].adminArea + ", " +
                            addresses[0].postalCode + ", " +
                            addresses[0].countryName
                    GeoCodeApi.GeoAddress().apply {
                        this.address = address
                        this.city = addresses[0].locality
                        this.pinCode = addresses[0].postalCode
                    }
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun checkLocationRequirement() {
        if (!hasPermission(permissionLocation)) {
            ActivityCompat.requestPermissions(
                this@LocationActivity,
                permissionLocation, LOCATION_REQ
            )
            return
        }
        if (!isGpsOn()) {
            return
        }

        // onLocationSatisfy()
    }

    override fun onResume() {
        super.onResume()
        if (!isGpsOn()) {
            return
        }
    }

    private fun startLocationUpdate() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdate() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 30 && data != null) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                //  binding.etSelectAddress.setText(place.address)
                binding.ivLocation.setText(place.getAddress());
                if (place.latLng != null) {
                    showMarker(place.latLng)
                }
                Log.e("TAG", "onActivityResult requestCode 30---> "+latLng )
                latLng = ifAddressCorrect(binding.ivLocation.getText().toString())
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data)
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == 109 && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data)
            val queriedLocation = place.latLng

            Log.e("TAG", "onActivityResult: "+queriedLocation)
           // val latLng = LatLng(queriedLocation.latitude, queriedLocation.longitude)
            getAddressFromLocation(queriedLocation.latitude.toString(), queriedLocation.longitude.toString())
            binding.ivLocation.setText(place.getAddress());
            if (place.latLng != null) {
                showMarker(place.latLng)
            }
            Log.e("TAG", "onActivityResult: " + place.address.toString())

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data)
            showToast(status.statusMessage.toString())
        }
        if (requestCode == LOCATION_ENABLE_REQ_CODE && resultCode == RESULT_OK) {
            // onLocationSatisfy()
        }
    }

    private fun getAddressFromLocation(lats: String, lngs: String) {
        val geocoder = Geocoder(this)
        val addresses: List<Address> =
            geocoder.getFromLocation(lats.toDouble(), lngs.toDouble(), 1)!!
        val address = addresses[0].getAddressLine(0)
        val city_ = addresses[0].locality
        val state = addresses[0].adminArea
        val country = addresses[0].countryName
        val postalCode = addresses[0].postalCode

        // Use the address details as required.
//        binding.etAddress.setText("$address, $city, $state, $country, $postalCode")
        Log.e("TAG", "address: $address, $city, $state, $country, $postalCode ")
       // lat =lats.toDouble()
      //  lng =lngs.toDouble()
        lng =addresses[0].longitude
        lat =addresses[0].latitude
        pincode =postalCode
        city =city_

        // Toast.makeText(requireContext(), "Address: $address, $city, $state, $country, $postalCode", Toast.LENGTH_SHORT).show()
    }

    private fun showMarker(latLng: LatLng?) {
        if (latLng != null) {
            mMap!!.clear()
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            mMap!!.setOnMarkerDragListener(object :
                GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}
                override fun onMarkerDrag(marker: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {
                    val newlatLng = marker.position
                    Log.e("MARKER", "onMarkerDragEnd: " + newlatLng)
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(newlatLng, 17f))
                }
            })
        }
    }


    private fun ifAddressCorrect(address: String): LatLng? {
        val geocoder = Geocoder(this)
        var addressFromLocation: List<Address?>? = ArrayList()
        try {
            addressFromLocation = geocoder.getFromLocationName(address, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var latitude: Double? = null
        var longitude: Double? = null
        val pcode: String
        if (addressFromLocation != null && addressFromLocation.size > 0 && addressFromLocation[0] != null) {
            latitude = addressFromLocation[0]!!.latitude
            longitude = addressFromLocation[0]!!.longitude
            if (isfirst) {
                showMarker(LatLng(latitude, longitude))
                isfirst = false
            }
            pcode = if (addressFromLocation[0]!!.postalCode != null) {
                addressFromLocation[0]!!.postalCode
            } else {
                "0"
            }

        }
        return if (latitude != null && longitude != null) {
            LatLng(latitude, longitude)
        } else {
            null
        }
    }


    override fun onMapClick(p0: LatLng) {
    }


    protected fun isGpsOn(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        if (!gps_enabled) {
            displayLocationSettingsRequest(this)
        }
        return gps_enabled
    }

    private fun displayLocationSettingsRequest(context: Activity) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> Log.i(
                    "BLEFragment",
                    "All location settings are satisfied."
                )
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        "BLEFragment",
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(this@LocationActivity, LOCATION_ENABLE_REQ_CODE)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.i("BLEFragment", "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    "BLEFragment",
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQ) {
            var isAllPermissionGranted = true
            if (grantResults.size > 0) {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        isAllPermissionGranted = false
                        break
                    }
                }
            }
            if (isAllPermissionGranted) {
                checkLocationRequirement()
            }
        }
    }


    private fun updateAddress(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_address, null)
        builder.setView(dialogView)

        val ivCancel = dialogView.findViewById<AppCompatImageView>(R.id.ivCancel)
        val btnDone = dialogView.findViewById<AppCompatButton>(R.id.btnDone)
        val editAddress = dialogView.findViewById<EditText>(R.id.editAddress)

        if (address!=null && !address.isNullOrEmpty()) {
            editAddress.setText(address.toString())
        }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        ivCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDone.setOnClickListener {
            if (!editAddress.text.isNullOrEmpty()){
                val intent = Intent()
                Log.e("TAG", "onActivityResult--location:$lat---$lng")
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)
                intent.putExtra("address", editAddress.text.toString())
                intent.putExtra("location", binding.ivLocation.text.toString())
                intent.putExtra("city", city.toString())
                intent.putExtra("pincode", pincode.toString())
                setResult(200, intent)
                dialog.dismiss()
                finish()
            }else{
                showToast("Address empty")
            }
        }
        dialog.show()
    }

//    override fun hasPermission(permissions: Array<String>): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
//            for (permission in permissions) {
//                if (ActivityCompat.checkSelfPermission(
//                        this,
//                        permission!!
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) return false
//            }
//        }
//        return true
//    }
}