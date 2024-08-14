package cache

import arrow.atomic.Atomic
import arrow.atomic.update
import arrow.continuations.SuspendApp
import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import io.github.reactivecircus.cache4k.Cache
import io.github.reactivecircus.cache4k.CacheEvent
import io.github.reactivecircus.cache4k.CacheEventListener
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/** Examples of caching using manual strategies vs. `cache4k`. */
object Caching {
  data class User(val id: Id, val name: String, var age: Int, val status: Status) {
    enum class Status {
      SINGLE,
      MARRIED,
      DIVORCED,
    }

    @JvmInline value class Id(val value: Long)
  }

  // Simulate database
  class UsersDB(
    val map: Map<Long, User> =
      mapOf(
        1L to User(User.Id(1), "John Doe", 30, User.Status.SINGLE),
        2L to User(User.Id(2), "Jane Doe", 25, User.Status.MARRIED),
        3L to User(User.Id(3), "Jack Doe", 40, User.Status.DIVORCED),
      )
  ) {
    suspend fun findById(id: Long): User? {
      println("$REPO_NAME Finding user with id: $id...")
      // simulate slow query
      delay(2.seconds.inWholeMilliseconds)
      println("$REPO_NAME User found: $id! Returning...")
      return map[id]
    }

    companion object {
      const val REPO_NAME = "[users-db]"
    }
  }

  // Cache entry with timestamp
  data class UserEntry(val user: User, val timestamp: Instant)

  /** Manual caching using [Atomic] and [timePolicy]. */
  class AtomicCacheService(
    private val usersDB: UsersDB = UsersDB(),
    private val cache: Atomic<MutableMap<Long, UserEntry>> = Atomic(mutableMapOf()),
    private val timePolicy: Duration = 4.seconds,
  ) {
    context(Raise<NotFound>)
    suspend fun findUserById(id: Long): User {
      val instant: Instant = Clock.System.now()
      println("$SERVICE_NAME Finding user with id: $id...")
      val cachedUser =
        cache
          .updateAndGet { map ->
            val entry = map[id]
            if (entry != null) {
              if (entry.timestamp.plus(timePolicy) < Clock.System.now()) {
                println(
                  "$SERVICE_NAME User with id: $id found in cache, but expired, invalidating..."
                )
                map.remove(id)
                map
              } else map
            } else map
          }[id]
          ?.user

      return if (cachedUser != null) {
        println("$SERVICE_NAME User with id: $id found in cache, returning...")
        cachedUser
      } else {
        println("$SERVICE_NAME User with id: $id not found in cache, fetching from database...")
        val user = ensureNotNull(usersDB.findById(id)) { NotFound }

        // update cache
        cache.update { map ->
          map[user.id.value] = UserEntry(user, instant)
          map
        }
        user
      }
    }

    companion object {
      const val SERVICE_NAME = "[all-manual-cache]"
    }
  }

  /** Base service with [usersDB], [cache] and [timePolicy]. */
  abstract class CachedService {
    open val usersDB: UsersDB = UsersDB()
    open val cache: Cache<Long, UserEntry> = Cache.Builder<Long, UserEntry>().build()
    open val timePolicy: Duration = 4.seconds

    context(Raise<NotFound>)
    abstract suspend fun findUserById(id: Long): User?

    /** Invalidates all entries in the [cache]. Should not be exposed to end consumer. */
    suspend fun invalidateAll() {
      cache.invalidateAll()
    }

    /** Expose cache as a map. Could be used for more complex use cases. */
    fun getCache(): Map<in Long, UserEntry> = cache.asMap()
  }

  /** Cached service, with [cache] and manual invalidation using [timePolicy]. */
  class ManualInvalidationService : CachedService() {
    /**
     * Will find the entry in the [cache], if it is not older than [timePolicy]. If the entry is
     * older than [timePolicy], it will be invalidated. If the entry is not in the cache, it will be
     * fetched from the database and added to the cache.
     */
    context(Raise<NotFound>)
    override suspend fun findUserById(id: Long): User {
      val instant: Instant = Clock.System.now()
      val user = ensureNotNull(getCachedOrInvalidate(id)?.user ?: usersDB.findById(id)) { NotFound }
      return user.also { userToCache ->
        println("$SERVICE_NAME Adding entry to cache for user with id: $id...")
        cache.put(userToCache.id.value, UserEntry(userToCache, instant))
      }
    }

