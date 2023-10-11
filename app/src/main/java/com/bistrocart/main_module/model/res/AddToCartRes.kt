package com.bistrocart.main_module.model.res

data class AddToCartRes(
    val config_amount: List<ConfigAmount>,
    val data: List<Data>,
    val message: String,
    val status: Boolean,
    val total_Cartprice: Int
) {
    data class ConfigAmount(
        val amount: String,
        val delivery_charge: String
    )

    data class Data(
        val favourite: String,
        val image: String,
        val price: String,
        val prices: List<Price>,
        val product_id: String,
        val product_name: String,
        val quantity: String
    ) {
        data class Price(
            val created_at: String,
            val id: Int,
            val price_per_unit: String,
            val pro_id: String,
            val unit: String,
            val updated_at: Any
        )
    }
}