@file:Suppress("Unused", "MagicNumber")

package typeclasses

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.json.Json.Default.encodeToString

object Serialization {
  @Serializable abstract class Book

  @Serializable data class Textbook(val name: String) : Book()

  sealed class User {
    abstract val id: Int
    abstract val name: String

    @Serializable
    data class StandardUser(override val id: Int, override val name: String, val level: String) :
      User() {
      fun promote(level: String, credit: Double): PremiumUser = PremiumUser(id, name, level, credit)
    }

    @Serializable
    data class PremiumUser(
      override val id: Int,
      override val name: String,
      val level: String,
      val credit: Double
    ) : User()
  }

  interface ContentConverter<T> {
    suspend fun toContent(content: T, contentType: ContentType): OutgoingContent

    suspend fun fromContent(contentType: ContentType, content: ByteReadChannel): T
  }

  class GenericConverter<A>(private val serializer: KSerializer<A>) : ContentConverter<A> {
    override suspend fun toContent(content: A, contentType: ContentType): OutgoingContent {
      val json = Json.encodeToString(serializer, content)
      return TextContent(json, contentType)
    }

    override suspend fun fromContent(contentType: ContentType, content: ByteReadChannel): A {
      val json = content.readRemaining().readText()
      return Json.decodeFromString(serializer, json)
    }
  }

  class UserConverter<T : User>(private val serializer: KSerializer<T>) : ContentConverter<T> {
    override suspend fun toContent(content: T, contentType: ContentType): OutgoingContent {
      val json = encodeToString(serializer, content)
      return TextContent(json, contentType)
    }

    override suspend fun fromContent(contentType: ContentType, content: ByteReadChannel): T {
      val bytes = content.readRemaining().readBytes()
      val json = String(bytes)
      return decodeFromString(serializer, json)
    }
  }

  fun main(): Unit = runBlocking {
    val user = User.StandardUser(1, "David", "Associate")
    println(user)

    val converter = GenericConverter(User.StandardUser.serializer())
    val json = converter.toContent(user, ContentType.Application.Json)
    println(json)

    // more type-safe way
    val standardUserConverter = UserConverter(User.StandardUser.serializer())
    val standard = standardUserConverter.toContent(user, ContentType.Application.Json)
    println(standard)

    val premiumUserConverter = UserConverter(User.PremiumUser.serializer())
    val premiumUser = user.promote("Silver", 100.0)
    val premium = premiumUserConverter.toContent(premiumUser, ContentType.Application.Json)
    println(premium)

    // won't work
    // val bookConverter = UserConverter(Book.serializer())

    val bookConverter = GenericConverter(Textbook.serializer())
    val textbook = Textbook("The fantastic adventures of a Kotliner")
    val book = bookConverter.toContent(textbook, ContentType.Application.Json)
    println(book)
  }
}
