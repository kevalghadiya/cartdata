package com.bistrocart.main_module.model.res

data class NotificationRes(
    val `data`: List<Data>,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val created_at: String,
        val id: Int,
        val message: String,
        val order_id: String,
        val title: String,
        val updated_at: String,
        val user_id: String,
        val type:String
    )
}