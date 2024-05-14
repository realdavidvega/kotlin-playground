package resilience

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import arrow.resilience.saga
import arrow.resilience.transact
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

// 3. Arrow's Sagas and SagaScope

object Sagas {

  // Sagas implement transactional logic in distributed systems
  // A Saga provides for each action a set of compensating actions, which are executed if
  // any of the steps in the saga fails. The role of compensating actions is to provide a
  // rollback mechanism.

  data class HttpRequest(val username: String?)

  enum class HttpResponse(val code: Int) {
    OK(200), // success
    BAD_REQUEST(400), // invalid request, such as missing username
    INTERNAL_SERVER_ERROR(500), // some unhandled exception
  }

  sealed interface AccountCreation

  data object InvalidUsername : AccountCreation

  data object UserAlreadyExists : AccountCreation

  data object AccountAlreadyExists : AccountCreation

  @JvmInline value class UserId(val value: Int)

  data class User(val id: UserId, val username: String)

  @JvmInline value class AccountId(val value: Int)

  data class Account(val id: AccountId, val userId: UserId, val balance: BigDecimal)

  private val USERS_PERSISTENCE: MutableMap<UserId, User> = mutableMapOf()
  private val ACCOUNTS_PERSISTENCE: MutableMap<AccountId, Account> = mutableMapOf()

  // Some service to create users, which can fail if the user already exists
  class UserService {
    context(Raise<UserAlreadyExists>)
    suspend fun createUser(username: String): User =
      withContext(Dispatchers.IO) {
        delay(1000)
        ensure(USERS_PERSISTENCE.values.none { user -> user.username == username }) {
          UserAlreadyExists
        }
        val userId = UserId(USERS_PERSISTENCE.size)
        val user = User(userId, username)
        USERS_PERSISTENCE[userId] = user
        user
      }

    suspend fun rollbackCreateUser(username: String) {
      withContext(Dispatchers.IO) {
        delay(250)
        USERS_PERSISTENCE.values
          .find { it.username == username }
          ?.let { user -> USERS_PERSISTENCE.remove(user.id) }
      }
    }
  }

  // Some service to create accounts, which can fail if the account already exists
  class AccountService {
    context(Raise<AccountAlreadyExists>)
    suspend fun createAccount(userId: UserId): Account {
      return withContext(Dispatchers.IO) {
        delay(1000)
        ensure(ACCOUNTS_PERSISTENCE.values.none { account -> account.userId == userId }) {
          AccountAlreadyExists
        }
        val accountId = AccountId(ACCOUNTS_PERSISTENCE.size)
        val account = Account(accountId, userId, BigDecimal(100))
        ACCOUNTS_PERSISTENCE[accountId] = account
        account
      }
    }

    suspend fun rollbackCreateAccount(userId: UserId) {
      withContext(Dispatchers.IO) {
        delay(250)
        ACCOUNTS_PERSISTENCE.values
          .find { it.userId == userId }
          ?.let { account -> ACCOUNTS_PERSISTENCE.remove(account.id) }
      }
    }
  }

  // Let's say we want to create an account to a new user, which can fail if the user already exists
  // or if the account already exists. And we want to rollback the account creation if any of the
  // steps fails

  interface AccountsController {
    suspend fun createAccount(request: HttpRequest): HttpResponse
  }

  class InitialAccountController(
    private val userService: UserService = UserService(),
    private val accountService: AccountService = AccountService(),
  ) : AccountsController {
    override suspend fun createAccount(request: HttpRequest): HttpResponse =
      // with recover, we transform typed errors into server errors
      recover({
        val username = ensureNotNull(request.username) { InvalidUsername }
        val user = userService.createUser(username)
        accountService.createAccount(user.id)
        return HttpResponse.OK
      }) {
        when (it) {
          InvalidUsername -> HttpResponse.BAD_REQUEST
          UserAlreadyExists -> HttpResponse.BAD_REQUEST
          AccountAlreadyExists -> HttpResponse.BAD_REQUEST
        }
      }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // We attempt to create an account to a new user
      InitialAccountController().createAccount(HttpRequest("yoda")).also {
        println("Response: $it")
      }

      println("User 0: ${USERS_PERSISTENCE[UserId(0)]}")
      println("Account 0: ${ACCOUNTS_PERSISTENCE[AccountId(0)]}")
      println("------------------------------")

      // We attempt to create an account to an existing user, so far so good, will get BAD REQUEST
      InitialAccountController().createAccount(HttpRequest("yoda")).also {
        println("Response: $it")
      }

      println("User 0: ${USERS_PERSISTENCE[UserId(0)]}")
      println("Account 1: ${ACCOUNTS_PERSISTENCE[AccountId(0)]}")
      println("------------------------------")

      // But imagine that an error happens in the account service after the user has been created,
      // and we want to rollback the account creation

      fun BOOM() {
        throw RuntimeException("BOOM!")
      }

      val rollbackController =
        object : AccountsController {
          val userService = UserService()
          val accountService = AccountService()

          override suspend fun createAccount(request: HttpRequest): HttpResponse =
            // with catch, we can simulate a server error when some unhandled exception is thrown
            catch({
              // with recover, we transform typed errors into server errors
              recover({
                val username = ensureNotNull(request.username) { InvalidUsername }

                // The saga function creates a new scope where compensating actions can be declared
                // alongside the action to perform.
                val transaction = saga {
                  val user =
                    saga({ userService.createUser(username) }) {
                      // will execute if next steps in the saga fails
                      userService.rollbackCreateUser(request.username)
                    }
                  saga({ accountService.createAccount(user.id) }) {
                    // will execute if next steps in the saga fails
                    accountService.rollbackCreateAccount(user.id)
                  }

                  // no compensation actions here needed, just throw an exception
                  saga({ BOOM() }) {}
                }
                // The resulting Saga<A> doesn't perform any actions, we need to use transact to
                // keep the chain going. Also, the exceptions raised bubbles up to the caller of
                // transact
                transaction.transact()

                HttpResponse.OK
              }) {
                when (it) {
                  InvalidUsername -> HttpResponse.BAD_REQUEST
                  UserAlreadyExists -> HttpResponse.OK
                  AccountAlreadyExists -> HttpResponse.BAD_REQUEST
                }
              }
            }) {
              HttpResponse.INTERNAL_SERVER_ERROR
            }
        }

      rollbackController.createAccount(HttpRequest("obi-wan")).also { println("Response: $it") }

      println("User 1: ${USERS_PERSISTENCE[UserId(1)]}")
      println("Account 1: ${ACCOUNTS_PERSISTENCE[AccountId(1)]}")

      // It's important to notice, that if the exception occurs inside the saga step, the
      // compensating function of that step will not be executed. We suppose that the step was not
      // completed, hence no compensating actions are needed.
    }
  }
}
