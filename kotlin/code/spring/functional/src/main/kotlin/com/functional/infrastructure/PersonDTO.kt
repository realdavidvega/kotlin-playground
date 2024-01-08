package com.functional.infrastructure

import kotlinx.datetime.LocalDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
data class PersonDTO(
    @Id val id: Long,
    @Column("firstName") val firstName: String,
    @Column("lastName") val lastName: String,
    @Column("birthdate") val birthdate: LocalDate?,
)
