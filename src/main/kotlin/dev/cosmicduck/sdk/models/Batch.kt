package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A single job in a batch submission.
 */
@Serializable
data class BatchJobInput(
    val model: String,
    val prompt: String,
    val title: String? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
)

/**
 * Request body for submitting a batch of jobs.
 */
@Serializable
data class BatchSubmitRequest(
    val jobs: List<BatchJobInput>,
)

/**
 * Response from batch submission.
 */
@Serializable
data class BatchSubmitResponse(
    @SerialName("job_ids") val jobIds: List<String> = emptyList(),
    val status: String = "",
)

/**
 * Response from JSONL batch submission.
 */
@Serializable
data class BatchJsonlResponse(
    @SerialName("job_ids") val jobIds: List<String> = emptyList(),
)

/**
 * A single job in the batch jobs list.
 */
@Serializable
data class BatchJobInfo(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
    val model: String? = null,
    val title: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    val result: JsonElement? = null,
    val error: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Response from listing batch jobs.
 */
@Serializable
data class BatchJobsResponse(
    val jobs: List<BatchJobInfo> = emptyList(),
)
