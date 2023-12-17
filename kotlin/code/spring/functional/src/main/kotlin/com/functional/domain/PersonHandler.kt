package com.functional.domain

import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.functional.infrastructure.PersonDTO
import com.functional.infrastructure.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PersonHandler {
    context(Raise<Error.Internal>)
    suspend fun readAll(): Flow<Person>

    context(Raise<Error>)
    suspend fun readOne(id: Long): Person

    sealed class Error(val message: String) {
        data class NotFound(val id: Long) : Error("Person with id $id not found")
        data object Internal : Error("Something went wrong")
    }

    companion object {
        operator fun invoke(personRepository: PersonRepository): PersonHandler = object : PersonHandler {
            context(Raise<Error.Internal>)
            override suspend fun readAll(): Flow<Person> =
                catch({ personRepository.findAll().map(::toDomain) }) {
                    raise(Error.Internal)
                }

            context(Raise<Error>)
            override suspend fun readOne(id: Long): Person =
                catch({ personRepository.findById(id)?.let(::toDomain) ?: raise(Error.NotFound(id)) }) {
                    raise(Error.Internal)
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
