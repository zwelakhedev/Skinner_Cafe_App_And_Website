package za.co.skinnercafe.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.skinnercafe.app.data.api.AuthRepository
import za.co.skinnercafe.app.data.api.LoginRequest
import za.co.skinnercafe.app.data.api.RegisterRequest
import za.co.skinnercafe.app.data.api.CustomerDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import okhttp3.MultipartBody

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val customer: CustomerDto? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                repository.login(
                    LoginRequest(email = email, password = password)
                )

                val customer = repository.getCurrentCustomer().customer

                _uiState.value = AuthUiState(customer = customer)
                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                // Create the account. The API also signs the customer in.
                repository.register(
                    RegisterRequest(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password
                    )
                )

                // Retrieve the newly authenticated customer.
                val customer = repository.getCurrentCustomer().customer

                // Store the customer and finish registration.
                _uiState.value = AuthUiState(
                    customer = customer
                )

                onSuccess()

            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                repository.logout()

                // Clear the signed-in customer's details.
                _uiState.value = AuthUiState()

                onSuccess()

            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = getErrorMessage(exception),
                    customer = _uiState.value.customer
                )
            }
        }
    }

    fun uploadProfilePicture(file: MultipartBody.Part) {
        viewModelScope.launch {
            val currentCustomer = _uiState.value.customer

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = repository.uploadProfilePicture(file)

                val updatedCustomer = currentCustomer?.copy(
                    profilePictureUrl = response.profilePictureUrl
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    customer = updatedCustomer,
                    successMessage = response.message
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            val currentCustomer = _uiState.value.customer

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = repository.removeProfilePicture()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    customer = currentCustomer?.copy(
                        profilePictureUrl = null
                    ),
                    successMessage = response.message
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is HttpException -> when (exception.code()) {
                400 -> "Please check the details you entered."
                401 -> "Invalid email or password."
                409 -> "An account with this email may already exist."
                else -> "The server returned an error (${exception.code()})."
            }
            is IOException -> "Unable to connect to Skinner Cafe. Check that the API is running."
            else -> "Something went wrong. Please try again."
        }
    }
}
