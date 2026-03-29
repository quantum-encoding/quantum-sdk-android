package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ── Mission Requests ─────────────────────────────────────────────────

/**
 * Request body for creating a mission.
 */
@Serializable
data class MissionCreateRequest(
    val goal: String,
    val strategy: String? = null,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val workers: Map<String, MissionWorkerConfig>? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
)

/**
 * Worker configuration within a mission.
 */
@Serializable
data class MissionWorkerConfig(
    val model: String = "",
    val tier: String = "",
    val description: String? = null,
)

/**
 * Request body for chatting with a mission's architect.
 */
@Serializable
data class MissionChatRequest(
    val message: String,
    val stream: Boolean? = null,
)

/**
 * Request body for updating a mission plan.
 */
@Serializable
data class MissionPlanUpdate(
    val tasks: List<JsonObject>? = null,
    val workers: Map<String, MissionWorkerConfig>? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
    val context: String? = null,
)

/**
 * Request body for confirming/rejecting a mission structure.
 */
@Serializable
data class MissionConfirmStructure(
    val confirmed: Boolean,
    val feedback: String? = null,
)

/**
 * Request body for approving a completed mission.
 */
@Serializable
data class MissionApproveRequest(
    @SerialName("commit_sha") val commitSha: String? = null,
    val comment: String? = null,
)

/**
 * Request body for importing a plan as a new mission.
 */
@Serializable
data class MissionImportRequest(
    val goal: String,
    val strategy: String? = null,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val workers: Map<String, MissionWorkerConfig>? = null,
    val tasks: List<JsonObject> = emptyList(),
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
    @SerialName("auto_execute") val autoExecute: Boolean = false,
)

// ── Mission Responses ────────────────────────────────────────────────

/**
 * Response from mission creation.
 */
@Serializable
data class MissionCreateResponse(
    @SerialName("mission_id") val missionId: String = "",
    val status: String = "",
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val strategy: String? = null,
    val workers: Map<String, MissionWorkerConfig>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("request_id") val requestId: String? = null,
)

/**
 * Mission detail (from GET /missions/{id}).
 */
@Serializable
data class MissionDetail(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val goal: String? = null,
    val strategy: String? = null,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    val error: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("total_steps") val totalSteps: Int = 0,
    @SerialName("session_id") val sessionId: String? = null,
    val result: String? = null,
    val tasks: List<MissionTask> = emptyList(),
    val approved: Boolean = false,
    @SerialName("commit_sha") val commitSha: String? = null,
)

/**
 * A task within a mission.
 */
@Serializable
data class MissionTask(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val worker: String? = null,
    val model: String? = null,
    val status: String? = null,
    val result: String? = null,
    val error: String? = null,
    val step: Int = 0,
    @SerialName("tokens_in") val tokensIn: Int = 0,
    @SerialName("tokens_out") val tokensOut: Int = 0,
)

/**
 * Response from listing missions.
 */
@Serializable
data class MissionListResponse(
    val missions: List<MissionDetail> = emptyList(),
)

/**
 * Response from chatting with the architect.
 */
@Serializable
data class MissionChatResponse(
    @SerialName("mission_id") val missionId: String? = null,
    val content: String? = null,
    val model: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    val usage: MissionChatUsage? = null,
)

/**
 * Token usage for a mission chat response.
 */
@Serializable
data class MissionChatUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
)

/**
 * A git checkpoint within a mission.
 */
@Serializable
data class MissionCheckpoint(
    val id: String? = null,
    @SerialName("commit_sha") val commitSha: String? = null,
    val message: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * Response from listing checkpoints.
 */
@Serializable
data class MissionCheckpointsResponse(
    @SerialName("mission_id") val missionId: String? = null,
    val checkpoints: List<MissionCheckpoint> = emptyList(),
)

/**
 * Generic status response for mission operations.
 */
@Serializable
data class MissionStatusResponse(
    @SerialName("mission_id") val missionId: String? = null,
    val status: String? = null,
    val confirmed: Boolean? = null,
    val approved: Boolean? = null,
    val deleted: Boolean? = null,
    val updated: Boolean? = null,
    @SerialName("commit_sha") val commitSha: String? = null,
)
