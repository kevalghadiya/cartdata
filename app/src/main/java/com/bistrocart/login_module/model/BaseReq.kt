package com.retailer.Model

import com.google.gson.Gson

open class BaseReq {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}