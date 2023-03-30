package env

import com.mongodb.ConnectionString
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import shopping.*

data class Dependencies(
    val shoppingService: ShoppingService
)

fun dependencies(
    env: Env
) : Dependencies {
    val collection = databaseCollection(env)
    val shoppingPersistence = defaultShoppingPersistence(collection)
    val shoppingService = defaultShoppingService(shoppingPersistence)

    return Dependencies(
        shoppingService
    )
}

fun databaseCollection(env: Env): CoroutineCollection<ShoppingListItem> {
    val connectionString = ConnectionString("${env.mongo.uri}?retryWrites=false")
    val client = KMongo.createClient(connectionString).coroutine
    val database = client.getDatabase(connectionString.database ?: env.mongo.db)

    return database.getCollection<ShoppingListItem>()
}
