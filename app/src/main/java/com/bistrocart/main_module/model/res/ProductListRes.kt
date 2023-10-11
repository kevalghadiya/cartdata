package com.bistrocart.main_module.model.res

import java.io.Serializable

data class ProductListRes(
    val `data`: Data?,
    val fileurl: String?,
    val message: String?,
    val status: Boolean?
): Serializable {
    data class Data(
        val current_page: Int?,
        val `data`: List<Data>?,
        val first_page_url: String?,
        val from: Int?,
        val last_page: Int?,
        val last_page_url: String?,
        val links: List<Link>?,
        val next_page_url: Any?,
        val path: String?,
        val per_page: Int?,
        val prev_page_url: Any?,
        val to: Int?,
        val total: Int?
    ):Serializable {
        data class Data(
            val cart_items: ArrayList<CartItem>,
            val cat_id: String?,
            val created_at: String?,
            val id: Int?,
            val pro_description: String?,
            val pro_name: String?,
            val products_images: List<ProductsImage>,
            val products_prices: List<ProductsPrice>,
            val favourite_products: List<favourite_product>,
            val sub_cat_id: String?,
            val updated_at: String?,
            val price:String?,
            val instock:String?
        ):Serializable {
            data class CartItem(
                val created_at: String?,
                val id: Int?,
                val order_id: Int?,
                val price: String?,
                val product_id: String?,
                val product_name: String?,
                var quantity: String?,
                val unit: String?,
                val updated_at: String?,
                val user_id: String?
            ):Serializable

            data class ProductsImage(
                val created_at: String?,
                val id: Int?,
                val pro_id: String?,
                val pro_image: String?,
                val updated_at: String?
            ):Serializable

            data class ProductsPrice(
                val created_at: String?,
                val id: Int?,
                val price_per_unit: String?,
                val pro_id: String?,
                val unit: String?,
                val updated_at: String?
            ):Serializable

           data class favourite_product (
               val  id:Int?,
               val user_id:String?,
               val pro_id:String?,
               val favourite:String?,
               val created_at:String?,
               val updated_at:String?):Serializable
        }

        data class Link(
            val active: Boolean?,
            val label: String?,
            val url: String?
        ):Serializable
    }
}