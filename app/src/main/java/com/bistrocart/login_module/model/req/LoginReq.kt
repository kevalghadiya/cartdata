package com.bistrocart.login_module.model.req

data class LoginReq(
    val phone_number: String,
    val device_type: String,
    val device_token: String
)