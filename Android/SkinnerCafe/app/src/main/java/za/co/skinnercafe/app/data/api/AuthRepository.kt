package za.co.skinnercafe.app.data.api

import okhttp3.MultipartBody

class AuthRepository(
    private val api: MenuApiService = MenuApiClient.service
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return api.login(request)
    }

    suspend fun getCurrentCustomer(): MeResponse {
        return api.getCurrentCustomer()
    }

    suspend fun logout(): AuthResponse {
        return api.logout()
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