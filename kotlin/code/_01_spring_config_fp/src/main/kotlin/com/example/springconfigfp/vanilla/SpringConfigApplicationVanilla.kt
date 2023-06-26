package com.example.springconfigfp.vanilla

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.*

@SpringBootApplication
class SpringConfigApplicationVanilla {}

fun main(args: Array<String>) {
	runApplication<SpringConfigApplicationVanilla>(*args)
}

@RestController
class PersonController(private val personRepository: PersonRepository) {

	@GetMapping("/person")
	fun readAll(): List<Person> = personRepository.findAll()

	@GetMapping("/person/{id}")
	fun readOne(@PathVariable id: Long) = personRepository.findById(id)
}

@Entity
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : JpaRepository<Person, Long>
