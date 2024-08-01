package langchain.recipe.domain

import dev.langchain4j.service.Result
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.spring.AiService
import dev.langchain4j.service.spring.AiServiceWiringMode
import langchain.graphql.types.Recipe

@AiService(wiringMode = AiServiceWiringMode.AUTOMATIC, chatModel = "openAiChatModel")
interface RecipeAiService {
  @SystemMessage("A versatile AI assistant capable of creating recipes for any dish")
  fun createRecipeFor(@UserMessage dishName: String): Result<Recipe>
}
