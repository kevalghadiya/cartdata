package com.bistrocart.main_module.model.res

data class MyCartRes(
    val config_amount: List<ConfigAmount>,
    val `data`: List<Data>,
    val filrurl: String,
    val message: String,
    val status: Boolean,
    val total_Cartprice: Int
) {
    data class ConfigAmount(
        val amount: String,
        val delivery_charge: String
    )

    data class Data(
        val image: String,
        val price: String,
        val prices: List<Price>,
        val product_id: String,
        val product_name: String,
        var quantity: String
    ) {
        data class Price(
            val created_at: String,
            val id: Int,
            val price_per_unit: String,
            val pro_id: String,
            val unit: String,
            val updated_at: String
        )
    }
}