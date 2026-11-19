package za.co.skinnercafe.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.skinnercafe.app.data.api.AuthRepository
import za.co.skinnercafe.app.data.api.LoginRequest
import za.co.skinnercafe.app.data.api.RegisterRequest
import za.co.skinnercafe.app.data.api.CustomerDto
import za.co.skinnercafe.app.ui.settings.AppPreferences
import za.co.skinnercafe.app.data.api.GoogleLoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import okhttp3.MultipartBody
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import za.co.skinnercafe.app.data.api.SetPasswordRequest
import za.co.skinnercafe.app.data.api.ChangePasswordRequest
import za.co.skinnercafe.app.data.api.UnlinkGoogleAccountRequest

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val customer: CustomerDto? = null,
    val hasPassword: Boolean = false,
    val isGoogleLinked: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

private suspend fun syncFcmToken() {
    try {
        val token = FirebaseMessaging.getInstance().token.await()

        appPreferences.saveFcmDeviceToken(token)
        repository.registerDeviceToken(token)

    } catch (exception: Exception) {
        android.util.Log.e(
            "SkinnerCafeFCM",
            "Unable to synchronize the FCM device token.",
            exception
        )
    }
}

    private suspend fun refreshAccountState() {
        val account = repository.getCurrentCustomer()

        _uiState.value = _uiState.value.copy(
            customer = account.customer,
            hasPassword = account.hasPassword,
            isGoogleLinked = account.isGoogleLinked
        )
    }

    fun checkSession(
        onAuthenticated: () -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                val account = repository.getCurrentCustomer()

                syncFcmToken()

                _uiState.value = AuthUiState(
                    customer = account.customer,
                    hasPassword = account.hasPassword,
                    isGoogleLinked = account.isGoogleLinked
                )

            } catch (exception: HttpException) {
                if (exception.code() == 401) {
                    repository.clearLocalSession()
                    _uiState.value = AuthUiState()
                    onUnauthenticated()
                } else {
                    _uiState.value = AuthUiState(
                        errorMessage = "Unable to verify your session. Please try again."
                    )
                }

            } catch (exception: IOException) {
                _uiState.value = AuthUiState(
                    errorMessage = "Unable to connect to Skinner Cafe. Check your connection and try again."
                )

            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = "Something went wrong while checking your session. Please try again."
                )
            }
        }
    }

    fun handleSessionExpired() {
        repository.clearLocalSession()
        _uiState.value = AuthUiState()
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                repository.login(
                    LoginRequest(email = email, password = password)
                )

                val account = repository.getCurrentCustomer()

                syncFcmToken()

                _uiState.value = AuthUiState(
                    customer = account.customer,
                    hasPassword = account.hasPassword,
                    isGoogleLinked = account.isGoogleLinked
                )
                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                repository.loginWithGoogle(GoogleLoginRequest(idToken))

                val account = repository.getCurrentCustomer()

                syncFcmToken()

                _uiState.value = AuthUiState(
                    customer = account.customer,
                    hasPassword = account.hasPassword,
                    isGoogleLinked = account.isGoogleLinked
                )
                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }



    fun linkGoogleAccount(
        idToken: String,
        onSessionExpired: () -> Unit
    ) {
        viewModelScope.launch {
            val currentCustomer = _uiState.value.customer

            if (currentCustomer == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please sign in before linking Google."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = repository.linkGoogleAccount(
                    GoogleLoginRequest(idToken)
                )

                refreshAccountState()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = response.message
                )

            } catch (exception: Exception) {

                if (exception is HttpException && exception.code() == 401) {
                    handleSessionExpired()
                    onSessionExpired()
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (exception) {
                        is HttpException -> when (exception.code()) {
                            400 -> "Google sign-in returned an invalid token. Please try again."
                            403 -> "This account cannot link a Google account."
                            409 -> "This Google account cannot be linked. Check that its email matches your customer account."
                            else -> getErrorMessage(exception)
                        }

                        else -> getErrorMessage(exception)
                    }
                )
            }
        }
    }

    fun setPassword(
        idToken: String,
        newPassword: String,
        onSessionExpired: () -> Unit
    ) {
        viewModelScope.launch {
            if (_uiState.value.customer == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please sign in before setting a password."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = repository.setPassword(
                    SetPasswordRequest(idToken, newPassword)
                )

                refreshAccountState()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = response.message
                )

            } catch (exception: Exception) {
                if (exception is HttpException && exception.code() == 401) {
                    handleSessionExpired()
                    onSessionExpired()
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (exception) {
                        is HttpException -> when (exception.code()) {
                            400 -> "The password does not meet the requirements, or Google verification failed."
                            403 -> "This account cannot set a password."
                            409 -> "A password already exists or Google verification failed."
                            else -> getErrorMessage(exception)
                        }
                        else -> getErrorMessage(exception)
                    }
                )
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSessionExpired: () -> Unit
    ) {
        viewModelScope.launch {
            if (_uiState.value.customer == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please sign in before changing your password."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = repository.changePassword(
                    ChangePasswordRequest(
                        currentPassword,
                        newPassword
                    )
                )

                refreshAccountState()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = response.message
                )

            } catch (exception: Exception) {
                if (exception is HttpException && exception.code() == 401) {
                    handleSessionExpired()
                    onSessionExpired()
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (exception) {
                        is HttpException -> when (exception.code()) {
                            400 -> "Your current password may be incorrect, or your new password does not meet the requirements."
                            403 -> "This account cannot change its password."
                            409 -> "Your account does not have a password yet."
                            else -> getErrorMessage(exception)
                        }
                        else -> getErrorMessage(exception)
                    }
                )
            }
        }
    }

    fun unlinkGoogleAccount(
        currentPassword: String,
        onSessionExpired: () -> Unit
    ) {
        viewModelScope.launch {
            if (_uiState.value.customer == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please sign in before unlinking Google."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = repository.unlinkGoogleAccount(
                    UnlinkGoogleAccountRequest(
                        currentPassword = currentPassword
                    )
                )

                refreshAccountState()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = response.message
                )

            } catch (exception: Exception) {
                if (exception is HttpException && exception.code() == 401) {
                    handleSessionExpired()
                    onSessionExpired()
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (exception) {
                        is HttpException -> when (exception.code()) {
                            403 -> "This account cannot unlink Google."
                            409 -> "Create a password before unlinking Google."
                            else -> getErrorMessage(exception)
                        }
                        else -> getErrorMessage(exception)
                    }
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

                // Retrieve the newly authenticated customer and account status.
                val account = repository.getCurrentCustomer()

                // Register this device for customer notifications.
                syncFcmToken()

                // Store the customer and account status.
                _uiState.value = AuthUiState(
                    customer = account.customer,
                    hasPassword = account.hasPassword,
                    isGoogleLinked = account.isGoogleLinked
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
                val deviceToken = appPreferences.fcmDeviceToken.first()
                repository.logout(deviceToken)

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
