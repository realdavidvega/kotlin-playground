package naps.domain.health

import kotlinx.serialization.Serializable

const val SERVICE_NAME = "naps"

enum class HealthStatus(val status: String) {
    Healthy("Healthy"),
    Unhealthy("Unhealthy")
}

sealed class HealthResponse {
    abstract val serviceName: String
    abstract val status: HealthStatus

    @Serializable
    data class Healthy(
        override val serviceName: String = SERVICE_NAME,
        override val status: HealthStatus = HealthStatus.Healthy,
    ) : HealthResponse()

    @Serializable
    data class Unhealthy(
        val errorDescription: String?,
        override val serviceName: String = SERVICE_NAME,
        override val status: HealthStatus = HealthStatus.Unhealthy
    ) : HealthResponse()
}