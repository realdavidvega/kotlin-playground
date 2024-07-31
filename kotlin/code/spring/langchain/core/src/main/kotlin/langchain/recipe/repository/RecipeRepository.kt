package langchain.recipe.repository

import kotlinx.uuid.SecureRandom
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RecipeRepository : CoroutineCrudRepository<RecipeRepository.Recipe, UUID> {
  @Table("recipes")
  data class Recipe(
    @Id val id: Long? = null,
    @Column("uuid") val uuid: UUID = UUID.generateUUID(SecureRandom),
    @Column("title") val title: String,
    @Column("ingredients") val ingredients: String,
    @Column("instructions") val instructions: String,
  )
}
