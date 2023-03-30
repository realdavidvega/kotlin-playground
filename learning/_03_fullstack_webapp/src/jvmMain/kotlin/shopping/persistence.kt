package shopping

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

interface ShoppingPersistence {
    suspend fun listAll(): List<ShoppingListItem>
    suspend fun insertOne(item: ShoppingListItem): Unit
    suspend fun deleteOne(id: String): Unit
}

fun defaultShoppingPersistence(
    collection: CoroutineCollection<ShoppingListItem>
) : ShoppingPersistence = object : ShoppingPersistence {
    override suspend fun listAll(): List<ShoppingListItem> =
        collection.find().toList()

    override suspend fun insertOne(item: ShoppingListItem) {
        collection.insertOne(item)
    }

    override suspend fun deleteOne(id: String) {
        collection.deleteOne(ShoppingListItem::id eq id)
    }
}
