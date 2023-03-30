package shopping

import shoppingList

interface ShoppingService {
    suspend fun listAll(): List<ShoppingListItem>
    suspend fun create(item: ShoppingListItem): Unit
    suspend fun delete(id: String): Unit
}

fun stubShoppingService() : ShoppingService = object : ShoppingService {
    override suspend fun listAll(): List<ShoppingListItem> = shoppingList

    override suspend fun create(item: ShoppingListItem) {
        shoppingList += item
    }

    override suspend fun delete(id: String) {
        shoppingList.removeIf { it.id == id }
    }
}

fun defaultShoppingService(
    persistence: ShoppingPersistence
) : ShoppingService = object : ShoppingService {
    override suspend fun listAll(): List<ShoppingListItem> =
        persistence.listAll()

    override suspend fun create(item: ShoppingListItem) {
        persistence.insertOne(item)
    }

    override suspend fun delete(id: String) {
        persistence.deleteOne(id)
    }
}
