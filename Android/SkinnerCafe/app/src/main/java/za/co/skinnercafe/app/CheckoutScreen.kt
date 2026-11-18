
package za.co.skinnercafe.app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import za.co.skinnercafe.app.data.api.CreateOrderItemRequest
import za.co.skinnercafe.app.data.api.CreateOrderRequest
import za.co.skinnercafe.app.data.api.CustomerDto
import za.co.skinnercafe.app.data.api.MenuApiClient
import za.co.skinnercafe.app.data.api.SimulatePaymentRequest
import java.util.UUID

@Composable
fun CheckoutScreen(
    customer: CustomerDto?,
    cartItems: List<CartItem>,
    onBack: () -> Unit,
    onContinueShopping: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    var customerName by remember(customer) {
        mutableStateOf(
            customer?.let { "${it.firstName} ${it.lastName}" } ?: ""
        )
    }

    var customerEmail by remember(customer) {
        mutableStateOf(customer?.email ?: "")
    }

    var customerPhone by remember(customer) {
        mutableStateOf(customer?.phoneNumber ?: "")
    }

    var submittedCartItems by remember {
        mutableStateOf<List<CartItem>>(emptyList())
    }
    var submittedTotal by remember {
        mutableStateOf(0.0)
    }

    var orderId by remember { mutableStateOf<Int?>(null) }

    val displayItems = if (orderId != null) submittedCartItems else cartItems
    val subtotal = if (orderId != null) submittedTotal else cartItems.sumOf { it.totalPrice }

    val coroutineScope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orderNumber by remember { mutableStateOf<String?>(null) }
    var guestAccessToken by remember { mutableStateOf<String?>(null) }

    var paymentMessage by remember { mutableStateOf<String?>(null) }
    var paymentStatus by remember { mutableStateOf<String?>(null) }

    val paymentMethods = listOf(
        "Card",
        "InstantEFT",
        "QR",
        "BankApp",
        "DigitalWallet"
    )

    var paymentMethod by remember { mutableStateOf("Card") }

    val providers = when (paymentMethod) {
        "Card" -> listOf("Visa", "Mastercard")
        "InstantEFT" -> listOf("Ozow")
        "QR" -> listOf("SnapScan", "Zapper")
        "BankApp" -> listOf("CapitecPay")
        "DigitalWallet" -> listOf("GooglePay", "ApplePay")
        else -> emptyList()
    }

    var paymentProvider by remember(paymentMethod) {
        mutableStateOf(providers.first())
    }

    val simulationOutcomes = listOf("Success", "Failure", "Cancelled")
    var simulationOutcome by remember { mutableStateOf("Success") }

    // Reuse the same key if a request fails before we receive a response.
    // Generate a new key after a confirmed payment attempt.
    var idempotencyKey by remember {
        mutableStateOf(UUID.randomUUID().toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Checkout",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (orderId == null) {
            TextButton(onClick = onBack) {
                Text("← Back to cart")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Collection Order",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Text("Your order will be collected at Skinner Cafe.")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Customer Details",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = orderId == null
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customerEmail,
            onValueChange = { customerEmail = it },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = orderId == null
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Phone number (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = orderId == null
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Order Summary",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        displayItems.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "${item.quantity} × ${item.product.productName}",
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "R${"%.2f".format(item.totalPrice)}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "R${"%.2f".format(subtotal)}",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (orderId == null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (customerName.isBlank()) {
                        errorMessage = "Please enter your full name."
                        return@Button
                    }

                    if (
                        customerEmail.isBlank() ||
                        !android.util.Patterns.EMAIL_ADDRESS
                            .matcher(customerEmail.trim())
                            .matches()
                    ) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }

                    if (cartItems.isEmpty()) {
                        errorMessage = "Your cart is empty."
                        return@Button
                    }

                    if (cartItems.any { it.variant == null }) {
                        errorMessage =
                            "One or more products have no selected variant."
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val request = CreateOrderRequest(
                                customerName = customerName.trim(),
                                customerEmail = customerEmail.trim(),
                                customerPhone = customerPhone.trim()
                                    .takeIf { it.isNotBlank() },
                                items = cartItems.map { item ->
                                    CreateOrderItemRequest(
                                        variantId = item.variant!!.variantId,
                                        quantity = item.quantity,
                                        addOnIds = item.addOns.map {
                                            it.addOnId
                                        }
                                    )
                                }
                            )

                            Log.d(
                                "SkinnerCafeCheckout",
                                "Sending order request"
                            )

                            val response =
                                MenuApiClient.service.createOrder(request)

                            submittedCartItems = cartItems.toList()
                            submittedTotal = cartItems.sumOf { it.totalPrice }

                            orderId = response.order.orderId
                            orderNumber = response.order.orderNumber
                            guestAccessToken = response.guestAccessToken

                            Log.d(
                                "SkinnerCafeCheckout",
                                "Order created: $orderNumber"
                            )
                        } catch (exception: Exception) {
                            Log.e(
                                "SkinnerCafeCheckout",
                                "Order submission failed",
                                exception
                            )

                            errorMessage = exception.message
                                ?: "Unable to place your order. Please try again."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isSubmitting && cartItems.isNotEmpty() && orderId == null,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "PLACE ORDER",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        orderNumber?.let { number ->
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Order created!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Your order number:")

                    Text(
                        text = number,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Choose a payment method to continue.")
                }
            }
        }

        if (orderId != null && paymentStatus != "Successful") {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Payment Method",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        enabled = !isSubmitting
                    )

                    Text(method)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Payment Provider",
                fontWeight = FontWeight.Bold
            )

            providers.forEach { provider ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = paymentProvider == provider,
                        onClick = { paymentProvider = provider },
                        enabled = !isSubmitting
                    )

                    Text(provider)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Simulation Outcome (Testing Only)",
                fontWeight = FontWeight.Bold
            )

            simulationOutcomes.forEach { outcome ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = simulationOutcome == outcome,
                        onClick = { simulationOutcome = outcome },
                        enabled = !isSubmitting
                    )

                    Text(outcome)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            paymentMessage?.let {
                Text(
                    text = it,
                    color = if (paymentStatus == "Failed") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    val currentOrderId = orderId ?: return@Button

                    isSubmitting = true
                    errorMessage = null
                    paymentMessage = null

                    coroutineScope.launch {
                        try {
                            val request = SimulatePaymentRequest(
                                guestAccessToken = guestAccessToken,
                                idempotencyKey = idempotencyKey,
                                paymentMethod = paymentMethod,
                                paymentProvider = paymentProvider,
                                simulationOutcome = simulationOutcome
                            )

                            val response =
                                MenuApiClient.service.simulatePayment(
                                    orderId = currentOrderId,
                                    request = request
                                )

                            paymentStatus = response.payment.paymentStatus

                            paymentMessage = response.message

                            Log.d(
                                "SkinnerCafeCheckout",
                                "Payment status: $paymentStatus"
                            )

                            // A confirmed response means this attempt has
                            // been processed. A retry needs a new key.
                            idempotencyKey = UUID.randomUUID().toString()

                            if (paymentStatus.equals(
                                    "Successful",
                                    ignoreCase = true
                                )
                            ) {
                                onOrderPlaced()
                            }
                        } catch (exception: Exception) {
                            Log.e(
                                "SkinnerCafeCheckout",
                                "Payment submission failed",
                                exception
                            )

                            errorMessage = exception.message
                                ?: "Unable to process payment. Please try again."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "PAY NOW",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (paymentStatus == "Successful") {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Payment successful!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Your order has been confirmed.")
                    Text("Order number: ${orderNumber ?: ""}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueShopping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CONTINUE SHOPPING",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
