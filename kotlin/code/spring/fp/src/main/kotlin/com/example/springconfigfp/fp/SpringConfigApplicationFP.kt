package com.example.springconfigfp.fp

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router
import java.time.LocalDate


// spring boot app is a configuration class
@SpringBootApplication
class SpringConfigApplicationFP

fun beans() = beans {
	bean {
		router {
			"/person".nest {
				val handler = PersonHandler(ref())
				GET("", handler::readAll)
				GET("/{id}", handler::readOne)
			}
		}
	}
}

class PersonHandler(private val personRepository: PersonRepository) {
	fun readAll(request: ServerRequest) = ServerResponse.ok().body(
		personRepository.findAll()
	)
	fun readOne(request: ServerRequest) = ServerResponse.ok().body(
		personRepository.findById(request.pathVariable("id").toLong())
	)
}

fun main(args: Array<String>) {
	runApplication<SpringConfigApplicationFP>(*args) {
		addInitializers(beans())
	}
}

@Entity
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : JpaRepository<Person, Long>
