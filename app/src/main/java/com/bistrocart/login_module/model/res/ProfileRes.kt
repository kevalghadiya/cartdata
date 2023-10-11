package com.bistrocart.login_module.model.res

data class ProfileRes(
    val `data`: List<Data>,
    val message: String?,
    val status: Boolean
) {
    data class Data(
        val address: String?,
        val city_id: String?,
        val created_at: String?,
        val device_token: Any?,
        val device_type: Any?,
        val doc_image: String?,
        val doc_number: String?,
        val document: String?,
        val email: String?,
        val email_verified_at: Any?,
        val id: Int?,
        val lat: String?,
        val long: String?,
        val name: String?,
        val outlet_name: String?,
        val pan_image: String?,
        val pan_number: String?,
        val phone_number: String?,
        val pincode: String?,
        val remember_token: Any,
        val updated_at: String?,
        val location: String?,
    )
}