package com.bistrocart.main_module.model.res

data class TimeSloteRes(
    val `data`: List<Data>,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val created_at: String,
        val end_time: String,
        val id: Int,
        val slot_msg: String,
        val start_time: String,
        val updated_at: String,
        val delivery_date:String?
    )
}