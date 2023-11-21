package com.springkotlindemo.theater

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TheaterApplication

// starts the application
fun main(args: Array<String>) {
	runApplication<TheaterApplication>(*args)
}

// -- Auto restarting the application when we make changes --
// File -> Settings -> Build, Execution & Deployment -> Compiler -> Check Build Project automatically
// Advanced settings -> Check Allow auto-mate to start even if developed application is currently running
