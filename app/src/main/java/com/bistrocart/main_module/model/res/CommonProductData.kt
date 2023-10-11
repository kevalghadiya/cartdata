package com.bistrocart.main_module.model.res
import java.io.Serializable

data class CommonProductData(
    val cartItems: ArrayList<CartItem>,
    val catId: String?,
    val createdAt: String?,
    val id: Int?,
    val proDescription: String?,
    val proName: String?,
    val productsImages: List<ProductsImage>,
    val productsPrices: List<ProductsPrice>,
    val subCatId: String?,
    val updatedAt: String?,
    val dealOfTheDay: String? = null,
    val gst: String? = null,
    val instock: String? = null,
    val price: String? = null,
    val proCode: String? = null
) : Serializable {
    data class CartItem(
        val createdAt: String?,
        val id: Int?,
        val orderId: Int?,
        val price: String?,
        val productId: String?,
        val productName: String?,
        var quantity: String?,
        val unit: String?,
        val updatedAt: String?,
        val userId: String?
    ) : Serializable

    data class ProductsImage(
        val createdAt: String?,
        val id: Int?,
        val proId: String?,
        val proImage: String?,
        val updatedAt: String?
    ) : Serializable

    data class ProductsPrice(
        val createdAt: String?,
        val id: Int?,
        val pricePerUnit: String?,
        val proId: String?,
        val unit: String?,
        val updatedAt: String?
    ) : Serializable
}

