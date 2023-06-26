package com.example.r2dbc.health

import arrow.core.Either
import com.example.r2dbc.users.UserDTO
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
    private val scope: CoroutineScope
) : HealthIndicator {

    override fun health(): Health =
        runBlocking {
            doHealthCheck().fold(
                { Health.outOfService().withException(it).build() },
                { Health.up().build() }
            )
        }

    suspend fun doHealthCheck(): Either<Throwable, Unit> =
        Either.catch {
            withContext(scope.coroutineContext) {
                template.select(UserDTO::class.java)
                    .from("users")
                    .awaitOneOrNull()
                    .let { }
            }
        }.mapLeft { it }
}
