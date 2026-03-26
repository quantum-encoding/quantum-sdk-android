package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Web Search (Brave) ──────────────────────────────────────────────

/**
 * Request body for web search.
 */
@Serializable
data class WebSearchRequest(
    val query: String = "",
    val count: Int? = null,
    val offset: Int? = null,
    val country: String? = null,
    val language: String? = null,
    val freshness: String? = null,
    val safesearch: String? = null,
)

/**
 * A single web search result.
 */
@Serializable
data class WebResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val age: String? = null,
    val favicon: String? = null,
)

/**
 * A news search result.
 */
@Serializable
data class NewsResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val age: String? = null,
    val source: String? = null,
)

/**
 * A video search result.
 */
@Serializable
data class VideoResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val thumbnail: String? = null,
    val age: String? = null,
)

/**
 * An infobox (knowledge panel) result.
 */
@Serializable
data class InfoboxResult(
    val title: String = "",
    val description: String = "",
    val url: String? = null,
)

/**
 * A discussion / forum result.
 */
@Serializable
data class DiscussionResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val age: String? = null,
    val forum: String? = null,
)

/**
 * Response from the web search endpoint.
 */
@Serializable
data class WebSearchResponse(
    val query: String = "",
    val web: List<WebResult> = emptyList(),
    val news: List<NewsResult> = emptyList(),
    val videos: List<VideoResult> = emptyList(),
    val infobox: List<InfoboxResult> = emptyList(),
    val discussions: List<DiscussionResult> = emptyList(),
)

// ── Search Context (LLM grounding) ─────────────────────────────────

/**
 * Request body for search context.
 */
@Serializable
data class SearchContextRequest(
    val query: String = "",
    val count: Int? = null,
    val country: String? = null,
    val language: String? = null,
    val freshness: String? = null,
)

/**
 * A content chunk from search context.
 */
@Serializable
data class SearchContextChunk(
    val content: String = "",
    val url: String = "",
    val title: String = "",
    val score: Double = 0.0,
    @SerialName("content_type") val contentType: String? = null,
)

/**
 * A source reference from search context.
 */
@Serializable
data class SearchContextSource(
    val url: String = "",
    val title: String = "",
)

/**
 * Response from the search context endpoint.
 */
@Serializable
data class SearchContextResponse(
    val chunks: List<SearchContextChunk> = emptyList(),
    val sources: List<SearchContextSource> = emptyList(),
    val query: String = "",
)

// ── Search Answer (grounded AI answer) ──────────────────────────────

/**
 * A chat message for the search answer endpoint.
 */
@Serializable
data class SearchAnswerMessage(
    val role: String = "",
    val content: String = "",
)

/**
 * Request body for search answer.
 */
@Serializable
data class SearchAnswerRequest(
    val messages: List<SearchAnswerMessage> = emptyList(),
    val model: String? = null,
)

/**
 * A citation reference in a search answer.
 */
@Serializable
data class SearchAnswerCitation(
    val url: String = "",
    val title: String = "",
    val snippet: String? = null,
)

/**
 * A choice in the search answer response.
 */
@Serializable
data class SearchAnswerChoice(
    val index: Int = 0,
    val message: SearchAnswerMessage = SearchAnswerMessage(),
    @SerialName("finish_reason") val finishReason: String? = null,
)

/**
 * Response from the search answer endpoint.
 */
@Serializable
data class SearchAnswerResponse(
    val choices: List<SearchAnswerChoice> = emptyList(),
    val model: String = "",
    val id: String = "",
    val citations: List<SearchAnswerCitation> = emptyList(),
)

typealias Infobox = InfoboxResult
typealias Discussion = DiscussionResult

// ── Search Options ──────────────────────────────────────────────────

/**
 * Options for configuring web search requests.
 */
@Serializable
data class SearchOptions(
    val count: Int? = null,
    val offset: Int? = null,
    val country: String? = null,
    val language: String? = null,
    val freshness: String? = null,
    @SerialName("safe_search") val safeSearch: String? = null,
)

/**
 * Options for configuring LLM context search requests.
 */
@Serializable
data class ContextOptions(
    val count: Int? = null,
    val country: String? = null,
    val language: String? = null,
    val freshness: String? = null,
)

/**
 * A message in a search-answer conversation.
 */
@Serializable
data class SearchMessage(
    val role: String = "",
    val content: String = "",
)

// ── LLM Context ─────────────────────────────────────────────────────

/**
 * A single chunk of context from a web page (simple variant).
 */
@Serializable
data class ContextChunk(
    val content: String = "",
    val url: String = "",
    val title: String = "",
    val score: Double = 0.0,
    @SerialName("content_type") val contentType: String? = null,
)

/**
 * LLM-optimised context response from web search.
 */
@Serializable
data class LLMContextResponse(
    val query: String = "",
    val chunks: List<ContextChunk> = emptyList(),
    val sources: List<String> = emptyList(),
)

/**
 * Context metadata returned in session responses.
 */
@Serializable
data class ContextMetadata(
    @SerialName("turn_count") val turnCount: Long = 0,
    @SerialName("estimated_tokens") val estimatedTokens: Long = 0,
    val compacted: Boolean = false,
    @SerialName("compaction_note") val compactionNote: String? = null,
    @SerialName("tools_cleared") val toolsCleared: Int? = null,
)
