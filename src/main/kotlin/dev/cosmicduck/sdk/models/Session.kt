package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for the session-based chat endpoint.
 * The server manages conversation history automatically.
 *
 * @property sessionId Session ID. Omit to create a new session.
 * @property model Model to use for generation.
 * @property message The user message.
 * @property tools Tools the model can call.
 * @property toolResults Tool results from previous calls.
 * @property stream Enable streaming.
 * @property systemPrompt System prompt.
 * @property contextConfig Context management configuration.
 * @property providerOptions Provider-specific settings.
 */
@Serializable
data class SessionChatRequest(
    @SerialName("session_id") val sessionId: String? = null,
    val model: String? = null,
    val message: String,
    val tools: List<ChatTool>? = null,
    @SerialName("tool_results") val toolResults: List<SessionToolResult>? = null,
    val stream: Boolean? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("context_config") val contextConfig: ContextConfig? = null,
    @SerialName("provider_options") val providerOptions: JsonObject? = null,
)

/**
 * A tool result to return to the model in a session conversation.
 */
@Serializable
data class SessionToolResult(
    @SerialName("tool_call_id") val toolCallId: String,
    val content: String,
    @SerialName("is_error") val isError: Boolean? = null,
)

/**
 * Context management configuration for session chat.
 */
@Serializable
data class ContextConfig(
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("auto_compact") val autoCompact: Boolean? = null,
)

/**
 * Metadata about the session context state.
 */
@Serializable
data class ContextMetadata(
    @SerialName("turn_count") val turnCount: Int = 0,
    @SerialName("estimated_tokens") val estimatedTokens: Int = 0,
    val compacted: Boolean = false,
    @SerialName("compaction_note") val compactionNote: String? = null,
)

/**
 * Response from the session-based chat endpoint.
 */
@Serializable
data class SessionChatResponse(
    @SerialName("session_id") val sessionId: String = "",
    val response: ChatResponse = ChatResponse(),
    val context: ContextMetadata = ContextMetadata(),
)
