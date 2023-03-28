import shopping.ShoppingListItem
import java.util.*

expect fun randomUUID(): String
actual fun randomUUID() = UUID.randomUUID().toString()

val shoppingList = mutableListOf(
    ShoppingListItem("Cucumbers 🥒", 1),
    ShoppingListItem("Tomatoes 🍅", 2),
    ShoppingListItem("Orange Juice 🍊", 3)
)
