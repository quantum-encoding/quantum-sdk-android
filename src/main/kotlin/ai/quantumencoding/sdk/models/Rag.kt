package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Vertex AI RAG ────────────────────────────────────────────────────

/**
 * Request body for Vertex AI RAG search.
 */
@Serializable
data class RagSearchRequest(
    val query: String = "",
    val corpus: String? = null,
    @SerialName("top_k") val topK: Int? = null,
)

/**
 * A single result from RAG search.
 */
@Serializable
data class RagResult(
    @SerialName("source_uri") val sourceUri: String = "",
    @SerialName("source_name") val sourceName: String = "",
    val text: String = "",
    val score: Double = 0.0,
    val distance: Double = 0.0,
)

/**
 * Response from RAG search.
 */
@Serializable
data class RagSearchResponse(
    val results: List<RagResult> = emptyList(),
    val query: String = "",
    val corpora: List<String>? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Describes an available RAG corpus.
 */
@Serializable
data class RagCorpus(
    val name: String = "",
    @SerialName("displayName") val displayName: String = "",
    val description: String = "",
    val state: String = "",
)

// ── SurrealDB RAG ───────────────────────────────────────────────────

/**
 * Request body for SurrealDB-backed RAG search.
 */
@Serializable
data class SurrealRagSearchRequest(
    val query: String = "",
    val provider: String? = null,
    val limit: Int? = null,
)

/**
 * A single result from SurrealDB RAG search.
 */
@Serializable
data class SurrealRagResult(
    val provider: String = "",
    val title: String = "",
    val heading: String = "",
    @SerialName("source_file") val sourceFile: String = "",
    val content: String = "",
    val score: Double = 0.0,
)

/**
 * Response from SurrealDB RAG search.
 */
@Serializable
data class SurrealRagSearchResponse(
    val results: List<SurrealRagResult> = emptyList(),
    val query: String = "",
    val provider: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * A SurrealDB RAG provider.
 */
@Serializable
data class SurrealRagProvider(
    val provider: String = "",
    @SerialName("chunk_count") val chunkCount: Long? = null,
)

/**
 * Response from listing SurrealDB RAG providers.
 */
@Serializable
data class SurrealRagProvidersResponse(
    val providers: List<SurrealRagProvider> = emptyList(),
)
