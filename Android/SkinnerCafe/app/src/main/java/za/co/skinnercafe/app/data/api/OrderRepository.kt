package za.co.skinnercafe.app.data.api

class OrderRepository(
    private val api: MenuApiService = MenuApiClient.service
) {
    suspend fun getMyOrders(): List<MyOrder> {
        return api.getMyOrders()
    }
}