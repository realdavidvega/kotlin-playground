package com.reactive.r2dbc.user.infrastructure

import com.reactive.r2dbc.user.infrastructure.UserRepository.Companion.USERS
import io.r2dbc.spi.Readable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.flow
import org.springframework.stereotype.Repository

interface UserRepository {

  suspend fun save(user: UserDTO): Long?

  suspend fun findUserById(id: Long): UserDTO?

  fun findAllUsers(): Flow<UserDTO>

  companion object {
    const val USERS = "users"
  }
}

/** Implementation using the actual Repository */
@Repository
class DefaultUserRepository(private val repository: CoroutineCrudUserRepository) : UserRepository {
  override suspend fun save(user: UserDTO): Long? = repository.save(user).id

  override suspend fun findUserById(id: Long): UserDTO? = repository.findById(id)

  override fun findAllUsers(): Flow<UserDTO> = repository.findAll()
}

/*
 * First example of repository using DatabaseClient with Spring R2DBC Kotlin extensions
 */
class ClientUserRepository(private val client: DatabaseClient) : UserRepository {
  override suspend fun save(user: UserDTO): Long? =
    this.client
      .sql("INSERT INTO $USERS (username, email) VALUES (:username, :email)")
      .map(::toDTO)
      .awaitOneOrNull()
      ?.id

  override suspend fun findUserById(id: Long): UserDTO? =
    this.client.sql("SELECT * FROM $USERS WHERE id = $id").map(::toDTO).awaitOneOrNull()

  override fun findAllUsers(): Flow<UserDTO> =
    this.client.sql("SELECT * FROM $USERS").map(::toDTO).flow()

  private fun toDTO(row: Readable): UserDTO =
    UserDTO(row["id"] as Long, row["username"] as String, row["email"] as String)
}

/*
 * Second example of repository using R2dbcEntityTemplate with Spring R2DBC Kotlin extensions
 */
class TemplateUserRepository(private val template: R2dbcEntityTemplate) : UserRepository {
  override suspend fun save(user: UserDTO): Long? = template.insert(user).awaitSingleOrNull()?.id

  override suspend fun findUserById(id: Long): UserDTO? =
    template
      .select(UserDTO::class.java)
      .from(USERS)
      .matching(Query.query(Criteria.where("id").`is`(id)))
      .awaitOneOrNull()

  override fun findAllUsers(): Flow<UserDTO> =
    template.select(UserDTO::class.java).from(USERS).flow()
}

/*
 * Third example of repository using CoroutineCrudRepository, which allows to access the DB in a
 * reactive way using support for Kotlin coroutines.
 *
 * This is the default repository used in this application.
 */
interface CoroutineCrudUserRepository : CoroutineCrudRepository<UserDTO, Long> {
  override suspend fun findById(id: Long): UserDTO?

  override fun findAll(): Flow<UserDTO>
}
