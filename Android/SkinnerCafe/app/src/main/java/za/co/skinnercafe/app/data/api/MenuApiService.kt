package za.co.skinnercafe.app.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.HTTP
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import android.content.Context

interface MenuApiService {

    @GET("api/Menu/categories")
    suspend fun getCategories(): List<MenuCategory>

    @GET("api/Menu/products")
    suspend fun getProducts(): List<MenuProduct>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): AuthResponse

    @POST("api/auth/google/link")
    suspend fun linkGoogleAccount(
        @Body request: GoogleLoginRequest
    ): AuthResponse

    @HTTP(
        method = "DELETE",
        path = "api/auth/google/unlink",
        hasBody = true
    )
    suspend fun unlinkGoogleAccount(
        @Body request: UnlinkGoogleAccountRequest
    ): AuthResponse

    @POST("api/auth/password/set")
    suspend fun setPassword(
        @Body request: SetPasswordRequest
    ): AuthResponse

    @POST("api/auth/password/change")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): AuthResponse

    @GET("api/auth/me")
    suspend fun getCurrentCustomer(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(): AuthResponse

    @POST("api/auth/device-token")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): AuthResponse

    @POST("api/auth/device-token/unregister")
    suspend fun unregisterDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): AuthResponse

    @Multipart
    @POST("api/auth/profile-picture")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): AuthResponse

    @DELETE("api/auth/profile-picture")
    suspend fun removeProfilePicture(): AuthResponse

    @POST("api/orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): CreateOrderResponse

    @GET("api/orders/my-orders")
    suspend fun getMyOrders(): List<MyOrder>

    @POST("api/orders/{orderId}/simulate-payment")
    suspend fun simulatePayment(
        @retrofit2.http.Path("orderId") orderId: Int,
        @Body request: SimulatePaymentRequest
    ): SimulatePaymentResponse
}

object MenuApiClient {

    private const val BASE_URL = "https://10.0.2.2:7277/"

    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val cookieJar: SecureCookieJar by lazy {
        check(::applicationContext.isInitialized) {
            "MenuApiClient must be initialized before use."
        }

        SecureCookieJar(applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: MenuApiService by lazy {
        retrofit.create(MenuApiService::class.java)
    }

    fun clearCookies() {
        if (::applicationContext.isInitialized) {
            cookieJar.clear()
        }
    }
}