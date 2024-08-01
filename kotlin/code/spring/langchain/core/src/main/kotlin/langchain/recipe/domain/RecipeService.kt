package langchain.recipe.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import langchain.graphql.types.Recipe
import langchain.graphql.types.RecipeInput
import langchain.recipe.infrastructure.RecipeDTO
import langchain.recipe.infrastructure.RecipeRepository
import org.springframework.stereotype.Service

interface RecipeService {
  fun getAllRecipes(): Flow<Recipe>

  suspend fun getRecipeById(id: String): Recipe?

  suspend fun createRecipe(input: RecipeInput): Recipe

  suspend fun createRecipeFor(dishName: String): Recipe
}

@Service
class DefaultRecipeService(
  val recipeAiService: RecipeAiService,
  val recipeRepository: RecipeRepository,
) : RecipeService {
  override fun getAllRecipes(): Flow<Recipe> =
    recipeRepository.findAll().map { recipe -> recipe.toDomain() }

  override suspend fun getRecipeById(id: String): Recipe? =
    recipeRepository.findById(id = id.toLong())?.toDomain()

  override suspend fun createRecipe(input: RecipeInput): Recipe =
    recipeRepository.save(entity = input.fromDomain()).toDomain()

  override suspend fun createRecipeFor(dishName: String): Recipe {
    val recipe = recipeAiService.createRecipeFor(dishName).content().fromDomain()
    return recipeRepository.save(recipe).toDomain()
  }
}

internal fun RecipeDTO.toDomain(): Recipe =
  Recipe(id = id.toString(), title = title, ingredients = ingredients, instructions = instructions)

internal fun RecipeInput.fromDomain(): RecipeDTO =
  RecipeDTO(title = title, ingredients = ingredients, instructions = instructions)

internal fun Recipe.fromDomain(): RecipeDTO =
  RecipeDTO(title = title, ingredients = ingredients, instructions = instructions)
