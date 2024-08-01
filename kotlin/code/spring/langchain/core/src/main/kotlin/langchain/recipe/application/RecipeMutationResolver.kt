package langchain.recipe.application

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import langchain.graphql.types.Recipe
import langchain.graphql.types.RecipeInput
import langchain.recipe.domain.RecipeService

@DgsComponent
class RecipeMutationResolver(private val recipeService: RecipeService) {

  @DgsMutation
  suspend fun createRecipe(@InputArgument input: RecipeInput): Recipe =
    recipeService.createRecipe(input)

  @DgsMutation
  suspend fun createRecipeFor(@InputArgument dishName: String): Recipe =
    recipeService.createRecipeFor(dishName)
}
