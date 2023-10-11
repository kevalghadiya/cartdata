package com.retailer.Model

import com.google.gson.Gson

open class BaseRes {

    var message : String? = null
    var status : Boolean = false

    override fun toString(): String {
        return Gson().toJson(this)
    }
}