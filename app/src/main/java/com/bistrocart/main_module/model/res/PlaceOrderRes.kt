package com.bistrocart.main_module.model.res

data class PlaceOrderRes(
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val address: String,
        val amount: String,
        val created_at: String,
        val deliverycharge: String,
        val discount: String,
        val id: Int,
        val lat: String,
        val lng: String,
        val payment_method: String,
        val promocode: String,
        val slot_id: String,
        val slot_text: String,
        val status: String,
        val transaction_id: String,
        val updated_at: String,
        val user_id: Int
    )
}