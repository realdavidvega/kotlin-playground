package com.functional.infrastructure

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PersonRepository : CoroutineCrudRepository<PersonDTO, Long> {
  override suspend fun findById(id: Long): PersonDTO?

  override fun findAll(): Flow<PersonDTO>
}
