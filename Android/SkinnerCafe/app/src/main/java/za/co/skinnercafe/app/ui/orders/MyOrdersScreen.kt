package za.co.skinnercafe.app.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.skinnercafe.app.data.api.MyOrder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size

@Composable
fun MyOrdersScreen(
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
    ordersViewModel: OrdersViewModel = viewModel()
) {
    val uiState by ordersViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        ordersViewModel.loadMyOrders(onSessionExpired)

        while (true) {
            delay(30_000)
            ordersViewModel.loadMyOrders(onSessionExpired)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }

            Text(
                text = "My Orders",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                ordersViewModel.loadMyOrders(onSessionExpired)
            }
        ) {
            Text("Refresh")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        when {
            uiState.isLoading && uiState.orders.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading your orders...",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            uiState.errorMessage != null && uiState.orders.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = {
                            ordersViewModel.loadMyOrders(onSessionExpired)
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Try Again")
                    }
                }
            }

            uiState.orders.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No orders yet",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Your completed and recent orders will appear here.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.errorMessage != null) {
                        item {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )

                            TextButton(
                                onClick = {
                                    ordersViewModel.loadMyOrders(onSessionExpired)
                                }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }

                    items(
                        items = uiState.orders,
                        key = { it.orderId }
                    ) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderProgressTracker(order: MyOrder) {
    val stages = listOf(
        "Pending",
        "Confirmed",
        "Preparing",
        "Ready",
        "Collected",
        "Completed"
    )

    val currentIndex = stages.indexOfFirst {
        it.equals(order.orderStatus, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Order progress",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (order.orderStatus.equals("Cancelled", ignoreCase = true)) {
            Text(
                text = "Order cancelled",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )

            order.statusHistory
                .lastOrNull {
                    it.newStatus.equals("Cancelled", ignoreCase = true)
                }
                ?.let { history ->
                    history.notes
                        ?.takeIf { it.isNotBlank() }
                        ?.let { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                }
        } else if (currentIndex == -1) {
            Text(
                text = "Current status: ${order.orderStatus}",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            stages.forEachIndexed { index, stage ->
                val isCompleted = index < currentIndex
                val isCurrent = index == currentIndex

                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (isCompleted || isCurrent) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CircleShape
                                )
                        )

                        if (index < stages.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .width(2.dp)
                                    .height(22.dp)
                                    .background(
                                        if (isCompleted) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = stage,
                            color = if (isCompleted || isCurrent) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = if (isCurrent) {
                                MaterialTheme.typography.bodyMedium
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            fontWeight = if (isCurrent) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )

                        order.statusHistory
                            .lastOrNull {
                                it.newStatus.equals(stage, ignoreCase = true)
                            }
                            ?.let { history ->
                                Text(
                                    text = history.changedAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: MyOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = order.orderNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formatOrderDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order status")
                Text(
                    text = order.orderStatus,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Payment status")
                Text(order.paymentStatus)
            }

            OrderProgressTracker(order)

            Divider()

            Text(
                text = "Items",
                fontWeight = FontWeight.Bold
            )

            order.items.forEach { item ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.quantity} × ${item.productNameSnapshot}")
                        Text(formatCurrency(item.lineTotal))
                    }

                    Text(
                        text = item.variantNameSnapshot,
                        style = MaterialTheme.typography.bodySmall
                    )

                    item.addOns.forEach { addOn ->
                        Text(
                            text = "  + ${addOn.addOnNameSnapshot} × ${addOn.quantity}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Divider()

            OrderAmountRow("Subtotal", order.subtotal)
            OrderAmountRow("Discount", order.discountAmount)
            OrderAmountRow(
                label = "Total",
                amount = order.totalAmount,
                bold = true
            )

            if (order.payments.isNotEmpty()) {
                Divider()

                Text(
                    text = "Payments",
                    fontWeight = FontWeight.Bold
                )

                order.payments.forEach { payment ->
                    Text(
                        text = "${payment.paymentMethod} • ${payment.paymentStatus}"
                    )
                    Text(
                        text = formatCurrency(payment.amount),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (order.statusHistory.isNotEmpty()) {
                Divider()

                Text(
                    text = "Order history",
                    fontWeight = FontWeight.Bold
                )

                order.statusHistory.forEach { history ->
                    Text(
                        text = "${history.previousStatus ?: "New"} → ${history.newStatus}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatOrderDate(history.changedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    history.notes?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderAmountRow(
    label: String,
    amount: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = formatCurrency(amount),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(
        Locale("en", "ZA")
    ).format(amount)
}

private fun formatOrderDate(date: String): String {
    return try {
        val input = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val output = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getDefault()
        }

        output.format(input.parse(date)!!)
    } catch (_: Exception) {
        date
    }
}