package langchain

import com.github.dockerjava.api.model.Image
import dev.langchain4j.model.ollama.OllamaChatModel
import io.kotest.core.spec.style.StringSpec
import org.testcontainers.DockerClientFactory
import org.testcontainers.ollama.OllamaContainer
import org.testcontainers.utility.DockerImageName

@DslMarker annotation class OllamaDsl

class OllamaSpec(private val model: OllamaChatModel) {
  fun String.generates(verbose: Boolean = true, block: (String) -> String?) {
    val msg = model.chat(this)
    if (verbose) println(msg)
    block(msg)
  }
}

fun createOrUseOllamaImage(modelName: String): OllamaContainer {
  val dockerImageName =
    modelName.split(":").let { (prefix, version) -> "tc-ollama-gemma-$prefix-$version" }

  val listImagesCmd: List<Image> =
    DockerClientFactory.lazyClient().listImagesCmd().withImageNameFilter(dockerImageName).exec()

  return if (listImagesCmd.isEmpty()) {
    println("Creating a new Ollama container with $modelName image...")
    val ollama = OllamaContainer("ollama/ollama:0.1.26")
    ollama.start()
    ollama.execInContainer("ollama", "pull", modelName)
    ollama.commitToImage(dockerImageName)
    ollama
  } else {
    println("Using existing Ollama container with $modelName image...")
    val ollama =
      OllamaContainer(
        DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("ollama/ollama")
      )
    ollama.start()
    ollama
  }
}

@OllamaDsl
fun ollamaTest(
  modelName: String,
  body:
    context(OllamaSpec, StringSpec)
    () -> Unit,
): StringSpec.() -> Unit = {
  val container = createOrUseOllamaImage(modelName)

  @Suppress("HttpUrlsUsage")
  val model: OllamaChatModel =
    OllamaChatModel.builder()
      .baseUrl("http://${container.host}:${container.firstMappedPort}")
      .modelName(modelName)
      .build()

  body(OllamaSpec(model), this)
}
