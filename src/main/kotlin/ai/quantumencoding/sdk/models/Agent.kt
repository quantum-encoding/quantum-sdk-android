package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for the agent orchestration endpoint.
 *
 * @property task The task or goal for the agent to accomplish.
 * @property conductorModel Model for the conductor (default: server picks).
 * @property workers Worker configurations.
 * @property maxSteps Maximum number of orchestration steps.
 * @property systemPrompt System prompt for the conductor.
 */
@Serializable
data class AgentRequest(
    val task: String,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val workers: List<AgentWorkerConfig>? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
)

/**
 * Configuration for an agent worker.
 */
@Serializable
data class AgentWorkerConfig(
    val name: String,
    val model: String? = null,
    val tools: List<ChatTool>? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
)

/**
 * A parsed SSE event from an agent orchestration stream.
 */
data class AgentEvent(
    val type: String,
    val done: Boolean = false,
    val worker: String? = null,
    val content: String? = null,
    val toolUse: StreamToolUse? = null,
    val usage: ChatUsage? = null,
    val error: String? = null,
)

// ── Mission ──────────────────────────────────────────────────────────

/**
 * Request body for the mission orchestration endpoint.
 *
 * @property goal The goal for the mission.
 * @property conductorModel Model for the conductor.
 * @property workers Worker configurations.
 * @property maxSteps Maximum orchestration steps.
 */
@Serializable
data class MissionRequest(
    val goal: String,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val workers: List<MissionWorkerConfig>? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
)

/**
 * Configuration for a mission worker.
 */
@Serializable
data class MissionWorkerConfig(
    val name: String,
    val model: String? = null,
    val tools: List<ChatTool>? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
)

/**
 * A parsed SSE event from a mission orchestration stream.
 */
data class MissionEvent(
    val type: String,
    val done: Boolean = false,
    val worker: String? = null,
    val content: String? = null,
    val toolUse: StreamToolUse? = null,
    val usage: ChatUsage? = null,
    val error: String? = null,
)
