package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Request body for creating an async job.
 */
@Serializable
data class JobCreateRequest(
    @SerialName("type") val jobType: String = "",
    val params: JsonElement? = null,
)

/**
 * Response from creating an async job.
 */
@Serializable
data class JobCreateResponse(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
)

/**
 * Response from checking the status of an async job.
 */
@Serializable
data class JobStatusResponse(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
    val result: JsonElement? = null,
    val error: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Summary of a job in the list response.
 */
@Serializable
data class JobSummary(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
    @SerialName("type") val jobType: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Response from listing jobs.
 */
@Serializable
data class ListJobsResponse(
    val jobs: List<JobSummary> = emptyList(),
)
