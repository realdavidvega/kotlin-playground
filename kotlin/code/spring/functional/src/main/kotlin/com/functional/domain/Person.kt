package com.functional.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Person(
  val id: Id,
  val firstName: FirstName,
  val lastName: LastName,
  val birthdate: BirthDate
) {
  @JvmInline @Serializable value class Id(val value: Long)

  @JvmInline @Serializable value class FirstName(val value: String)

  @JvmInline @Serializable value class LastName(val value: String)

  @JvmInline @Serializable value class BirthDate(val value: LocalDate?)

  companion object {
    operator fun invoke(
      id: Long,
      firstName: String,
      lastName: String,
      birthdate: LocalDate?
    ): Person =
      Person(
        id = Id(id),
        firstName = FirstName(firstName),
        lastName = LastName(lastName),
        birthdate = BirthDate(birthdate)
      )
  }
}
