package language

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.fx.stm.TMap
import arrow.fx.stm.atomically
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import language.Contracts.RegisterController
import language.Contracts.RegisterService
import language.Contracts.UserRepository

/**
 * Kotlin's contracts
 */
object Contracts {

  @JvmInline value class UserId(val value: Int)

  @JvmInline value class Email(val value: String)

  @JvmInline value class Password(val value: String)

  data class User(val userId: UserId, val email: Email, val password: Password)

  sealed interface RegisterError {
    data class Generic(val message: String) : RegisterError

    data object InvalidEmail : RegisterError

    data object InvalidPassword : RegisterError
  }

  data class RegisterRequest(val email: String, val password: String)

  sealed interface RegisterResponse {
    data class Success(val userId: UserId) : RegisterResponse

    data class Failure(val error: RegisterError) : RegisterResponse
  }

  fun interface UserRepository {
    context(Raise<RegisterError>)
    suspend fun save(email: String, password: String): UserId

    companion object {
      operator fun invoke(db: TMap<UserId, User>): UserRepository =
        UserRepository { email, password ->
          catch({
            val userId = UserId(value = Random.nextInt())
            val user = User(userId, Email(email), Password(password))
            atomically { db.insert(userId, user) }
            userId
          }) { e ->
            raise(RegisterError.Generic(e.message ?: "unknown error"))
          }
        }
    }
  }

  fun interface RegisterService {
    suspend fun register(request: RegisterRequest): RegisterResponse

    companion object {
      operator fun invoke(repository: UserRepository): RegisterService =
        RegisterService { request ->
          either {
              val userId = repository.save(request.email, request.password)
              RegisterResponse.Success(userId)
            }
            .getOrElse { error -> RegisterResponse.Failure(error) }
        }
    }
  }

  fun interface RegisterController {
    suspend fun register(request: RegisterRequest): RegisterResponse

    companion object {
      operator fun invoke(registerService: RegisterService): RegisterController =
        RegisterController { request ->
          val response = registerService.register(request)

          // some logging
          when (response) {
            is RegisterResponse.Success -> println("registered user: ${response.userId.value}")
            is RegisterResponse.Failure -> println("failed to register user: ${response.error}")
          }

          // alternative using contracts
          if (isSuccessful(response)) {
            println("registered user: ${response.userId.value}")
          } else {
            println("failed to register user: ${response.error}")
          }
          response
        }
    }
  }

  @OptIn(ExperimentalContracts::class)
  fun isSuccessful(response: RegisterResponse): Boolean {
    contract {
      returns(true) implies (response is RegisterResponse.Success)
      returns(false) implies (response is RegisterResponse.Failure)
    }
    return response is RegisterResponse.Success
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val db: TMap<UserId, User> = TMap.new()
      val userRepo = UserRepository(db)
      val registerService = RegisterService(userRepo)
      val registerController = RegisterController(registerService)
      registerController.register(RegisterRequest("a@a.com", "a"))
    }
  }
}
