package com.bistrocart.main_module.model.res

data class FavouriteAddEditRes(
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val created_at: String,
        val favourite: String,
        val id: Int,
        val pro_id: String,
        val updated_at: String,
        val user_id: Int
    )
}