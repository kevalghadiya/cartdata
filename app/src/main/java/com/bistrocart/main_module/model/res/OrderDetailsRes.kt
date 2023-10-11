package com.bistrocart.main_module.model.res

data class OrderDetailsRes(
    val `data`: List<Data>,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val address: String,
        val amount: String,
        val created_at: String,
        val deliverycharge: String,
        val discount: String,
        val id: Int,
        val lat: String,
        val lng: String,
        val order_items: List<OrderItem>,
        val order_status: List<OrderStatu>,
        val payment_method: String,
        val promocode: String,
        val slot_id: String,
        val slot_text: String,
        val status: String,
        val transaction_id: String,
        val updated_at: String,
        val user_id: String,
        val is_cancellable: Boolean,
        val is_returnable: Boolean,
        val total:String
    ) {
        data class OrderItem(
            val id: Int,
            val image: String,
            val order_id: Int,
            val price: String,
            val product_id: String,
            val product_name: String,
            val quantity: String,
            val unit: String,
            val user_id: String
        )

        data class OrderStatu(
            val Datetime: String,
            val status: String,
            val status_text: String
        )
    }
}