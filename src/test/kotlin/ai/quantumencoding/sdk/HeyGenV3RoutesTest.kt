package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.AudioSoundsQuery
import ai.quantumencoding.sdk.models.AvatarAudioInput
import ai.quantumencoding.sdk.models.AvatarRealtimeRequest
import ai.quantumencoding.sdk.models.AvatarRealtimeTextRequest
import ai.quantumencoding.sdk.models.VideoBatchStatusQuery
import ai.quantumencoding.sdk.models.VideoBatchSubmitRequest
import ai.quantumencoding.sdk.models.VideoTemplateDimension
import ai.quantumencoding.sdk.models.VideoTemplateGenerateRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Mock-gateway tests for the 9 HeyGen v3 routes: avatar realtime
 * (create/get/text/cancel), audio sounds search, video template
 * (detail/generate), and video batch (submit/status).
 *
 * Each test asserts path, method, auth header, request wire format, and
 * response decoding against the gateway wire contract. No production calls.
 */
class HeyGenV3RoutesTest {

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

    private fun enqueueJson(code: Int, body: String, headers: Map<String, String> = emptyMap()) {
        val response = MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
        headers.forEach { (k, v) -> response.setHeader(k, v) }
        server.enqueue(response)
    }

    private fun RecordedRequest.assertAuth() {
        assertEquals("Bearer qai_test_key", getHeader("Authorization"))
    }

    private fun RecordedRequest.bodyJson() = json.parseToJsonElement(body.readUtf8()).jsonObject

    // ── 1. POST /qai/v1/avatar/realtime ──────────────────────────────

    @Test
    fun createAvatarRealtimeSession_textStream() = runTest {
        enqueueJson(
            200,
            """
            {
              "stream_id": "rt_9f2c1a",
              "status": "pending",
              "prepaid_seconds": 300,
              "cost_ticks": 345000000000,
              "request_id": "req_abc123def456"
            }
            """.trimIndent(),
            headers = mapOf(
                "X-QAI-Cost-Ticks" to "345000000000",
                "X-QAI-Balance-After" to "655000000000",
            ),
        )

        val resp = client.createAvatarRealtimeSession(
            AvatarRealtimeRequest(
                sessionType = "text_stream",
                avatarId = "Abigail_expressive_2024112501",
                voiceId = "73c0b6a2e29d4d38aca41454bf58c955",
                text = "Hello! Let me think about that...",
                maxDurationSeconds = 300,
            )
        )

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/avatar/realtime", req.path)
        req.assertAuth()
        val body = req.bodyJson()
        assertEquals("text_stream", body["type"]!!.jsonPrimitive.content)
        assertEquals("Abigail_expressive_2024112501", body["avatar_id"]!!.jsonPrimitive.content)
        assertEquals("73c0b6a2e29d4d38aca41454bf58c955", body["voice_id"]!!.jsonPrimitive.content)
        assertEquals("Hello! Let me think about that...", body["text"]!!.jsonPrimitive.content)
        assertEquals(300, body["max_duration_seconds"]!!.jsonPrimitive.content.toInt())
        assertNull(body["audio"])

        assertEquals("rt_9f2c1a", resp.streamId)
        assertEquals("pending", resp.status)
        assertEquals(300, resp.prepaidSeconds)
        assertEquals(345000000000L, resp.costTicks)
        assertEquals(655000000000L, resp.balanceAfter)
        assertEquals("req_abc123def456", resp.requestId)
    }

