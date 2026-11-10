package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── xAI Collection Proxy Types ──────────────────────────────────────

/**
 * A user-scoped xAI collection (proxied through quantum-ai).
 */
@Serializable
data class Collection(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    @SerialName("document_count") val documentCount: Long? = null,
    val owner: String? = null,
    val provider: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * One matched chunk from `POST /qai/v1/rag/collections/search`.
 */
@Serializable
data class CollectionSearchResult(
    /** The matched chunk text. */
    val content: String = "",
    /** Relevance score; results arrive highest first. */
    val score: Double = 0.0,
    /** Name of the collection the chunk came from. */
    val collection: String = "",
    /** ID of the collection the chunk came from. */
    @SerialName("collection_id") val collectionId: String = "",
    /** Provider-side ID of the document holding the chunk, when known. */
    @SerialName("document_id") val documentId: String? = null,
    /** Filename of the document holding the chunk, when known. */
    val filename: String? = null,
    /** True when the chunk came from a shared collection rather than one the caller owns. */
    @SerialName("is_shared") val isShared: Boolean = false,
)

/**
 * Request body for `POST /qai/v1/rag/collections/search`.
 */
@Serializable
data class CollectionSearchRequest(
    /** The search query. Required; the gateway answers 400 without it. */
    val query: String,
    /**
     * Collections to search. Empty searches every collection the caller can
     * read, their own and shared. IDs the caller cannot read are skipped.
     */
    @SerialName("collection_ids") val collectionIds: List<String> = emptyList(),
    /** Maximum chunks to return across all collections. The gateway defaults to 10. */
    @SerialName("max_chunks") val maxChunks: Int? = null,
)

/**
 * Upload result for a document added to a collection.
 */
@Serializable
data class CollectionUploadResult(
    @SerialName("file_id") val fileId: String = "",
    val filename: String = "",
    val bytes: Long? = null,
)

// ── Internal wrapper types for API responses ────────────────────────
// The gateway encodes an empty list as JSON null, so the lists are nullable.

@Serializable
internal data class CollectionsListResponse(
    val collections: List<Collection>? = null,
)

@Serializable
internal data class CollectionSearchResponse(
    val results: List<CollectionSearchResult>? = null,
)

@Serializable
internal data class DeleteCollectionResponse(
    val message: String = "",
)
