package com.bistrocart.main_module.model.req

data class PlaceOrderReq(
    val address: String,
    val location:String,
    val amount: String,
    val deliverycharge: String,
    val discount: String,
    val lat: String,
    val lng: String,
    val payment_method: String,
    val promocode: String,
    val slot_id: String,
    val slot_text: String,
    val transaction_id: String
)