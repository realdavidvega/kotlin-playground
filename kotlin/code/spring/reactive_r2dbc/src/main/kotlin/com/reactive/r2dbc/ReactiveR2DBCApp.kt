package com.reactive.r2dbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ReactiveR2DBCApp

fun main(args: Array<String>) {
  runApplication<ReactiveR2DBCApp>(*args)
}
