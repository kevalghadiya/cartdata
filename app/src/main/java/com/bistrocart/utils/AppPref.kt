package com.bistrocart.utils

import android.content.Context
import android.content.SharedPreferences

open class AppPref {

    companion object {

        const val FCM_TOKEN = "FCM_TOKEN"
        const val USER_ID = "USER_ID"
        const val API_KEY = "API_KEY"
        const val NAME = "NAME"
        const val ADDRESS = "ADDRESS"
        const val FMCTOKEN = "FMCTOKEN"
        const val MOBILE_NO = "MOBILE_NO"
        const val OUTLET_NAME = "OUTLET_NAME"
        const val LOCATION = "LOCATION"
        const val PROFILE_IMAGE = "PROFILE_IMAGE"
        const val EMAIL = "EMAIL"
        const val IS_LOGIN = "IS_LOGIN"
        const val VENDOR_REGISTER = "VENDOR_REGISTER"
        const val REFRESH_LIST = "REFRESH_LIST"
        const val LAT = "LAT"
        const val LNG = "LNG"
        const val PRODUCT_NAME = "PRODUCT_NAME"
        const val IMG_PATH = "IMG_PATH"

        var sInstance: AppPref? = null
        var sPref: SharedPreferences? = null
        var sEditor: SharedPreferences.Editor? = null

        @Synchronized
        fun getInstance(context: Context?): AppPref? {
            if (sInstance == null) {
                sInstance = AppPref(context!!)
            }
            return sInstance
        }
    }

    constructor(context: Context) {
        sPref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        sEditor = sPref?.edit()
    }

    //set methods
    open operator fun set(key: String?, value: String?) {
        sEditor!!.putString(key, value).commit()
    }

    open operator fun set(key: String?, value: Boolean) {
        sEditor!!.putBoolean(key, value).commit()
    }

    open operator fun set(key: String?, value: Float) {
        sEditor!!.putFloat(key, value).commit()
    }

    open operator fun set(key: String?, value: Int) {
        sEditor!!.putInt(key, value).commit()
    }

    open operator fun set(key: String?, value: Long) {
        sEditor!!.putLong(key, value).commit()
    }

    open operator fun set(key: String?, value: Set<String?>?) {
        sEditor!!.putStringSet(key, value).commit()
    }

    // get methods
    open fun getInt(key: String?, defaultVal: Int): Int {
        return sPref!!.getInt(key, defaultVal)
    }

    open fun getInt(key: String?): Int {
        return sPref!!.getInt(key, 0)
    }

    open fun getString(key: String?, defaultVal: String?): String? {
        return sPref!!.getString(key, defaultVal)
    }

    open fun getString(key: String?): String? {
        return sPref!!.getString(key, "")
    }


    open fun getBoolean(key: String?, defaultVal: Boolean): Boolean {
        return sPref!!.getBoolean(key, defaultVal)
    }

    open fun getBoolean(key: String?): Boolean {
        return sPref!!.getBoolean(key, false)
    }


    open fun getFloat(key: String?, defaultVal: Float): Float {
        return sPref!!.getFloat(key, defaultVal)
    }

    open fun getFloat(key: String?): Float {
        return sPref!!.getFloat(key, 0f)
    }

    open fun getLong(key: String?, defaultVal: Long): Long {
        return sPref!!.getLong(key, defaultVal)
    }

    open fun getLong(key: String?): Long {
        return sPref!!.getLong(key, 0)
    }

    open fun getStringSet(key: String?): Set<String?>? {
        return sPref!!.getStringSet(key, null)
    }

    open operator fun contains(key: String?): Boolean {
        return sPref!!.contains(key)
    }

    open fun remove(key: String?) {
        sEditor!!.remove(key)
    }

    open fun getAll(): Map<String?, *>? {
        return sPref!!.all
    }

    open fun isLogin(): Boolean {
        return sPref!!.getBoolean(IS_LOGIN, false)
    }

    open fun isVendorRegister(): Boolean {
        return sPref!!.getBoolean(VENDOR_REGISTER, false)
    }

    open fun clearSession() {
        val fcm_token = getString(FCM_TOKEN)
        val lat = getString(LAT)
        val lng = getString(LNG)
        sEditor!!.clear()
        set(FCM_TOKEN, fcm_token)
        set(LAT, lat)
        set(LNG, lng)
    }
}