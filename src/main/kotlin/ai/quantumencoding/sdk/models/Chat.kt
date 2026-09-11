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
    /** "auto" (default), "any" (force a tool), "none", or a specific tool name. */
    @SerialName("tool_choice") val toolChoice: String? = null,
    /** JSON Schema for structured output — the model is forced to return matching JSON. */
    @SerialName("output_schema") val outputSchema: JsonObject? = null,
    /**
     * Reasoning budget: "none"/"low"/"medium"/"high"/"xhigh"/"max". Null =
     * provider default. Each adapter folds a tier its model lacks onto the
     * nearest one.
     */
    @SerialName("reasoning_effort") val reasoningEffort: String? = null,
    /**
     * Pins every turn of one conversation to the same provider prompt-cache
     * shard. Any stable string kept per conversation: the gateway hashes it
     * with the caller's identity before forwarding it as OpenAI/xAI
     * `prompt_cache_key` (or `x-grok-conv-id` on the xAI chat-completions
     * lane). Null = derived from the caller's identity alone, which puts all
     * of one user's conversations on one shard. Generate one per conversation
     * object and reuse it on every turn.
     *
     * Honored by POST /qai/v1/chat only: the session endpoint derives its key
     * from the session ID and ignores a client-supplied one.
     */
    @SerialName("prompt_cache_key") val promptCacheKey: String? = null,
    /** Vertex context-cache resource name (e.g. "cachedContents/abc123"); Gemini only. */
    @SerialName("cached_content") val cachedContent: String? = null,
    /**
     * Provider-specific settings, keyed by provider. An open [JsonObject], so
     * a key the gateway documents but this SDK version does not name still
     * rides through, as does the flat `region` entry.
     *
     *   provider_options.openai.reasoning_summary  auto|concise|detailed|none
     *   provider_options.openai.reasoning_mode     standard|pro
     *   provider_options.openai.verbosity          low|medium|high
     *   provider_options.openai.text_format        text|json_object
     *   provider_options.xai.native_files          boolean
     *   provider_options.region                    americas|europe|asia
     */
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
    /**
     * Gemini thought signature (base64). Rides "tool_use" blocks and, on
     * Gemini 3, the "text" block of a turn that ended in text. Echo it back on
     * the corresponding block of the next turn's assistant message. A
     * streaming turn that ends in text carries it on the "thought_signature"
     * event instead — see [StreamEvent.thoughtSignature].
     */
    @SerialName("thought_signature") val thoughtSignature: String? = null,
    /**
     * The provider's own reasoning item, verbatim, on a block whose
     * [blockType] is "reasoning". Opaque — never inspect or rebuild it. Pass
     * the whole block back untouched, IN THE POSITION IT ARRIVED IN, on the
     * next turn's assistant message: its place among the "tool_use" blocks is
     * how the provider learns where the reasoning sat, and replaying it behind
     * the call it reasoned about is a different conversation the provider
     * rejects. Dropping it re-bills the reasoning tokens on every round of a
     * tool loop.
     *
     * Distinct from a "thinking" block, which is the human-readable summary of
     * the same turn: one is for the reader, one is for the wire.
     */
    val reasoning: JsonElement? = null,
    /**
     * Model that produced a "reasoning" block. Reasoning state is bound to its
     * model, so a block is never replayed to a different one.
     */
    @SerialName("minted_by") val mintedBy: String? = null,
    /** Base64-encoded payload, for blockType "image" and "file". */
    val data: String? = null,
    /** MIME type, e.g. "image/png", "application/pdf", "video/mp4". */
    @SerialName("mime_type") val mimeType: String? = null,
    /** File name, for blockType "file". */
    @SerialName("file_name") val fileName: String? = null,
    /** Remote resource URL (YouTube, gs://, etc.), for blockType "file_uri". */
    @SerialName("file_uri") val fileUri: String? = null,
)

/**
 * Token and cost usage for a chat response. Mirrors the backend
 * `ChatUsage` contract (`internal/server/convert.go`): non-cached input
 * tokens, cached input tokens (billed at the lower cached rate), total
 * billable output tokens, reasoning-token sub-component, and cost in ticks.
 */
@Serializable
data class ChatUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("cached_tokens") val cachedTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
    @SerialName("reasoning_tokens") val reasoningTokens: Int = 0,
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
    val type: String = "",
    val delta: StreamDelta? = null,
    @SerialName("tool_use") val toolUse: StreamToolUse? = null,
    val usage: ChatUsage? = null,
    /**
     * Gemini 3's signature (base64) for a stream that ended in text, on the
     * "thought_signature" event the gateway sends just before "done". It also
     * rides the atomic "tool_use" event. Store it on the assistant block
     * echoed back next turn — the same value [ContentBlock.thoughtSignature]
     * carries on a non-streaming response.
     */
    @SerialName("thought_signature") val thoughtSignature: String? = null,
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
    @SerialName("balance_after") val balanceAfter: Long = 0,
)

// ── Cross-SDK parity ────────────────────────────────────────────────

@Serializable
data class Citation(
    val url: String = "",
    val title: String = "",
    val text: String = "",
    val index: Int = 0,
)
