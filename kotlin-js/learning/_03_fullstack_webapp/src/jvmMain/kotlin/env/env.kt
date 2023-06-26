package env

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

data class Env(
    val http: Http = Http(),
    val mongo: Mongo = Mongo()
) {
    companion object {
        private val config: Config = ConfigFactory.load()
    }

    data class Http(
        val host: String = config.getString("ktor.deployment.host"),
        val port: Int = config.getInt("ktor.deployment.port")
    )

    data class Mongo(
        val uri: String = config.getString("database.mongo.uri"),
        val db: String = config.getString("database.mongo.db")
    )
}
