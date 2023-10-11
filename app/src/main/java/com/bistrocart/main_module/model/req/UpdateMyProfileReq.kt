package com.bistrocart.main_module.model.req

data class UpdateMyProfileReq(
    val email: String,
    val name: String,
    val phone_number: String,
    val address:String?,
    val lat:String?,
    val long:String?,
    val location:String
)