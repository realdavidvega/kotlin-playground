package env

import shopping.*

data class Dependencies(
    val shoppingService: ShoppingService
)

fun dependencies() : Dependencies {
    val shoppingPersistence = defaultShoppingPersistence()
    val shoppingService = defaultShoppingService(shoppingPersistence)

    return Dependencies(
        shoppingService
    )
}