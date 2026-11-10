package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.DigitalTwinCreateRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Mock-gateway tests for `POST /qai/v1/video/digital-twin`.
 *
 * The wire shapes mirror `handleDigitalTwinCreate` in
 * `internal/server/routes_heygen_twin.go`: a JSON body is decoded as
 * `{name, video_url, avatar_group_id?}` (any non-multipart Content-Type takes
 * the JSON branch), and the synchronous response always carries `name`,
 * `consent_url`, `model` and `request_id`, plus `group_id`, `status`,
 * `consent_status` when HeyGen returned a group and `avatar_id` when it
 * returned a look.
 */
class DigitalTwinRoutesTest {

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

    private fun enqueueJson(body: String, headers: Map<String, String> = emptyMap()) {
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
        headers.forEach { (k, v) -> response.setHeader(k, v) }
        server.enqueue(response)
    }

    private fun RecordedRequest.bodyJson() = json.parseToJsonElement(body.readUtf8()).jsonObject

    @Test
    fun videoDigitalTwin_postsCreateBodyAndDecodesTwin() = runTest {
        enqueueJson(
            """
            {
              "name": "Ada",
              "consent_url": "https://app.heygen.com/consent/abc",
              "model": "heygen-digital-twin",
              "request_id": "req_twin_1",
              "group_id": "grp_42",
              "status": "pending_consent",
              "consent_status": "pending",
              "avatar_id": "look_7"
            }
            """.trimIndent()
        )

        val twin = client.videoDigitalTwin(
            DigitalTwinCreateRequest(
                name = "Ada",
                videoUrl = "https://cdn.example.com/footage.mp4",
                avatarGroupId = "grp_42",
            )
        )

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/video/digital-twin", req.path)
        assertEquals("Bearer qai_test_key", req.getHeader("Authorization"))
        assertTrue(req.getHeader("Content-Type")!!.startsWith("application/json"))

        val body = req.bodyJson()
        assertEquals(setOf("name", "video_url", "avatar_group_id"), body.keys)
        assertEquals("Ada", body["name"]!!.jsonPrimitive.content)
        assertEquals("https://cdn.example.com/footage.mp4", body["video_url"]!!.jsonPrimitive.content)
        assertEquals("grp_42", body["avatar_group_id"]!!.jsonPrimitive.content)

        assertEquals("Ada", twin.name)
        assertEquals("https://app.heygen.com/consent/abc", twin.consentUrl)
        assertEquals("heygen-digital-twin", twin.model)
        assertEquals("req_twin_1", twin.requestId)
        assertEquals("grp_42", twin.groupId)
        assertEquals("pending_consent", twin.status)
        assertEquals("pending", twin.consentStatus)
        assertEquals("look_7", twin.avatarId)
    }

    @Test
    fun videoDigitalTwin_newGroupOmitsGroupIdAndToleratesMissingGroup() = runTest {
        enqueueJson(
            """{"name": "Ada", "consent_url": "", "model": "heygen-digital-twin"}""",
            headers = mapOf("X-QAI-Request-Id" to "req_from_header"),
        )

        val twin = client.videoDigitalTwin(
            DigitalTwinCreateRequest(name = "Ada", videoUrl = "https://cdn.example.com/footage.mp4")
        )

        assertEquals(setOf("name", "video_url"), server.takeRequest().bodyJson().keys)

        assertEquals("", twin.consentUrl)
        assertNull(twin.groupId)
        assertNull(twin.status)
        assertNull(twin.consentStatus)
        assertNull(twin.avatarId)
        assertEquals("req_from_header", twin.requestId)
    }
}
