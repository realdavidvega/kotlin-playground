package com.functional.domain

import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.functional.infrastructure.PersonDTO
import com.functional.infrastructure.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PersonHandler {
  context(Raise<Error>)
  suspend fun readOne(id: Long): Person

  context(Raise<Error.Internal>)
  fun readAll(): Flow<Person>

  sealed class Error(val message: String) {
    data class NotFound(val id: Long) : Error("Person with id $id not found")

    data class Internal(val detail: String) : Error("Something went wrong")
  }

  companion object {
    operator fun invoke(repository: PersonRepository): PersonHandler =
      object : PersonHandler {
        context(Raise<Error>)
        override suspend fun readOne(id: Long): Person =
          catch({ repository.findById(id)?.let(::toDomain) ?: raise(Error.NotFound(id)) }) { e ->
            raise(Error.Internal(e.message ?: "Unknown error"))
          }

        context(Raise<Error.Internal>)
        override fun readAll(): Flow<Person> =
          catch({ repository.findAll().map(::toDomain) }) { e ->
            raise(Error.Internal(e.message ?: "Unknown error"))
          }

        private fun toDomain(person: PersonDTO): Person =
          Person(
            id = person.id,
            firstName = person.firstName,
            lastName = person.lastName,
            birthdate = person.birthdate
          )
      }
  }
}