    /**
     * Will return the [UserEntry] if it is in the [cache]. If the entry is expired, it will be
     * invalidated. If not, we just return it.
     */
    private fun getCachedOrInvalidate(id: Long): UserEntry? =
      cache.get(id)?.let { entry ->
        val now = Clock.System.now()
        if (entry.timestamp.plus(timePolicy) < Clock.System.now()) {
          println("$SERVICE_NAME Invalidating cache entry for user with id: $id...")
          cache.invalidate(id).let { null }
        } else {
          println("$SERVICE_NAME Returning cached entry for user with id: $id...")
          entry
        }
      }

    companion object {
      const val SERVICE_NAME = "[manual-invalidation-service]"
    }
  }

  /** We can define our own [CacheEventListener] to be notified about events in the [cache]. */
  class UserEventListener(private val logPrefix: String = "") :
    CacheEventListener<Long, UserEntry> {
    override fun onEvent(event: CacheEvent<Long, UserEntry>) {
      when (event) {
        is CacheEvent.Created -> println("$logPrefix Created user with id: ${event.key}")
        is CacheEvent.Updated -> println("$logPrefix Updated user with id: ${event.key}")
        is CacheEvent.Evicted -> println("$logPrefix Evicted user with id: ${event.key}")
        is CacheEvent.Expired -> println("$logPrefix Expired user with id: ${event.key}")
        is CacheEvent.Removed -> println("$logPrefix Removed user with id: ${event.key}")
      }
    }
  }

  /** Cached service, with [cache] and [timePolicy] set to expire after access. */
  class ExpirationService : CachedService() {
    /**
     * Using builder with [Cache.Builder.expireAfterAccess] to set the time policy and event
     * listener.
     */
    override val cache: Cache<Long, UserEntry> =
      Cache.Builder<Long, UserEntry>()
        .eventListener(UserEventListener(logPrefix = SERVICE_NAME))
        .expireAfterAccess(timePolicy)
        .build()

    context(Raise<NotFound>)
    override suspend fun findUserById(id: Long): User {
      println("$SERVICE_NAME Finding user with id: $id...")
      return cache
        .get(id) {
          println("$SERVICE_NAME User with id: $id not found in cache, fetching from database...")
          val instant: Instant = Clock.System.now()
          ensureNotNull(usersDB.findById(id)?.let { user -> UserEntry(user, instant) }) { NotFound }
        }
        .user
    }

    companion object {
      const val SERVICE_NAME = "[expiration-service]"
    }
  }

  sealed class Error(val message: String)

  data object NotFound : Error("User not found")

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val atomicCacheService = AtomicCacheService()
    val manualInvalidationService = ManualInvalidationService()
    val expirationService = ExpirationService()

    recover({
      println("---------------- ATOMIC CACHE SERVICE ----------------")
      atomicCacheService.findUserById(1L) // not cached
      println("--------------------------")
      atomicCacheService.findUserById(1L) // cached if fast
      delay(6.seconds.inWholeMilliseconds)
      println("--------------------------")
      atomicCacheService.findUserById(1L) // cached if fast

      println("---------------- MANUAL INVALIDATION SERVICE ----------------")
      manualInvalidationService.findUserById(1L) // not cached, added to cache
      println("--------------------------")
      manualInvalidationService.findUserById(1L) // cached if fast
      delay(6.seconds.inWholeMilliseconds)
      println("--------------------------")
      manualInvalidationService.findUserById(1L) // expired, but added to cache

      println("---------------- EXPIRATION SERVICE ----------------")
      expirationService.findUserById(1L) // not cached, added to cache
      println("--------------------------")
      expirationService.findUserById(1L) // cached if fast
      delay(6.seconds.inWholeMilliseconds)
      println("--------------------------")
      expirationService.findUserById(1L) // expired, but added to cache
    }) { e: Error ->
      println("Error: $e")
    }
  }
}
