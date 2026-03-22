package dev.cosmicduck.sdk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Exception thrown when the Quantum AI API returns a non-2xx status code.
 *
 * @property statusCode HTTP status code from the response.
 * @property code Error type from the API (e.g. "invalid_request", "rate_limit").
 * @property requestId Unique request identifier from the X-QAI-Request-Id header.
 */
class QuantumApiException(
    val statusCode: Int,
    val code: String,
    override val message: String,
    val requestId: String? = null,
) : Exception(
    buildString {
        append("qai: $statusCode $code: $message")
        if (requestId != null) append(" (request_id=$requestId)")
    }
) {
    /** True if this is a 429 rate limit response. */
    val isRateLimit: Boolean get() = statusCode == 429

    /** True if this is a 401 or 403 authentication/authorization failure. */
    val isAuth: Boolean get() = statusCode == 401 || statusCode == 403

    /** True if this is a 404 not found response. */
    val isNotFound: Boolean get() = statusCode == 404
}

/**
 * Exception thrown for network-level failures (connection timeout, DNS, etc.).
 */
class QuantumNetworkException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Exception thrown when SSE stream parsing fails.
 */
class QuantumStreamException(
    override val message: String,
) : Exception(message)

/** Error response body shape returned by the API. */
@Serializable
internal data class ApiErrorBody(
    val error: ApiErrorDetail? = null,
)

@Serializable
internal data class ApiErrorDetail(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null,
)
