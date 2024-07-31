package langchain

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class LangchainApp

fun main(args: Array<String>) {
  runApplication<LangchainApp>(*args)
}
