package za.co.skinnercafe.app.data.api

import okhttp3.MultipartBody
import za.co.skinnercafe.app.data.api.UnlinkGoogleAccountRequest

class AuthRepository(
    private val api: MenuApiService = MenuApiClient.service
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return api.login(request)
    }

    suspend fun loginWithGoogle(request: GoogleLoginRequest): AuthResponse {
        return api.loginWithGoogle(request)
    }
    suspend fun linkGoogleAccount(
        request: GoogleLoginRequest
    ): AuthResponse {
        return api.linkGoogleAccount(request)
    }

    suspend fun unlinkGoogleAccount(
        request: UnlinkGoogleAccountRequest
    ): AuthResponse = api.unlinkGoogleAccount(request)

    suspend fun setPassword(
        request: SetPasswordRequest
    ): AuthResponse {
        return api.setPassword(request)
    }

    suspend fun changePassword(
        request: ChangePasswordRequest
    ): AuthResponse {
        return api.changePassword(request)
    }

    suspend fun getCurrentCustomer(): MeResponse {
        return api.getCurrentCustomer()
    }

    suspend fun registerDeviceToken(token: String): AuthResponse {
        return api.registerDeviceToken(
            RegisterDeviceTokenRequest(token)
        )
    }

    suspend fun unregisterDeviceToken(token: String): AuthResponse {
        return api.unregisterDeviceToken(
            RegisterDeviceTokenRequest(token)
        )
    }

    suspend fun logout(deviceToken: String?): AuthResponse {
        if (!deviceToken.isNullOrBlank()) {
            try {
                unregisterDeviceToken(deviceToken)
            } catch (exception: Exception) {
                android.util.Log.e(
                    "SkinnerCafeFCM",
                    "Unable to unregister the device token during logout.",
                    exception
                )
            }
        }

        val response = api.logout()
        MenuApiClient.clearCookies()
        return response
    }

    fun clearLocalSession() {
        MenuApiClient.clearCookies()
    }

    suspend fun uploadProfilePicture(
        file: MultipartBody.Part
    ): AuthResponse {
        return api.uploadProfilePicture(file)
    }

    suspend fun removeProfilePicture(): AuthResponse {
        return api.removeProfilePicture()
    }
}