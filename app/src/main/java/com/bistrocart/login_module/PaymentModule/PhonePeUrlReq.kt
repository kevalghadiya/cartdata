package com.bistrocart.login_module.PaymentModule

data class PhonePeUrlReq(
    val amount: Int,
    val mobileNumber: String,
    val user_id: String
)