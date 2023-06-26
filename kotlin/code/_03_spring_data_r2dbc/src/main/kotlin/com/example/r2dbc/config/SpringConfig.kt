package com.example.r2dbc.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.cancelAndJoin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.coroutines.CoroutineContext

@Configuration
class SpringConfig {
    @Bean
    fun scope(): CoroutineScope = SpringScope(Dispatchers.IO)
}

class SpringScope(coroutineDispatcher: CoroutineDispatcher) : CoroutineScope, DisposableBean {
    private val logger: Logger = LoggerFactory.getLogger(SpringScope::class.java)
    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext =
        coroutineDispatcher + job + CoroutineExceptionHandler { context, throwable ->
            if (throwable !is CancellationException) {
                val coroutineName = context[CoroutineName] ?: context.toString()
                logger.error("Unhandled exception caught for $coroutineName", throwable)
            }
        }

    override fun destroy() = runBlocking {
        job.cancelAndJoin()
    }
}
