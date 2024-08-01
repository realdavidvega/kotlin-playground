package langchain.recipe.infrastructure

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository interface RecipeRepository : CoroutineCrudRepository<RecipeDTO, Long>
