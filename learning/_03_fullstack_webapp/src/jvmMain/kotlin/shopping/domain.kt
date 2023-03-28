package shopping

import kotlinx.serialization.Serializable
import randomUUID

@Serializable
data class ShoppingListItem(val desc: String, val priority: Int) {
    val id: String = randomUUID()

    companion object {
        const val path = "/shoppingList"
    }
}
