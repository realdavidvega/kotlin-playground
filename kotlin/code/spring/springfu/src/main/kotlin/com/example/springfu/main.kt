package com.example.springfu

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.support.beans

@SpringBootApplication
class Application

fun application(): SpringApplication =
    springApplication<Application> {
        addInitializers {
            listOf(
                beans {

                }
            )
        }
    }

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    application().run(*args)
}
