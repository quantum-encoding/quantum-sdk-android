package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Request body for creating a scoped API key.
 */
@Serializable
data class CreateKeyRequest(
    val name: String = "",
    val endpoints: List<String>? = null,
    @SerialName("spend_cap_usd") val spendCapUsd: Double? = null,
    @SerialName("rate_limit") val rateLimit: Int? = null,
)

/**
 * Details about an API key.
 */
@Serializable
data class KeyDetails(
    val id: String = "",
    val name: String = "",
    @SerialName("key_prefix") val keyPrefix: String = "",
    val scope: JsonElement? = null,
    @SerialName("spent_ticks") val spentTicks: Long = 0,
    val revoked: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_used") val lastUsed: String? = null,
)

/**
 * Response from creating a scoped API key.
 */
@Serializable
data class CreateKeyResponse(
    val key: String = "",
    val details: KeyDetails = KeyDetails(),
)

/**
 * Response from listing API keys.
 */
@Serializable
data class ListKeysResponse(
    val keys: List<KeyDetails> = emptyList(),
)
