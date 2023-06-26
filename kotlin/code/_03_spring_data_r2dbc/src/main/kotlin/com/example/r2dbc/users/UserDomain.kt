package com.example.r2dbc.users

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

interface UserDomain {
    fun toDomain(): User
}

@Table("users")
data class UserDTO(
    @Id val id: Long,
    @Column("username") val username: String,
    @Column("email") val email: String,
) : UserDomain {
    override fun toDomain(): User =
        User(username, email)
}

data class User(
    val username: String,
    val email: String
)
