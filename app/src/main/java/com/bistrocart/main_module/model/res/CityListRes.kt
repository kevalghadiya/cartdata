package com.bistrocart.main_module.model.res

data class CityListRes(
    val `data`: ArrayList<Data>,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val city_name: String,
        val created_at: String,
        val id: Int,
        val updated_at: String
    )
}