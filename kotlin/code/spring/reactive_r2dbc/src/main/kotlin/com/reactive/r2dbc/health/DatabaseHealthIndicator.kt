package com.reactive.r2dbc.health

import arrow.core.raise.catch
import com.reactive.r2dbc.user.infrastructure.UserDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Component

@Component("usersDb")
class DatabaseHealthIndicator(
  private val template: R2dbcEntityTemplate,
  private val scope: CoroutineScope,
) : HealthIndicator {

  override fun health(): Health = runBlocking {
    catch({
      doHealthCheck()
      Health.up().build()
    }) { error ->
      Health.outOfService().withException(error).build()
    }
  }

  suspend fun doHealthCheck(): Unit =
    withContext(scope.coroutineContext) {
      template.select(UserDTO::class.java).from("users").awaitOneOrNull()
    }
}
