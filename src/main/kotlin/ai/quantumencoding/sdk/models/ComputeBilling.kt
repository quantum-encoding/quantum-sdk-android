package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for querying compute billing.
 */
@Serializable
data class BillingRequest(
    @SerialName("instance_id") val instanceId: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
)

/**
 * A single billing line item from BigQuery.
 */
@Serializable
data class BillingEntry(
    @SerialName("instance_id") val instanceId: String = "",
    @SerialName("instance_name") val instanceName: String? = null,
    @SerialName("cost_usd") val costUsd: Double = 0.0,
    @SerialName("usage_hours") val usageHours: Double? = null,
    @SerialName("sku_description") val skuDescription: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
)

/**
 * Response from a compute billing query.
 */
@Serializable
data class BillingResponse(
    val entries: List<BillingEntry> = emptyList(),
    @SerialName("total_cost_usd") val totalCostUsd: Double = 0.0,
)
