package com.reactive.r2dbc.user.infrastructure

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserDTO(
  @Id val id: Long? = null,
  @Column("username") val username: String,
  @Column("email") val email: String,
)
