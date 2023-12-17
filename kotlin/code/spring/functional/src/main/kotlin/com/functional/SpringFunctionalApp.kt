package com.functional

import com.functional.application.PersonController
import com.functional.config.DatabaseConfig
import com.functional.domain.PersonHandler
import com.functional.infrastructure.PersonRepository
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.beans
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    application().run(*args)
}

@SpringBootApplication
@EnableR2dbcRepositories
class SpringFunctionalApp

fun application(): SpringApplication =
    springApplication<SpringFunctionalApp> {
        addInitializers {
            listOf(
                beans {
                    databaseConfig()
                    personController()
                }
            )
        }
    }

private fun BeanDefinitionDsl.databaseConfig(): Unit {
    with(DatabaseConfig()) {
        bean { connectionFactory() }
        bean {
            val connectionFactory = ref<ConnectionFactory>()
            databaseInitializer(connectionFactory)
        }
    }
}

private fun BeanDefinitionDsl.personController(): Unit =
    bean {
        val repository = ref<PersonRepository>()
        val handler = PersonHandler(repository)
        PersonController(handler)
    }
