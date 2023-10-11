package com.bistrocart.main_module.model.res

data class CategoryMainSubRes(
    val `data`: ArrayList<Data>,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val created_at: String,
        val id: Int,
        val main_cat_id: String,
        val sub_cat_image: String,
        val sub_cat_name: String,
        val updated_at: String
    )
}