    @Test
    fun createAvatarRealtimeSession_audioType_serializesAudioUnion() = runTest {
        enqueueJson(200, """{"stream_id":"rt_1","status":"pending","prepaid_seconds":120,"cost_ticks":1,"request_id":"req_1"}""")

        client.createAvatarRealtimeSession(
            AvatarRealtimeRequest(
                sessionType = "audio",
                avatarId = "av_1",
                audio = AvatarAudioInput(
                    inputType = "base64",
                    mediaType = "audio/mpeg",
                    data = "AQID",
                ),
                maxDurationSeconds = 120,
            )
        )

        val body = server.takeRequest().bodyJson()
        assertEquals("audio", body["type"]!!.jsonPrimitive.content)
        // voice_id and text must be omitted for audio-type sessions.
        assertNull(body["voice_id"])
        assertNull(body["text"])
        val audio = body["audio"]!!.jsonObject
        assertEquals("base64", audio["type"]!!.jsonPrimitive.content)
        assertEquals("audio/mpeg", audio["media_type"]!!.jsonPrimitive.content)
        assertEquals("AQID", audio["data"]!!.jsonPrimitive.content)
        assertNull(audio["url"])
        assertNull(audio["asset_id"])
    }

    @Test
    fun createAvatarRealtimeSession_backfillsFromHeaders() = runTest {
        // Body cost_ticks/request_id absent → backfilled from headers.
        enqueueJson(
            200,
            """{"stream_id":"rt_2","status":"pending","prepaid_seconds":60}""",
            headers = mapOf(
                "X-QAI-Cost-Ticks" to "69000000000",
                "X-QAI-Balance-After" to "31000000000",
                "X-QAI-Request-Id" to "req_hdr",
            ),
        )

        val resp = client.createAvatarRealtimeSession(
            AvatarRealtimeRequest(
                sessionType = "tts",
                avatarId = "av_1",
                voiceId = "v_1",
                text = "Hi",
                maxDurationSeconds = 60,
            )
        )
        assertEquals(69000000000L, resp.costTicks)
        assertEquals(31000000000L, resp.balanceAfter)
        assertEquals("req_hdr", resp.requestId)
    }

    // ── 2. GET /qai/v1/avatar/realtime/{id} ──────────────────────────

    @Test
    fun getAvatarRealtimeSession_streaming() = runTest {
        enqueueJson(
            200,
            """
            {
              "stream_id": "rt_9f2c1a",
              "status": "streaming",
              "hls_url": "https://cdn.heygen.com/realtime/rt_9f2c1a/index.m3u8",
              "request_id": "req_abc123def457"
            }
            """.trimIndent(),
        )

        val resp = client.getAvatarRealtimeSession("rt_9f2c1a")

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/qai/v1/avatar/realtime/rt_9f2c1a", req.path)
        req.assertAuth()

        assertEquals("rt_9f2c1a", resp.streamId)
        assertEquals("streaming", resp.status)
        assertEquals("https://cdn.heygen.com/realtime/rt_9f2c1a/index.m3u8", resp.hlsUrl)
        assertNull(resp.errorMessage)
        assertNull(resp.endReason)
        assertEquals("req_abc123def457", resp.requestId)
    }

    @Test
    fun getAvatarRealtimeSession_completedWithEndReason() = runTest {
        enqueueJson(
            200,
            """{"stream_id":"rt_1","status":"completed","end_reason":"idle_timeout","request_id":"req_2"}""",
        )
        val resp = client.getAvatarRealtimeSession("rt_1")
        assertEquals("completed", resp.status)
        assertEquals("idle_timeout", resp.endReason)
        assertNull(resp.hlsUrl)
    }

    // ── 3. POST /qai/v1/avatar/realtime/{id}/text ────────────────────

    @Test
    fun sendAvatarRealtimeText_delta() = runTest {
        enqueueJson(
            200,
            """{"ok":true,"buffered_bytes":512,"final":false,"request_id":"req_abc123def458"}""",
        )

        val resp = client.sendAvatarRealtimeText(
            "rt_9f2c1a",
            AvatarRealtimeTextRequest.delta(" and here is the rest."),
        )

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/avatar/realtime/rt_9f2c1a/text", req.path)
        req.assertAuth()
        val body = req.bodyJson()
        assertEquals(" and here is the rest.", body["delta"]!!.jsonPrimitive.content)

        assertTrue(resp.ok)
        assertEquals(512L, resp.bufferedBytes)
        assertFalse(resp.isFinal)
        assertEquals("req_abc123def458", resp.requestId)
    }

