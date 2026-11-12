
package za.co.skinnercafe.app

import za.co.skinnercafe.app.data.api.MenuProduct
import za.co.skinnercafe.app.data.api.ProductVariant
import za.co.skinnercafe.app.data.api.ProductAddOn

data class CartItem(
    val product: MenuProduct,
    val variant: ProductVariant?,
    val addOns: List<ProductAddOn>,
    val quantity: Int = 1
) {
    val unitPrice: Double
        get() = (variant?.price ?: 0.0) +
                addOns.sumOf { it.price }

    val totalPrice: Double
        get() = unitPrice * quantity
}