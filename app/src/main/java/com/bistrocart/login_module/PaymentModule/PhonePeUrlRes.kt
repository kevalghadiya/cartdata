package com.bistrocart.login_module.PaymentModule

data class PhonePeUrlRes(
    val key: String,
    val key_index: String,
    val return_url: String,
    val status: Boolean,
    val url: String,
    val redirect_url:String,
    val m_transaction_id:String
)