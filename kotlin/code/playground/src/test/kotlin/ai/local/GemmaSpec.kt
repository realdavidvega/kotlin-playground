package ai.local

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

class GemmaSpec : StringSpec(
  ollamaTest(modelName = "gemma:2b") {
    "should guess cities correctly" {
      "The capital of France is".generates {
        it shouldContain "Paris"
      }
    }
  }
)
