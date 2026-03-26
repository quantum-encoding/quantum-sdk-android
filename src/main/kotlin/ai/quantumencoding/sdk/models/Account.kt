package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response from the account balance endpoint.
 */
@Serializable
data class BalanceResponse(
    @SerialName("user_id") val userId: String = "",
    @SerialName("credit_ticks") val creditTicks: Long = 0,
    @SerialName("credit_usd") val creditUsd: Double = 0.0,
    @SerialName("ticks_per_usd") val ticksPerUsd: Long = 0,
)

/**
 * A single usage history entry.
 */
@Serializable
data class UsageEntry(
    val id: String = "",
    @SerialName("request_id") val requestId: String? = null,
    val model: String? = null,
    val provider: String? = null,
    val endpoint: String? = null,
    @SerialName("delta_ticks") val deltaTicks: Long? = null,
    @SerialName("balance_after") val balanceAfter: Long? = null,
    @SerialName("input_tokens") val inputTokens: Long? = null,
    @SerialName("output_tokens") val outputTokens: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * Response from the usage history endpoint.
 */
@Serializable
data class UsageResponse(
    val entries: List<UsageEntry> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_cursor") val nextCursor: String? = null,
)

/**
 * Query parameters for usage history.
 */
data class UsageQuery(
    val limit: Int? = null,
    @SerialName("start_after") val startAfter: String? = null,
)

/**
 * Monthly usage summary.
 */
@Serializable
data class UsageSummaryMonth(
    val month: String = "",
    @SerialName("total_requests") val totalRequests: Long = 0,
    @SerialName("total_input_tokens") val totalInputTokens: Long = 0,
    @SerialName("total_output_tokens") val totalOutputTokens: Long = 0,
    @SerialName("total_cost_usd") val totalCostUsd: Double = 0.0,
    @SerialName("total_margin_usd") val totalMarginUsd: Double = 0.0,
    @SerialName("by_provider") val byProvider: List<JsonElement> = emptyList(),
)

/**
 * Response from the usage summary endpoint.
 */
@Serializable
data class UsageSummaryResponse(
    val months: List<UsageSummaryMonth> = emptyList(),
)

/**
 * A pricing entry for a model.
 */
@Serializable
data class PricingEntry(
    val provider: String = "",
    val model: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("input_per_million") val inputPerMillion: Double = 0.0,
    @SerialName("output_per_million") val outputPerMillion: Double = 0.0,
    @SerialName("cached_per_million") val cachedPerMillion: Double = 0.0,
)

/**
 * Response from the account pricing endpoint.
 */
@Serializable
data class PricingResponse(
    val pricing: Map<String, PricingEntry> = emptyMap(),
)

// ── Models / Pricing ─────────────────────────────────────────────────

/**
 * Information about an available model.
 */
@Serializable
data class ModelInfo(
    val id: String = "",
    val provider: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("input_per_million") val inputPerMillion: Double = 0.0,
    @SerialName("output_per_million") val outputPerMillion: Double = 0.0,
)

/**
 * Pricing information for a model.
 */
@Serializable
data class PricingInfo(
    val id: String = "",
    val provider: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("input_per_million") val inputPerMillion: Double = 0.0,
    @SerialName("output_per_million") val outputPerMillion: Double = 0.0,
)

/**
 * Response wrapper for models list.
 */
@Serializable
internal data class ModelsResponseBody(
    val models: List<ModelInfo> = emptyList(),
)

/**
 * Response wrapper for pricing list.
 */
@Serializable
internal data class PricingResponseBody(
    val pricing: List<PricingInfo> = emptyList(),
)

// ── Contact ──────────────────────────────────────────────────────────

/**
 * Request body for the contact form (public endpoint, no auth required).
 */
@Serializable
data class ContactRequest(
    val name: String,
    val email: String,
    val subject: String? = null,
    val message: String,
)

// ── Generic Status ───────────────────────────────────────────────────

/**
 * Generic status response used by many endpoints.
 */
@Serializable
data class StatusResponse(
    val status: String = "",
    val message: String? = null,
)

/**
 * Response from the contact form endpoint.
 */
@Serializable
data class ContactResponse(
    val status: String = "",
    val message: String? = null,
)
