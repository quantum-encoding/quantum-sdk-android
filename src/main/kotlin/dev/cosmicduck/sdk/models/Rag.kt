package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Vertex AI RAG ────────────────────────────────────────────────────

/**
 * Request body for RAG search.
 *
 * @property query Search query.
 * @property corpus Corpus name or ID (optional).
 * @property topK Maximum number of results.
 */
@Serializable
data class RAGSearchRequest(
    val query: String,
    val corpus: String? = null,
    @SerialName("top_k") val topK: Int? = null,
)

/**
 * A single RAG search result.
 */
@Serializable
data class RAGResult(
    val text: String = "",
    val score: Double = 0.0,
    val source: String? = null,
)

/**
 * Response from RAG search.
 */
@Serializable
data class RAGSearchResponse(
    val results: List<RAGResult> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * A Vertex AI RAG corpus.
 */
@Serializable
data class RAGCorpus(
    val name: String = "",
    val displayName: String = "",
    val description: String = "",
    val state: String = "",
)

/**
 * Response from listing RAG corpora.
 */
@Serializable
internal data class RAGCorporaResponse(
    val corpora: List<RAGCorpus> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
)

// ── SurrealDB RAG ───────────────────────────────────────────────────

/**
 * Request body for SurrealDB RAG search.
 */
@Serializable
data class SurrealRAGSearchRequest(
    val query: String,
    val provider: String? = null,
    val limit: Int? = null,
)

/**
 * A single SurrealDB RAG search result.
 */
@Serializable
data class SurrealRAGResult(
    val id: String = "",
    val text: String = "",
    val score: Double = 0.0,
    val provider: String = "",
    val source: String? = null,
)

/**
 * Response from SurrealDB RAG search.
 */
@Serializable
data class SurrealRAGSearchResponse(
    val results: List<SurrealRAGResult> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Information about a SurrealDB RAG provider.
 */
@Serializable
data class SurrealRAGProviderInfo(
    val provider: String = "",
    @SerialName("chunk_count") val chunkCount: Int = 0,
)

/**
 * Response from listing SurrealDB RAG providers.
 */
@Serializable
data class SurrealRAGProvidersResponse(
    val providers: List<SurrealRAGProviderInfo> = emptyList(),
)
