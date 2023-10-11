package com.bistrocart.login_module.model.res

data class SignUpRes(
    val `data`: Data,
    val message: String,
    val otp: Int,
    val status: Boolean
) {
    data class Data(
        val address: String,
        val city_id: String,
        val doc_image: String,
        val document: String,
        val email: String,
        val id: Int,
        val lat: String,
        val long: String,
        val name: String,
        val outlet_name: String,
        val pan_image: String,
        val pan_number: String,
        val phone_number: String,
        val pincode: String,
        val token: String,
        val location: String,
    )
}