package com.bistrocart.main_module.model.req

data class AddToCartReq(
    val product_id: String,
    val quantity: String,
    val unit: String
)