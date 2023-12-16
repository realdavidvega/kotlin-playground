package com.example.r2dbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ExampleR2DBCApp

fun main(args: Array<String>) {
  runApplication<ExampleR2DBCApp>(*args)
}
