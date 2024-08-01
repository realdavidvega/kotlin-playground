package langchain.recipe.application

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import langchain.graphql.DgsConstants
import langchain.graphql.types.Recipe

@DgsComponent
class RecipeDataResolver {

  @DgsData(parentType = DgsConstants.RECIPE.TYPE_NAME)
  fun id(dfe: DgsDataFetchingEnvironment): String? = dfe.getSource<Recipe>()?.id

  @DgsData(parentType = DgsConstants.RECIPE.TYPE_NAME)
  fun title(dfe: DgsDataFetchingEnvironment): String? = dfe.getSource<Recipe>()?.title

  @DgsData(parentType = DgsConstants.RECIPE.TYPE_NAME)
  fun ingredients(dfe: DgsDataFetchingEnvironment): String? = dfe.getSource<Recipe>()?.ingredients

  @DgsData(parentType = DgsConstants.RECIPE.TYPE_NAME)
  fun instructions(dfe: DgsDataFetchingEnvironment): String? = dfe.getSource<Recipe>()?.instructions
}