    @Test
    fun sendAvatarRealtimeText_finalMarkerOmitsDelta() = runTest {
        enqueueJson(200, """{"ok":true,"buffered_bytes":512,"final":true,"request_id":"req_1"}""")

        val resp = client.sendAvatarRealtimeText("rt_1", AvatarRealtimeTextRequest.finalMarker())

        val body = server.takeRequest().bodyJson()
        // Empty delta omitted entirely; final carried as true.
        assertNull(body["delta"])
        assertEquals("true", body["final"]!!.jsonPrimitive.content)
        assertTrue(resp.isFinal)
    }

    // ── 4. POST /qai/v1/avatar/realtime/{id}/cancel ──────────────────

    @Test
    fun cancelAvatarRealtimeSession() = runTest {
        enqueueJson(
            200,
            """{"stream_id":"rt_9f2c1a","cancelled":true,"request_id":"req_abc123def459"}""",
        )

        val resp = client.cancelAvatarRealtimeSession("rt_9f2c1a")

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/avatar/realtime/rt_9f2c1a/cancel", req.path)
        req.assertAuth()
        // No request body fields — an empty JSON object is sent.
        assertEquals("{}", req.body.readUtf8())

        assertEquals("rt_9f2c1a", resp.streamId)
        assertTrue(resp.cancelled)
        assertEquals("req_abc123def459", resp.requestId)
    }

    @Test
    fun cancelAvatarRealtimeSession_alreadyTerminal() = runTest {
        enqueueJson(200, """{"stream_id":"rt_1","cancelled":false,"request_id":"req_1"}""")
        val resp = client.cancelAvatarRealtimeSession("rt_1")
        assertFalse(resp.cancelled)
    }

    // ── 5. GET /qai/v1/audio/sounds ──────────────────────────────────

    @Test
    fun searchAudioSounds() = runTest {
        enqueueJson(
            200,
            """
            {
              "sounds": [
                {
                  "id": "trk_8842aa",
                  "name": "Uplifting Corporate",
                  "description": "Bright, optimistic corporate track",
                  "audio_url": "https://resource.heygen.ai/sounds/trk_8842aa.wav?sig=x",
                  "duration": 94.5,
                  "score": 0.91,
                  "type": "music"
                }
              ],
              "has_more": true,
              "next_token": "eyJvZmZzZXQiOjEwfQ",
              "request_id": "req_abc123def45a"
            }
            """.trimIndent(),
        )

        val resp = client.searchAudioSounds(
            AudioSoundsQuery(
                query = "calm piano",
                soundType = "music",
                limit = 10,
                minScore = 0.7,
            )
        )

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        req.assertAuth()
        val url = req.requestUrl!!
        assertEquals("/qai/v1/audio/sounds", url.encodedPath)
        assertEquals("calm piano", url.queryParameter("query"))
        assertEquals("music", url.queryParameter("type"))
        assertEquals("10", url.queryParameter("limit"))
        assertEquals("0.7", url.queryParameter("min_score"))
        assertNull(url.queryParameter("token"))

        assertEquals(1, resp.sounds.size)
        val track = resp.sounds[0]
        assertEquals("trk_8842aa", track.id)
        assertEquals("Uplifting Corporate", track.name)
        assertEquals("https://resource.heygen.ai/sounds/trk_8842aa.wav?sig=x", track.audioUrl)
        assertEquals(94.5, track.duration, 0.0001)
        assertEquals(0.91, track.score, 0.0001)
        assertEquals("music", track.soundType)
        assertTrue(resp.hasMore)
        assertEquals("eyJvZmZzZXQiOjEwfQ", resp.nextToken)
        assertEquals("req_abc123def45a", resp.requestId)
    }

