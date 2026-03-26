package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ── Chat Request / Response ──────────────────────────────────────────

/**
 * Request body for the chat completion endpoint.
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val tools: List<ChatTool>? = null,
    val stream: Boolean? = null,
    val temperature: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("provider_options") val providerOptions: JsonObject? = null,
)

/**
 * A single message in a chat conversation.
 */
@Serializable
data class ChatMessage(
    val role: String,
    val content: String? = null,
    @SerialName("content_blocks") val contentBlocks: List<ContentBlock>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    @SerialName("is_error") val isError: Boolean? = null,
) {
    companion object {
        fun system(content: String) = ChatMessage(role = "system", content = content)
        fun user(content: String) = ChatMessage(role = "user", content = content)
        fun assistant(content: String) = ChatMessage(role = "assistant", content = content)
        fun toolResult(toolCallId: String, content: String, isError: Boolean = false) =
            ChatMessage(role = "tool", content = content, toolCallId = toolCallId, isError = isError)
    }
}

/**
 * Defines a function the model can call.
 */
@Serializable
data class ChatTool(
    val name: String = "",
    val description: String = "",
    val parameters: JsonElement? = null,
)

/**
 * A content block within a chat message or response.
 */
@Serializable
data class ContentBlock(
    @SerialName("type") val blockType: String = "",
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: JsonObject? = null,
    @SerialName("thought_signature") val thoughtSignature: String? = null,
)

/**
 * Token and cost usage for a chat response.
 */
@Serializable
data class ChatUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Response from the chat completion endpoint.
 */
@Serializable
data class ChatResponse(
    val id: String = "",
    val model: String = "",
    val content: List<ContentBlock> = emptyList(),
    val usage: ChatUsage? = null,
    @SerialName("stop_reason") val stopReason: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
) {
    /** Extract concatenated text content, ignoring thinking and tool_use blocks. */
    fun text(): String =
        content.filter { it.blockType == "text" }.mapNotNull { it.text }.joinToString("")

    /** Extract concatenated thinking content. */
    fun thinking(): String =
        content.filter { it.blockType == "thinking" }.mapNotNull { it.text }.joinToString("")

    /** Extract all tool_use content blocks. */
    fun toolCalls(): List<ContentBlock> =
        content.filter { it.blockType == "tool_use" }
}

// ── Streaming ────────────────────────────────────────────────────────

/**
 * A text delta within a stream event.
 */
@Serializable
data class StreamDelta(
    val text: String? = null,
)

/**
 * A tool use event within a stream.
 */
@Serializable
data class StreamToolUse(
    val id: String = "",
    val name: String = "",
    val input: JsonObject = JsonObject(emptyMap()),
)

/**
 * A parsed SSE event from a streaming chat response.
 */
data class StreamEvent(
    @SerialName("type") val eventType: String = "",
    val delta: StreamDelta? = null,
    @SerialName("tool_use") val toolUse: StreamToolUse? = null,
    val usage: ChatUsage? = null,
    val error: String? = null,
    val done: Boolean = false,
)

// ── Response Metadata ────────────────────────────────────────────────

/**
 * Common metadata parsed from API response headers.
 */
data class ResponseMeta(
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
    val model: String = "",
)

// ── Cross-SDK parity ────────────────────────────────────────────────

@Serializable
data class Citation(
    val url: String = "",
    val title: String = "",
    val text: String = "",
    val index: Int = 0,
)

@Serializable
data class ChatUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
    @SerialName("cost_ticks") val costTicks: Long = 0,
)
