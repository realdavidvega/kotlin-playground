package shopping

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

val client = KMongo.createClient().coroutine
val database = client.getDatabase("shoppingList")
val collection = database.getCollection<ShoppingListItem>()

interface ShoppingPersistence {
    suspend fun listAll(): List<ShoppingListItem>
    suspend fun insertOne(item: ShoppingListItem): Unit
    suspend fun deleteOne(id: String): Unit
}

fun defaultShoppingPersistence() : ShoppingPersistence = object : ShoppingPersistence {
    override suspend fun listAll(): List<ShoppingListItem> =
        collection.find().toList()

    override suspend fun insertOne(item: ShoppingListItem) {
        collection.insertOne(item)
    }

    override suspend fun deleteOne(id: String) {
        collection.deleteOne(ShoppingListItem::id eq id)
    }
}
