package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Web Search (Brave) ──────────────────────────────────────────────

/**
 * Request body for web search.
 *
 * @property query Search query string (required).
 * @property count Number of results to return.
 * @property offset Pagination offset.
 * @property country Country code for localized results (e.g. "US", "GB").
 * @property language UI language code (e.g. "en", "fr").
 * @property freshness Time filter: "pd" (day), "pw" (week), "pm" (month).
 * @property safesearch Safe search level.
 */
@Serializable
data class WebSearchRequest(
    val query: String,
    val count: Int? = null,
    val offset: Int? = null,
    val country: String? = null,
    val language: String? = null,
    val freshness: String? = null,
    val safesearch: String? = null,
)

/**
 * Full web search response containing multiple result types.
 */
@Serializable
data class WebSearchResponse(
    val query: SearchQueryInfo? = null,
    val web: WebResults? = null,
    val news: NewsResults? = null,
    val videos: SearchVideoResults? = null,
    val infobox: SearchInfobox? = null,
    val discussions: DiscussionResults? = null,
)

/**
 * Query metadata from the search engine.
 */
@Serializable
data class SearchQueryInfo(
    val original: String = "",
    val altered: String = "",
    val language: String = "",
    @SerialName("spellcheck_off") val spellcheckOff: Boolean = false,
)

/**
 * Container for web search results.
 */
@Serializable
data class WebResults(
    val results: List<WebResult> = emptyList(),
    @SerialName("family_friendly") val familyFriendly: Boolean = false,
)

/**
 * A single web search result.
 */
@Serializable
data class WebResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    @SerialName("extra_snippets") val extraSnippets: List<String> = emptyList(),
    val age: String = "",
    val language: String = "",
    @SerialName("family_friendly") val familyFriendly: Boolean = false,
    @SerialName("meta_url") val metaUrl: SearchMetaUrl? = null,
    val thumbnail: SearchThumbnail? = null,
)

/**
 * URL metadata for a search result.
 */
@Serializable
data class SearchMetaUrl(
    val scheme: String = "",
    val netloc: String = "",
    val hostname: String = "",
    val favicon: String = "",
    val path: String = "",
)

/**
 * Thumbnail image for a search result.
 */
@Serializable
data class SearchThumbnail(
    val src: String = "",
    val height: Int = 0,
    val width: Int = 0,
)

/**
 * Container for news results.
 */
@Serializable
data class NewsResults(
    val results: List<NewsResult> = emptyList(),
)

/**
 * A single news result.
 */
@Serializable
data class NewsResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val age: String = "",
    val source: String = "",
    val thumbnail: SearchThumbnail? = null,
)

/**
 * Container for video results.
 */
@Serializable
data class SearchVideoResults(
    val results: List<SearchVideoResult> = emptyList(),
)

/**
 * A single video search result.
 */
@Serializable
data class SearchVideoResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val age: String = "",
    val thumbnail: SearchThumbnail? = null,
)

/**
 * Knowledge panel / infobox result.
 */
@Serializable
data class SearchInfobox(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    @SerialName("long_desc") val longDesc: String = "",
    val type: String = "",
    val images: List<SearchThumbnail> = emptyList(),
)

/**
 * Container for discussion/forum results.
 */
@Serializable
data class DiscussionResults(
    val results: List<DiscussionResult> = emptyList(),
)

/**
 * A single discussion/forum result.
 */
@Serializable
data class DiscussionResult(
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val age: String = "",
)

// ── Search Context (LLM grounding) ─────────────────────────────────

/**
 * Request body for LLM context search.
 *
 * @property query Search query string (required).
 * @property count Number of content chunks to return.
 * @property country Country code for localized results.
 * @property language UI language code.
 * @property freshness Time filter: "pd" (day), "pw" (week), "pm" (month).
 */
@Serializable
data class SearchContextRequest(
    val query: String,
    val count: Int? = null,
    val country: String? = null,
    val language: String? = null,
    val freshness: String? = null,
)

/**
 * Response containing extracted content chunks optimized for LLM context.
 */
@Serializable
data class SearchContextResponse(
    val chunks: List<ContentChunk> = emptyList(),
    val sources: List<ContextSource> = emptyList(),
    val query: String = "",
)

/**
 * A single content chunk extracted from a web page.
 */
@Serializable
data class ContentChunk(
    val content: String = "",
    val url: String = "",
    val title: String = "",
    val score: Double = 0.0,
    @SerialName("content_type") val contentType: String = "",
    val index: Int = 0,
)

/**
 * A source referenced in the context response.
 */
@Serializable
data class ContextSource(
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val snippet: String = "",
)

// ── Search Answer (grounded AI answer) ──────────────────────────────

/**
 * Request body for grounded AI answer.
 *
 * @property messages Conversation messages (required).
 * @property model Model to use for answer generation.
 */
@Serializable
data class SearchAnswerRequest(
    val messages: List<SearchChatMessage>,
    val model: String? = null,
)

/**
 * A message in the search answer conversation.
 */
@Serializable
data class SearchChatMessage(
    val role: String,
    val content: String,
)

/**
 * Response containing a grounded AI answer with citations.
 */
@Serializable
data class SearchAnswerResponse(
    val choices: List<AnswerChoice> = emptyList(),
    val model: String = "",
    val id: String = "",
    val citations: List<SearchCitation> = emptyList(),
)

/**
 * A single answer choice.
 */
@Serializable
data class AnswerChoice(
    val index: Int = 0,
    val message: SearchChatMessage? = null,
    @SerialName("finish_reason") val finishReason: String = "",
)

/**
 * A citation referenced in the answer.
 */
@Serializable
data class SearchCitation(
    val url: String = "",
    val title: String = "",
    val snippet: String = "",
)
