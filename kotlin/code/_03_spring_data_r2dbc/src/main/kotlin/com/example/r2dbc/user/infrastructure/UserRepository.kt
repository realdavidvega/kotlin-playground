package com.example.r2dbc.user.infrastructure

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Repository

@Repository
class ClientUserRepository(private val client: DatabaseClient) {
  suspend fun findById(id: Long): UserDTO? =
    this.client
      .sql("SELECT * FROM users WHERE id = $id")
      .map { row ->
        UserDTO(
          row.get("id") as Long,
          row.get("username") as String,
          row.get("email") as String,
        )
      }
      .awaitOneOrNull()
}

@Repository
class TemplateUserRepository(private val template: R2dbcEntityTemplate) {
  suspend fun findById(id: Long): UserDTO? =
    template
      .select(UserDTO::class.java)
      .from("users")
      .matching(Query.query(Criteria.where("id").`is`(id)))
      .awaitOneOrNull()
}

@Repository
interface CoroutineUserRepository : CoroutineCrudRepository<UserDTO, Long> {
  override suspend fun findById(id: Long): UserDTO?
}
