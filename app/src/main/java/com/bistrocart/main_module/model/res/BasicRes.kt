package com.bistrocart.main_module.model.res

data class BasicRes(
    val message: String,
    val otp: Int,
    val status: Boolean,
    val promocode_discount:Double,
    val new_total:Double
)