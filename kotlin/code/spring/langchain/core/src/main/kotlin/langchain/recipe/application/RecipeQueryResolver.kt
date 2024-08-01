package langchain.recipe.application

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.flow.Flow
import langchain.graphql.types.Recipe
import langchain.recipe.domain.RecipeService

@DgsComponent
class RecipeResolver(private val recipeService: RecipeService) {

  @DgsQuery suspend fun recipe(@InputArgument id: String): Recipe? = recipeService.getRecipeById(id)

  @DgsQuery fun recipes(): Flow<Recipe> = recipeService.getAllRecipes()
}
