package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Configuration for session context management.
 */
@Serializable
data class ContextConfig(
    @SerialName("max_tokens") val maxTokens: Long? = null,
    @SerialName("auto_compact") val autoCompact: Boolean? = null,
)

/**
 * A tool result to feed back into the session.
 */
@Serializable
data class ToolResult(
    @SerialName("tool_call_id") val toolCallId: String = "",
    val content: String = "",
    @SerialName("is_error") val isError: Boolean? = null,
)

/**
 * Request body for session-based chat.
 */
@Serializable
data class SessionChatRequest(
    @SerialName("session_id") val sessionId: String? = null,
    val model: String? = null,
    val message: String = "",
    val tools: List<ChatTool>? = null,
    @SerialName("tool_results") val toolResults: List<ToolResult>? = null,
    val stream: Boolean? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("context_config") val contextConfig: ContextConfig? = null,
    /**
     * Reasoning budget: "none"/"low"/"medium"/"high"/"xhigh"/"max". Null =
     * provider default. Mirrors [ChatRequest.reasoningEffort]; the session
     * handler validates the same set.
     */
    @SerialName("reasoning_effort") val reasoningEffort: String? = null,
    /**
     * Provider-specific settings — the same open map as
     * [ChatRequest.providerOptions].
     *
     * The session lane has no `prompt_cache_key`: it derives one from the
     * session ID, so every turn of a session already shares a cache shard.
     */
    @SerialName("provider_options") val providerOptions: JsonObject? = null,
)

/**
 * Context metadata returned with session responses.
 */
@Serializable
data class SessionContext(
    @SerialName("turn_count") val turnCount: Long = 0,
    @SerialName("estimated_tokens") val estimatedTokens: Long = 0,
    val compacted: Boolean = false,
    @SerialName("compaction_note") val compactionNote: String? = null,
)

/**
 * Response from session-based chat.
 */
@Serializable
data class SessionChatResponse(
    @SerialName("session_id") val sessionId: String = "",
    val response: ChatResponse = ChatResponse(),
    val context: SessionContext = SessionContext(),
)

/**
 * A tool execution result from the client (used in SessionChatRequest.toolResults).
 */
@Serializable
data class SessionToolResult(
    @SerialName("tool_call_id") val toolCallId: String = "",
    val content: String = "",
    @SerialName("is_error") val isError: Boolean? = null,
)
