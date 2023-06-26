package typeclasses

import kotlinx.serialization.Serializable

data class User(
    val id: String,
    val name: String,
    val password: String,
    val roles: List<Role>
)

data class Role(
    val id: String,
    val name: String
)

@Serializable
data class UserDTO(
    val name: String,
    val roles: List<String>
)

@Serializable
data class UserCreationDTO(
    val name: String,
    val password: String
)

interface UserDomain {
    fun fromDomain()
}