package za.co.skinnercafe.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface MenuApiService {

    @GET("api/Menu/categories")
    suspend fun getCategories(): List<MenuCategory>

    @GET("api/Menu/products")
    suspend fun getProducts(): List<MenuProduct>
}

object MenuApiClient {

    private const val BASE_URL = "http://10.0.2.2:5043/"

    val service: MenuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MenuApiService::class.java)
    }
}
