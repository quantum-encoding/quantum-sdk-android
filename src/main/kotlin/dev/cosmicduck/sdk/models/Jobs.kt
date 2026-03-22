package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Request body for creating an async job.
 *
 * @property type Job type (e.g. "3d/generate").
 * @property params Job parameters.
 */
@Serializable
data class JobCreateRequest(
    val type: String,
    val params: Map<String, JsonElement>,
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
 * A job in the jobs list.
 */
@Serializable
data class JobListItem(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
    val type: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Response from listing jobs.
 */
@Serializable
data class JobListResponse(
    val jobs: List<JobListItem> = emptyList(),
)

// ── 3D Generation ────────────────────────────────────────────────────

/**
 * Request body for 3D model generation via the jobs system.
 *
 * @property model Model for 3D generation (e.g. "meshy-6").
 * @property prompt Text prompt.
 * @property imageUrl Image URL for image-to-3D.
 */
data class Generate3DRequest(
    val model: String,
    val prompt: String? = null,
    val imageUrl: String? = null,
)