    @Test
    fun searchAudioSounds_emptyPageAndToken() = runTest {
        enqueueJson(200, """{"sounds":[],"has_more":false,"next_token":"","request_id":"req_1"}""")
        val resp = client.searchAudioSounds(AudioSoundsQuery(query = "x", token = "tok en"))
        val url = server.takeRequest().requestUrl!!
        assertEquals("tok en", url.queryParameter("token"))
        assertTrue(resp.sounds.isEmpty())
        assertFalse(resp.hasMore)
        assertEquals("", resp.nextToken)
    }

    // ── 6. GET /qai/v1/video/template/{id} ───────────────────────────

    @Test
    fun videoTemplateDetail() = runTest {
        enqueueJson(
            200,
            """
            {
              "template": {
                "id": "tmpl_5f0a",
                "name": "Product Launch",
                "aspect_ratio": "16:9",
                "variables": {
                  "headline": { "type": "text", "content": "Default headline" },
                  "presenter": { "type": "character", "character_id": "Abigail", "character_type": "avatar" }
                },
                "scenes": [
                  {
                    "scene_id": "scene_1",
                    "script": "Introducing {{headline}}...",
                    "variables": [ { "name": "headline", "variable_type": "text" } ]
                  }
                ]
              },
              "request_id": "req_abc123def45b"
            }
            """.trimIndent(),
        )

        val resp = client.videoTemplateDetail("tmpl_5f0a")

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/qai/v1/video/template/tmpl_5f0a", req.path)
        req.assertAuth()

        val template = resp.template
        assertEquals("tmpl_5f0a", template.id)
        assertEquals("Product Launch", template.name)
        assertEquals("16:9", template.aspectRatio)
        // Variable unions round-trip as raw JSON, discriminated by "type".
        val headline = template.variables["headline"]!!.jsonObject
        assertEquals("text", headline["type"]!!.jsonPrimitive.content)
        assertEquals("Default headline", headline["content"]!!.jsonPrimitive.content)
        val presenter = template.variables["presenter"]!!.jsonObject
        assertEquals("character", presenter["type"]!!.jsonPrimitive.content)
        assertEquals("Abigail", presenter["character_id"]!!.jsonPrimitive.content)
        assertEquals(1, template.scenes.size)
        assertEquals("scene_1", template.scenes[0].sceneId)
        assertEquals("Introducing {{headline}}...", template.scenes[0].script)
        assertEquals("headline", template.scenes[0].variables[0].name)
        assertEquals("text", template.scenes[0].variables[0].variableType)
        assertEquals("req_abc123def45b", resp.requestId)
    }

    // ── 7. POST /qai/v1/video/template/{id} (async job) ──────────────

    @Test
    fun videoTemplateGenerate() = runTest {
        enqueueJson(
            202,
            """
            {
              "job_id": "qai_job_3def45c00112",
              "status": "pending",
              "type": "video/template-v3",
              "request_id": "req_abc123def45c"
            }
            """.trimIndent(),
        )

        val resp = client.videoTemplateGenerate(
            "tmpl_5f0a",
            VideoTemplateGenerateRequest(
                variables = mapOf(
                    "headline" to buildJsonObject {
                        put("type", "text")
                        put("content", "New headline")
                    },
                ),
                title = "Launch video",
                sceneIds = listOf("scene_1"),
                dimension = VideoTemplateDimension(width = 1280, height = 720),
                fps = 30,
            ),
        )

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/video/template/tmpl_5f0a", req.path)
        req.assertAuth()
        val body = req.bodyJson()
        val headline = body["variables"]!!.jsonObject["headline"]!!.jsonObject
        assertEquals("text", headline["type"]!!.jsonPrimitive.content)
        assertEquals("New headline", headline["content"]!!.jsonPrimitive.content)
        assertEquals("Launch video", body["title"]!!.jsonPrimitive.content)
        val sceneIds = body["scene_ids"] as JsonArray
        assertEquals("scene_1", sceneIds[0].jsonPrimitive.content)
        assertEquals(1280, body["dimension"]!!.jsonObject["width"]!!.jsonPrimitive.content.toInt())
        assertEquals(30, body["fps"]!!.jsonPrimitive.content.toInt())
        // Omitted optionals never hit the wire.
        assertNull(body["subtitles"])
        assertNull(body["callback_url"])

        assertEquals("qai_job_3def45c00112", resp.jobId)
        assertEquals("pending", resp.status)
        assertEquals("video/template-v3", resp.jobType)
        assertEquals("req_abc123def45c", resp.requestId)
    }

