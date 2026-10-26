package za.co.skinnercafe.app.data.api

class MenuRepository(
    private val api: MenuApiService = MenuApiClient.service
) {
    suspend fun getCategories(): List<MenuCategory> {
        return api.getCategories()
    }

    suspend fun getProducts(): List<MenuProduct> {
        return api.getProducts()
    }
}
