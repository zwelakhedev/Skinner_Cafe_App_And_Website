package za.co.skinnercafe.app.data.api

data class CreateOrderRequest(
    val customerName: String,
    val customerEmail: String?,
    val customerPhone: String?,
    val items: List<CreateOrderItemRequest>
)

data class CreateOrderItemRequest(
    val variantId: Int,
    val quantity: Int,
    val addOnIds: List<Int>
)

data class CreateOrderResponse(
    val message: String,
    val order: CreatedOrder,
    val guestAccessToken: String? = null
)

data class CreatedOrder(
    val orderId: Int,
    val orderNumber: String,
    val orderStatus: String,
    val paymentStatus: String,
    val subtotal: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val createdAt: String
)

data class SimulatePaymentRequest(
    val guestAccessToken: String?,
    val idempotencyKey: String,
    val paymentMethod: String,
    val paymentProvider: String,
    val simulationOutcome: String
)

data class SimulatePaymentResponse(
    val message: String,
    val payment: SimulatedPayment,
    val order: PaidOrder
)

data class SimulatedPayment(
    val paymentId: Long,
    val paymentMethod: String,
    val paymentProvider: String?,
    val paymentStatus: String,
    val amount: Double,
    val providerReference: String?,
    val paidAt: String?
)

data class PaidOrder(
    val orderId: Int,
    val orderNumber: String,
    val orderStatus: String,
    val paymentStatus: String
)

data class MyOrder(
    val orderId: Int,
    val orderNumber: String,
    val orderSource: String,
    val orderStatus: String,
    val paymentStatus: String,
    val subtotal: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val createdAt: String,
    val completedAt: String?,
    val items: List<MyOrderItem>,
    val payments: List<MyOrderPayment>,
    val statusHistory: List<MyOrderStatusHistory>
)

data class MyOrderItem(
    val orderItemId: Int,
    val productNameSnapshot: String,
    val variantNameSnapshot: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val addOns: List<MyOrderAddOn>
)

data class MyOrderAddOn(
    val addOnNameSnapshot: String,
    val quantity: Int,
    val unitPrice: Double
)

data class MyOrderPayment(
    val paymentId: Long,
    val paymentMethod: String,
    val paymentStatus: String,
    val amount: Double,
    val paymentProvider: String?,
    val createdAt: String,
    val paidAt: String?
)

data class MyOrderStatusHistory(
    val previousStatus: String?,
    val newStatus: String,
    val notes: String? = null,
    val changedAt: String
)

data class RegisterDeviceTokenRequest(
    val token: String
)