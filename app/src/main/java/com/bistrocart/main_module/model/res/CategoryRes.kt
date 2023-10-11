package com.bistrocart.main_module.model.res

data class CategoryRes(
    val `data`: ArrayList<Data>,
    val message: String,
    val status: Boolean,
    val user_status:String? =null
) {
    data class Data(
        val created_at: String,
        val id: Int,
        val main_cat_image: String,
        val main_cat_name: String,
        val updated_at: String
    )
}