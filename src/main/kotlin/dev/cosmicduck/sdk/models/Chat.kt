package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ── Chat Request / Response ──────────────────────────────────────────

/**
 * Request body for the chat completion endpoint.
 *
 * @property model Model ID that determines provider routing (e.g. "claude-sonnet-4-6").
 * @property messages Conversation history.
 * @property tools Functions the model can call.
 * @property stream Enable server-sent event streaming. Use [QuantumClient.chatStream] instead.
 * @property temperature Controls randomness (0.0-2.0).
 * @property maxTokens Limits the response length.
 * @property providerOptions Provider-specific settings (e.g. Anthropic thinking, xAI search).
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
 *
 * Use the companion factory methods for convenience:
 * ```kotlin
 * ChatMessage.system("You are helpful")
 * ChatMessage.user("Hello!")
 * ChatMessage.assistant("Hi there!")
 * ```
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
        /** Create a system message. */
        fun system(content: String) = ChatMessage(role = "system", content = content)

        /** Create a user message. */
        fun user(content: String) = ChatMessage(role = "user", content = content)

        /** Create an assistant message. */
        fun assistant(content: String) = ChatMessage(role = "assistant", content = content)

        /** Create a tool result message. */
        fun toolResult(toolCallId: String, content: String, isError: Boolean = false) =
            ChatMessage(role = "tool", content = content, toolCallId = toolCallId, isError = isError)
    }
}

/**
 * A tool (function) the model can call during chat.
 */
@Serializable
data class ChatTool(
    val type: String = "function",
    val function: ChatFunction,
)

/**
 * Function definition within a [ChatTool].
 */
@Serializable
data class ChatFunction(
    val name: String,
    val description: String? = null,
    val parameters: JsonObject? = null,
)

/**
 * A content block within a chat message or response.
 * Can be text, thinking, tool_use, etc.
 */
@Serializable
data class ContentBlock(
    val type: String,
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: JsonObject? = null,
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
    val usage: ChatUsage = ChatUsage(),
    @SerialName("stop_reason") val stopReason: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
) {
    /** Extract concatenated text content, ignoring thinking and tool_use blocks. */
    fun text(): String =
        content.filter { it.type == "text" }.mapNotNull { it.text }.joinToString("")

    /** Extract concatenated thinking content. */
    fun thinking(): String =
        content.filter { it.type == "thinking" }.mapNotNull { it.text }.joinToString("")

    /** Extract all tool_use content blocks. */
    fun toolCalls(): List<ContentBlock> =
        content.filter { it.type == "tool_use" }
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
    val type: String,
    val delta: StreamDelta? = null,
    val toolUse: StreamToolUse? = null,
    val usage: ChatUsage? = null,
    val error: String? = null,
    val done: Boolean = false,
)

// ── Response Metadata ────────────────────────────────────────────────

/**
 * Common metadata parsed from API response headers.
 */
data class ResponseMeta(
    val costTicks: Long = 0,
    val requestId: String = "",
    val model: String = "",
)
