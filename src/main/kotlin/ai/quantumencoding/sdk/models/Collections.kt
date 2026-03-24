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
 * A document within a collection.
 */
@Serializable
data class CollectionDocument(
    @SerialName("file_id") val fileId: String = "",
    val name: String = "",
    @SerialName("size_bytes") val sizeBytes: Long? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("processing_status") val processingStatus: String? = null,
    @SerialName("document_status") val documentStatus: String? = null,
    val indexed: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * A search result from collection search.
 */
@Serializable
data class CollectionSearchResult(
    val content: String = "",
    val score: Double? = null,
    @SerialName("file_id") val fileId: String? = null,
    @SerialName("collection_id") val collectionId: String? = null,
    val metadata: kotlinx.serialization.json.JsonElement? = null,
)

/**
 * Request body for collection search.
 */
@Serializable
data class CollectionSearchRequest(
    val query: String = "",
    @SerialName("collection_ids") val collectionIds: List<String> = emptyList(),
    val mode: String? = null,
    @SerialName("max_results") val maxResults: Int? = null,
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

@Serializable
internal data class CollectionsListResponse(
    val collections: List<Collection> = emptyList(),
)

@Serializable
internal data class CollectionDocumentsResponse(
    val documents: List<CollectionDocument> = emptyList(),
)

@Serializable
internal data class CollectionSearchResponse(
    val results: List<CollectionSearchResult> = emptyList(),
)

@Serializable
internal data class DeleteCollectionResponse(
    val message: String = "",
)
