package com.bistrocart.main_module.model

data class SavedListModel(
    var productName: String? = null,
    var productPriceAndQty: String? = null,
    var productImage: Int? = null,
    var isSavedItem : Boolean? = null
)
