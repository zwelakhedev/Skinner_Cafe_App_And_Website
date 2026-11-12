package za.co.skinnercafe.app.data.api

import com.google.gson.annotations.SerializedName

data class MenuProduct(
    @SerializedName("productId")
    val productId: Int,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("category")
    val category: String,

    @SerializedName("variants")
    val variants: List<ProductVariant>,

    @SerializedName("addOns")
    val addOns: List<ProductAddOn> = emptyList()
)

data class ProductVariant(
    @SerializedName("variantId")
    val variantId: Int,

    @SerializedName("variantName")
    val variantName: String,

    @SerializedName("price")
    val price: Double
)

data class ProductAddOn(
    @SerializedName("addOnId")
    val addOnId: Int,

    @SerializedName("addOnName")
    val addOnName: String,

    @SerializedName("price")
    val price: Double
)
