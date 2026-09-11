package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.TtsRequest
import ai.quantumencoding.sdk.models.TtsSpeaker
import ai.quantumencoding.sdk.models.TtsVoiceSettings
import ai.quantumencoding.sdk.models.VoicesResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The TTS request contract on POST /qai/v1/audio/tts, and the voice catalogue
 * on GET /qai/v1/voices.
 *
 * Gemini exposes no parameters for tone, accent or pace — the steering is
 * prose in `instructions` plus inline tags inside `text` — so these tests pin
 * the field names the handler actually decodes. Source of truth: backend
 * internal/server/routes_media.go (ttsRequest, ttsVoiceSettings, ttsSpeaker)
 * and internal/server/routes_voice.go (voiceResponse).
 */
class TtsContractTest {
    // Mirrors HttpClient's encoder: defaults and nulls are dropped, which is
    // what makes an unset model absent rather than "".
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }

    private fun encode(req: TtsRequest): JsonObject =
        json.encodeToJsonElement(TtsRequest.serializer(), req).jsonObject

    /**
     * The gateway supplies gemini-3.1-flash-tts-preview + Laomedeia when the
     * request names neither. Sending "model": "" would pin the request to a
     * model that does not exist.
     */
    @Test
    fun `text alone is a complete request`() {
        val body = encode(TtsRequest(text = "Hello"))

        assertEquals("Hello", body["text"]?.jsonPrimitive?.content)
        for (key in listOf("model", "voice", "speakers", "voice_settings", "instructions")) {
            assertFalse("$key sent on a request that set none", body.containsKey(key))
        }
    }

    @Test
    fun `the steering fields use the wire names`() {
        val body = encode(
            TtsRequest(
                model = "gemini-3.1-flash-tts-preview",
                text = "[excited] Hi! [whispers] can you keep a secret?",
                voice = "Laomedeia",
                outputFormat = "wav",
                speed = 1.1,
                instructions = "Read aloud with a natural British accent",
                language = "en-GB",
                sampleRate = 24000,
                bitRate = 128000,
            )
        )

        assertEquals("gemini-3.1-flash-tts-preview", body["model"]?.jsonPrimitive?.content)
        assertEquals("Laomedeia", body["voice"]?.jsonPrimitive?.content)
        // outputFormat rides as "format" — the handler reads no other key.
        assertEquals("wav", body["format"]?.jsonPrimitive?.content)
        assertFalse(body.containsKey("outputFormat"))
        assertEquals("1.1", body["speed"]?.jsonPrimitive?.content)
        assertEquals(
            "Read aloud with a natural British accent",
            body["instructions"]?.jsonPrimitive?.content,
        )
        assertEquals("en-GB", body["language"]?.jsonPrimitive?.content)
        assertEquals("24000", body["sample_rate"]?.jsonPrimitive?.content)
        assertEquals("128000", body["bit_rate"]?.jsonPrimitive?.content)
    }

    /** Two speakers, labelled to match the lines the text carries. */
    @Test
    fun `a two-speaker dialogue serializes`() {
        val body = encode(
            TtsRequest(
                text = "Lacey: Hi there.\nCustomer: [excited] Hi!",
                instructions = "Lacey is calm; the customer is cheerful",
                speakers = listOf(
                    TtsSpeaker(name = "Lacey", voice = "Laomedeia"),
                    TtsSpeaker(name = "Customer", voice = "Puck"),
                ),
            )
        )

        val speakers = body["speakers"]!!.jsonArray
        assertEquals("gemini takes exactly two speakers", 2, speakers.size)
        assertEquals("Lacey", speakers[0].jsonObject["name"]?.jsonPrimitive?.content)
        assertEquals("Laomedeia", speakers[0].jsonObject["voice"]?.jsonPrimitive?.content)
        assertEquals("Customer", speakers[1].jsonObject["name"]?.jsonPrimitive?.content)
        assertEquals("Puck", speakers[1].jsonObject["voice"]?.jsonPrimitive?.content)
    }

    /**
     * 0.0 stability is a real setting the provider honours, so a zeroed
     * object silently retunes the voice rather than leaving the default.
     */
    @Test
    fun `voice settings omit what was not set`() {
        val body = encode(
            TtsRequest(
                text = "hi",
                voiceSettings = TtsVoiceSettings(stability = 0.4, useSpeakerBoost = true),
            )
        )

        val vs = body["voice_settings"]!!.jsonObject
        assertEquals("0.4", vs["stability"]?.jsonPrimitive?.content)
        assertEquals("true", vs["use_speaker_boost"]?.jsonPrimitive?.content)
        assertFalse("an unset knob was sent as zero", vs.containsKey("similarity_boost"))
        assertFalse("an unset knob was sent as zero", vs.containsKey("style"))
    }

    /** The catalogue a picker is built from. */
    @Test
    fun `the voice listing decodes every documented field`() {
        val wire = """
            {"voices":[
              {"voice_id":"Laomedeia","name":"Laomedeia","category":"premade",
               "provider":"gemini","model":"gemini-3.1-flash-tts-preview","is_cloned":false},
              {"voice_id":"el_7f3","name":"Rachel","category":"cloned",
               "provider":"elevenlabs","model":"eleven_multilingual_v2","is_cloned":true,
               "description":"warm narrator","preview_url":"https://cdn/x.mp3"}
            ],"request_id":"qai_req_1"}
        """.trimIndent()

        val resp = json.decodeFromString(VoicesResponse.serializer(), wire)
        assertEquals(2, resp.voices.size)
        assertEquals("qai_req_1", resp.requestId)

        val gemini = resp.voices[0]
        assertEquals("gemini", gemini.provider)
        // The model to pass back for this voice, so a picker never hardcodes
        // the provider-to-model mapping.
        assertEquals("gemini-3.1-flash-tts-preview", gemini.model)
        assertEquals("premade", gemini.category)
        assertEquals(false, gemini.isCloned)

        val el = resp.voices[1]
        assertEquals(true, el.isCloned)
        assertEquals("warm narrator", el.description)
        assertNull(gemini.description)
    }
}
