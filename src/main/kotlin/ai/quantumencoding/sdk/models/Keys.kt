package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating a scoped API key.
 *
 * @property name Display name for the key.
 * @property scopes Permission scopes.
 * @property expiresAt Expiration timestamp (ISO 8601).
 */
@Serializable
data class CreateKeyRequest(
    val name: String,
    val scopes: List<String>? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

/**
 * Response from creating a scoped API key.
 */
@Serializable
data class CreateKeyResponse(
    val key: String = "",
    val id: String = "",
)

/**
 * Details about an API key.
 */
@Serializable
data class KeyDetails(
    val id: String = "",
    val name: String = "",
    val prefix: String = "",
    val scopes: List<String>? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("last_used_at") val lastUsedAt: String? = null,
)

/**
 * Response from listing API keys.
 */
@Serializable
data class ListKeysResponse(
    val keys: List<KeyDetails> = emptyList(),
)
