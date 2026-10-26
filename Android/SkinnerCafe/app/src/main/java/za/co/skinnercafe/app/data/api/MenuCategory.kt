package za.co.skinnercafe.app.data.api

import com.google.gson.annotations.SerializedName

data class MenuCategory(
    @SerializedName("categoryId")
    val categoryId: Int,

    @SerializedName("categoryName")
    val categoryName: String
)
