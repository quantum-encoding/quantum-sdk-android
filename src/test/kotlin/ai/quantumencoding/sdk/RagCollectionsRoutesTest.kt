package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.CollectionSearchRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Mock-gateway tests for the user-scoped RAG collection routes.
 *
 * The wire shapes mirror the gateway handlers in
 * `internal/server/routes_rag_collections.go`: the search request decodes
 * `query`, `collection_ids` and `max_chunks`; the search response carries
 * `results`, `query`, `collections_searched` and `request_id`. A Go nil slice
 * encodes as JSON `null`, so both list endpoints can answer `null` when empty.
 */
class RagCollectionsRoutesTest {

    private lateinit var server: MockWebServer
    private lateinit var client: QuantumClient
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = QuantumClient(
            apiKey = "qai_test_key",
            baseUrl = server.url("/").toString().trimEnd('/'),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueueJson(body: String) {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private fun RecordedRequest.bodyJson() = json.parseToJsonElement(body.readUtf8()).jsonObject

    // ── POST /qai/v1/rag/collections/search ──────────────────────────

    @Test
    fun collectionsSearch_postsHandlerFieldsAndDecodesResults() = runTest {
        enqueueJson(
            """
            {
              "results": [
                {"content": "chunk one", "score": 0.91, "collection": "Docs",
                 "collection_id": "col_1", "document_id": "doc_9",
                 "filename": "guide.pdf", "is_shared": false},
                {"content": "chunk two", "score": 0.42, "collection": "Shared KB",
                 "collection_id": "col_2", "is_shared": true}
              ],
              "query": "how do refunds work",
              "collections_searched": 2,
              "request_id": "req_abc"
            }
            """.trimIndent()
        )

        val results = client.collectionsSearch(
            CollectionSearchRequest(
                query = "how do refunds work",
                collectionIds = listOf("col_1", "col_2"),
                maxChunks = 5,
            )
        )

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/rag/collections/search", req.path)
        assertEquals("Bearer qai_test_key", req.getHeader("Authorization"))

        val body = req.bodyJson()
        assertEquals(setOf("query", "collection_ids", "max_chunks"), body.keys)
        assertEquals("how do refunds work", body["query"]!!.jsonPrimitive.content)
        assertEquals(
            listOf("col_1", "col_2"),
            body["collection_ids"]!!.jsonArray.map { it.jsonPrimitive.content },
        )
        assertEquals(5, body["max_chunks"]!!.jsonPrimitive.int)

        assertEquals(2, results.size)
        val first = results[0]
        assertEquals("chunk one", first.content)
        assertEquals(0.91, first.score, 1e-9)
        assertEquals("Docs", first.collection)
        assertEquals("col_1", first.collectionId)
        assertEquals("doc_9", first.documentId)
        assertEquals("guide.pdf", first.filename)
        assertFalse(first.isShared)

        val second = results[1]
        assertEquals("Shared KB", second.collection)
        assertNull(second.documentId)
        assertNull(second.filename)
        assertTrue(second.isShared)
    }

    @Test
    fun collectionsSearch_queryOnlySendsJustQuery() = runTest {
        enqueueJson("""{"results": [], "query": "q", "collections_searched": 0, "request_id": "r"}""")

        client.collectionsSearch(CollectionSearchRequest(query = "q"))

        // Empty collection_ids means "every readable collection" to the
        // gateway, so it must be omitted rather than sent as [].
        assertEquals(setOf("query"), server.takeRequest().bodyJson().keys)
    }

    @Test
    fun collectionsSearch_nullResultsDecodeAsEmpty() = runTest {
        enqueueJson("""{"results": null, "query": "q", "collections_searched": 0, "request_id": "r"}""")

        assertEquals(emptyList<Any>(), client.collectionsSearch(CollectionSearchRequest(query = "q")))
    }

    // ── GET /qai/v1/rag/collections ──────────────────────────────────

    @Test
    fun collectionsList_nullCollectionsDecodeAsEmpty() = runTest {
        enqueueJson("""{"collections": null, "request_id": "r"}""")

        assertEquals(emptyList<Any>(), client.collectionsList())

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/qai/v1/rag/collections", req.path)
    }
}
