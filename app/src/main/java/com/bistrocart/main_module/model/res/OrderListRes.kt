package com.bistrocart.main_module.model.res

data class OrderListRes(
    val `data`: Data,
    val filrurl: String,
    val message: String,
    val status: Boolean
) {
    data class  Data(
        val current_page: Int,
        val `data`: List<Data>,
        val first_page_url: String,
        val from: Int,
        val last_page: Int,
        val last_page_url: String,
        val links: List<Link>,
        val next_page_url: Any,
        val path: String,
        val per_page: Int,
        val prev_page_url: Any,
        val to: Int,
        val total: Int
    ) {
        data class Data(
            val address: String,
            val amount: String,
            val created_at: String,
            val deliverycharge: String,
            val discount: String,
            val id: Int,
            val item_list: List<Item>,
            val lat: String,
            val lng: String,
            val payment_method: String,
            val promocode: String,
            val slot_id: String,
            val slot_text: String,
            val status: String,
            val transaction_id: String,
            val updated_at: String,
            val user_id: String
        ) {
            data class Item(
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
        }

        data class Link(
            val active: Boolean,
            val label: String,
            val url: String
        )
    }
}