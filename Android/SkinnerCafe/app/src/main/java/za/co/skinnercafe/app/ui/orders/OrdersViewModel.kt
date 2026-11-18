package za.co.skinnercafe.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.skinnercafe.app.data.api.MyOrder
import za.co.skinnercafe.app.data.api.OrderRepository
import java.io.IOException

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<MyOrder> = emptyList(),
    val errorMessage: String? = null
)

class OrdersViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    fun loadMyOrders(onSessionExpired: () -> Unit = {}) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val orders = repository.getMyOrders()

                _uiState.value = OrdersUiState(
                    orders = orders
                )
            } catch (exception: Exception) {
                if (exception is HttpException && exception.code() == 401) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Your session has expired. Please sign in again."
                    )

                    onSessionExpired()
                    return@launch
                }
                val message = when (exception) {
                    is HttpException -> when (exception.code()) {
                        401 -> "Please sign in again to view your orders."
                        else -> "Unable to load orders (server error ${exception.code()})."
                    }
                    is IOException -> "Unable to connect to Skinner Cafe. Check that the API is running."
                    else -> "Something went wrong while loading your orders."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
        }
    }
}