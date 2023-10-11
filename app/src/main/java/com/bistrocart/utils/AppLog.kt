package com.bistrocart.utils

import android.util.Log
import com.bistrocart.BuildConfig
class AppLog {

    companion object{

        fun e(TAG: String?, msg: String?) {
            if (BuildConfig.DEBUG) Log.e(TAG, msg!!)
        }

        fun i(TAG: String?, msg: String?) {
            if (BuildConfig.DEBUG) Log.i(TAG, msg!!)
        }

        fun d(TAG: String?, msg: String?) {
            if (BuildConfig.DEBUG) Log.d(TAG, msg!!)
        }

        fun v(TAG: String?, msg: String?) {
            if (BuildConfig.DEBUG) Log.v(TAG, msg!!)
        }
    }
}