    // ── 8. POST /qai/v1/video/batch ──────────────────────────────────

    @Test
    fun videoBatchSubmit() = runTest {
        enqueueJson(
            202,
            """
            {
              "batch_id": "batch_66aa1c",
              "status": "processing",
              "total_items": 2,
              "request_id": "req_abc123def45d"
            }
            """.trimIndent(),
        )

        val resp = client.videoBatchSubmit(
            VideoBatchSubmitRequest(
                title = "Onboarding videos",
                videos = listOf(
                    buildJsonObject {
                        put("type", "avatar")
                        put("avatar_id", "Abigail")
                        put("voice_id", "v_1")
                        put("script", "Welcome to the team!")
                    },
                    buildJsonObject {
                        put("type", "avatar")
                        put("avatar_id", "Abigail")
                        put("voice_id", "v_1")
                        put("script", "Here is how billing works.")
                    },
                ),
            )
        )

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/qai/v1/video/batch", req.path)
        req.assertAuth()
        val body = req.bodyJson()
        assertEquals("Onboarding videos", body["title"]!!.jsonPrimitive.content)
        // videos is an array of raw payloads passed through verbatim.
        val videos = body["videos"] as JsonArray
        assertEquals(2, videos.size)
        val item0 = videos[0].jsonObject
        assertEquals("avatar", item0["type"]!!.jsonPrimitive.content)
        assertEquals("Welcome to the team!", item0["script"]!!.jsonPrimitive.content)

        assertEquals("batch_66aa1c", resp.batchId)
        assertEquals("processing", resp.status)
        assertEquals(2, resp.totalItems)
        assertEquals("req_abc123def45d", resp.requestId)
    }

    // ── 9. GET /qai/v1/video/batch/{id} ──────────────────────────────

    @Test
    fun videoBatchStatus_settled() = runTest {
        enqueueJson(
            200,
            """
            {
              "batch_id": "batch_66aa1c",
              "title": "Onboarding videos",
              "status": "completed",
              "total_items": 3,
              "counts_by_status": { "completed": 2, "failed": 1 },
              "created_at": 1752741600,
              "items": [
                { "item_index": 0, "status": "completed", "video_id": "vid_001", "video_url": "https://resource.heygen.ai/video/vid_001.mp4?sig=x" },
                { "item_index": 1, "status": "completed", "video_id": "vid_002", "video_url": "https://resource.heygen.ai/video/vid_002.mp4?sig=x" },
                { "item_index": 2, "status": "failed", "error": { "code": "avatar_not_found", "message": "avatar id not found" } }
              ],
              "has_more": false,
              "next_token": "",
              "billing_status": "settled",
              "cost_ticks": 46000000000,
              "request_id": "req_abc123def45e"
            }
            """.trimIndent(),
        )

        val resp = client.videoBatchStatus(
            "batch_66aa1c",
            VideoBatchStatusQuery(limit = 50, token = "curs or"),
        )

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        req.assertAuth()
        val url = req.requestUrl!!
        assertEquals("/qai/v1/video/batch/batch_66aa1c", url.encodedPath)
        assertEquals("50", url.queryParameter("limit"))
        assertEquals("curs or", url.queryParameter("token"))

        assertEquals("batch_66aa1c", resp.batchId)
        assertEquals("Onboarding videos", resp.title)
        assertEquals("completed", resp.status)
        assertEquals(3, resp.totalItems)
        assertEquals(2, resp.countsByStatus["completed"])
        assertEquals(1, resp.countsByStatus["failed"])
        assertEquals(1752741600L, resp.createdAt)
        assertEquals(3, resp.items.size)
        assertEquals(0, resp.items[0].itemIndex)
        assertEquals("completed", resp.items[0].status)
        assertEquals("vid_001", resp.items[0].videoId)
        assertEquals("https://resource.heygen.ai/video/vid_001.mp4?sig=x", resp.items[0].videoUrl)
        assertNull(resp.items[0].error)
        assertEquals("failed", resp.items[2].status)
        assertNull(resp.items[2].videoUrl)
        assertEquals("avatar_not_found", resp.items[2].error!!.code)
        assertEquals("avatar id not found", resp.items[2].error!!.message)
        assertFalse(resp.hasMore)
        assertEquals("", resp.nextToken)
        assertEquals("settled", resp.billingStatus)
        assertEquals(46000000000L, resp.costTicks)
        assertEquals("req_abc123def45e", resp.requestId)
    }

