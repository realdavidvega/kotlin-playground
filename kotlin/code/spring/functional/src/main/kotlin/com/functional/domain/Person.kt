package com.functional.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate? = null
)
