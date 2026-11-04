package za.co.skinnercafe.app.data.api

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val customer: CustomerDto? = null,
    val errors: List<String>? = null,
    val profilePictureUrl: String? = null
)

data class CustomerDto(
    val customerId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null
)

data class MeResponse(
    val customer: CustomerDto
)