package langchain.recipe.resolver

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.uuid.UUID
import langchain.graphql.types.Recipe
import langchain.graphql.types.RecipeInput
import langchain.recipe.repository.RecipeRepository
import langchain.recipe.service.RecipeAiService

@DgsComponent
class RecipeResolver(
  private val recipeRepository: RecipeRepository,
  private val recipeAiService: RecipeAiService,
) {
  @DgsQuery
  suspend fun recipe(@InputArgument id: String): Recipe? =
    recipeRepository.findById(id = UUID(id))?.toDomain()

  @DgsQuery
  fun recipes(): Flow<Recipe> = recipeRepository.findAll().map { recipe -> recipe.toDomain() }

  @DgsMutation
  suspend fun createRecipe(@InputArgument input: RecipeInput): Recipe =
    recipeRepository
      .save(
        entity =
          RecipeRepository.Recipe(
            title = input.title,
            ingredients = input.ingredients,
            instructions = input.instructions,
          )
      )
      .toDomain()

  @DgsMutation
  suspend fun createRecipeFor(@InputArgument dishName: String): Recipe =
    recipeRepository.save(entity = recipeAiService.createRecipeFor(dishName)).toDomain()
}

fun RecipeRepository.Recipe.toDomain(): Recipe =
  Recipe(id = id.toString(), title = title, ingredients = ingredients, instructions = instructions)
