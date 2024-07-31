package langchain.recipe.service

import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.spring.AiService
import langchain.recipe.repository.RecipeRepository

@AiService
interface RecipeAiService {
  @SystemMessage("A versatile AI assistant capable of creating and editing recipes.")
  @UserMessage("Create a recipe for: {{it}}")
  fun createRecipeFor(dishName: String): RecipeRepository.Recipe
}
