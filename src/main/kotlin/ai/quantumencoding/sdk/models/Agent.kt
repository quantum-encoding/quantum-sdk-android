package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ── Agent ──────────────────────────────────────────────────────────

/**
 * Describes a worker agent in a multi-agent run.
 */
@Serializable
data class AgentWorker(
    val name: String = "",
    val model: String? = null,
    val tier: String? = null,
    val description: String? = null,
)

/**
 * Request body for the agent orchestration endpoint.
 */
@Serializable
data class AgentRequest(
    val task: String,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val workers: List<AgentWorker>? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
)

/**
 * A single event from an agent or mission SSE stream.
 *
 * The backend agentruntime `Event` carries `{type, role, content, data,
 * timestamp, index}` (`internal/agentruntime/runtime.go`) and mission events
 * carry `{type, mission_id, task_id, message, ...}`. The SDK parser unpacks
 * the common stream fields onto this surface so consumers can react to
 * deltas, tool-use, usage, and errors without re-parsing [data]. The raw
 * payload is retained in [data] for event-type-specific access.
 */
@Serializable
data class AgentStreamEvent(
    val type: String = "",
    val data: JsonObject? = null,
    /** True for the terminal `done` event. */
    val done: Boolean = false,
    /** Worker name emitting a content delta, when present. */
    val worker: String? = null,
    /** Text content delta (or mission `message`) for this event. */
    val content: String? = null,
    /** Parsed tool_use block, when this event carries one. */
    @SerialName("tool_use") val toolUse: StreamToolUse? = null,
    /** Token/cost usage, when this event carries a usage report. */
    val usage: ChatUsage? = null,
    /** Error message, when this event is an error. */
    val error: String? = null,
)

// ── Mission ──────────────────────────────────────────────────────────

/**
 * Describes a named worker for a mission.
 */
@Serializable
data class MissionWorker(
    val model: String? = null,
    val tier: String? = null,
    val description: String? = null,
)

/**
 * Request body for the mission orchestration endpoint.
 */
@Serializable
data class MissionRequest(
    val goal: String,
    val strategy: String? = null,
    @SerialName("conductor_model") val conductorModel: String? = null,
    val workers: Map<String, MissionWorker>? = null,
    @SerialName("max_steps") val maxSteps: Int? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("auto_plan") val autoPlan: Boolean? = null,
    @SerialName("context_config") val contextConfig: ContextConfig? = null,
    @SerialName("worker_model") val workerModel: String? = null,
    @SerialName("deployment_id") val deploymentId: String? = null,
    @SerialName("build_command") val buildCommand: String? = null,
    @SerialName("workspace_path") val workspacePath: String? = null,
)

/**
 * Backwards-compatible alias for [AgentWorker].
 */
typealias AgentWorkerConfig = AgentWorker

/**
 * Backwards-compatible alias for [MissionWorker].
 */
typealias MissionWorkerConfig = MissionWorker

/**
 * A single SSE event from an agent run stream.
 */
typealias AgentEvent = AgentStreamEvent

/**
 * A single SSE event from a mission run stream.
 */
typealias MissionEvent = AgentStreamEvent