    @Test
    fun videoBatchStatus_unsettledWithheldUrls_noQuery() = runTest {
        enqueueJson(
            200,
            """
            {
              "batch_id": "batch_1",
              "status": "completed",
              "total_items": 1,
              "counts_by_status": { "completed": 1 },
              "created_at": 1752741600,
              "items": [ { "item_index": 0, "status": "completed", "video_id": "vid_001" } ],
              "has_more": false,
              "next_token": "",
              "billing_status": "settlement_pending",
              "cost_ticks": 0,
              "request_id": "req_1"
            }
            """.trimIndent(),
        )

        val resp = client.videoBatchStatus("batch_1")
        val req = server.takeRequest()
        assertEquals("/qai/v1/video/batch/batch_1", req.path)

        assertEquals("settlement_pending", resp.billingStatus)
        assertEquals(0L, resp.costTicks)
        // URLs withheld until settled.
        assertNull(resp.items[0].videoUrl)
        assertEquals("vid_001", resp.items[0].videoId)
    }

    // ── Error envelope ───────────────────────────────────────────────

    @Test
    fun insufficientBalance_402() = runTest {
        enqueueJson(
            402,
            """
            {
              "error": {
                "message": "out of credits — top up to continue",
                "type": "insufficient_balance",
                "code": "INSUFFICIENT_BALANCE"
              }
            }
            """.trimIndent(),
            headers = mapOf("X-QAI-Request-Id" to "req_err"),
        )

        try {
            client.createAvatarRealtimeSession(
                AvatarRealtimeRequest(
                    sessionType = "tts",
                    avatarId = "av_1",
                    voiceId = "v_1",
                    text = "Hi",
                    maxDurationSeconds = 300,
                )
            )
            fail("expected QuantumApiException")
        } catch (e: QuantumApiException) {
            assertEquals(402, e.statusCode)
            assertEquals("INSUFFICIENT_BALANCE", e.code)
            assertEquals("out of credits — top up to continue", e.message)
            assertEquals("req_err", e.requestId)
        }
    }

    @Test
    fun notFound_404_onUnknownStream() = runTest {
        enqueueJson(
            404,
            """{"error":{"message":"session rt_x not found","type":"not_found","code":"not_found"}}""",
        )

        try {
            client.getAvatarRealtimeSession("rt_x")
            fail("expected QuantumApiException")
        } catch (e: QuantumApiException) {
            assertEquals(404, e.statusCode)
            assertTrue(e.isNotFound)
            assertEquals("not_found", e.code)
        }
    }

    @Test
    fun providerError_410_onClosedTextStream() = runTest {
        enqueueJson(
            410,
            """{"error":{"message":"text stream already closed","type":"provider_error","code":"provider_error"}}""",
        )

        try {
            client.sendAvatarRealtimeText("rt_1", AvatarRealtimeTextRequest.delta("late"))
            fail("expected QuantumApiException")
        } catch (e: QuantumApiException) {
            assertEquals(410, e.statusCode)
            assertEquals("provider_error", e.code)
        }
    }
}
