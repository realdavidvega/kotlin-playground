package com.functional.domain

import arrow.core.raise.Raise
import com.functional.infrastructure.PersonDTO
import com.functional.infrastructure.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PersonHandler {
    context(Raise<Error.GenericError>)
    suspend fun readAll(): Flow<Person>

    context(Raise<Error>)
    suspend fun readOne(id: Long): Person

    sealed class Error(val message: String) {
        data class PersonNotFound(val id: Long) : Error("Person with id $id not found")
        data object GenericError : Error("Something went wrong")
    }

    companion object {
        operator fun invoke(personRepository: PersonRepository): PersonHandler = object : PersonHandler {
            context(Raise<Error.GenericError>)
            override suspend fun readAll(): Flow<Person> =
                personRepository.findAll().map(::toDomain)

            context(Raise<Error>)
            override suspend fun readOne(id: Long): Person =
                personRepository.findById(id)?.let(::toDomain) ?: raise(Error.PersonNotFound(id))

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
