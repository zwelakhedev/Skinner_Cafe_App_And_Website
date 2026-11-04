package za.co.skinnercafe.app.data.api

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import java.net.CookieManager
import java.net.CookiePolicy

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

    @GET("api/auth/me")
    suspend fun getCurrentCustomer(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(): AuthResponse

    @Multipart
    @POST("api/auth/profile-picture")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): AuthResponse

    @DELETE("api/auth/profile-picture")
    suspend fun removeProfilePicture(): AuthResponse

}

object MenuApiClient {

    // ASP.NET Core API HTTPS endpoint on the Android emulator.
    private const val BASE_URL = "https://10.0.2.2:7277/"

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
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
}