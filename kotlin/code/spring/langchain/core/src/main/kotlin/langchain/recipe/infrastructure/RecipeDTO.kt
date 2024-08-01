package langchain.recipe.infrastructure

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("recipes")
data class RecipeDTO(
  @Id val id: Long? = null,
  @Column("title") val title: String,
  @Column("ingredients") val ingredients: String,
  @Column("instructions") val instructions: String,
)
