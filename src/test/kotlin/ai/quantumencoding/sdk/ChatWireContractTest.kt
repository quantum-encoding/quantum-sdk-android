package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.ChatMessage
import ai.quantumencoding.sdk.models.ChatRequest
import ai.quantumencoding.sdk.models.ChatResponse
import ai.quantumencoding.sdk.models.ContentBlock
import ai.quantumencoding.sdk.models.SessionChatRequest
import ai.quantumencoding.sdk.networking.SseClient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The chat wire contract the gateway added on 2026-09-11: the reasoning state
 * a tool loop must hand back, the cache key that keeps a conversation on one
 * provider shard, and the Gemini 3 signature that now rides text blocks as
 * well as tool_use blocks.
 *
 * Source of truth: backend internal/server/convert.go (ChatRequest,
 * ContentBlock) and internal/server/sse.go (the thought_signature event).
 */
class ChatWireContractTest {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    private fun encode(request: ChatRequest): JsonObject =
        json.encodeToJsonElement(ChatRequest.serializer(), request).jsonObject

    // ── prompt_cache_key ──────────────────────────────────────────────

    /**
     * Omitted, the gateway derives a key from the caller's identity — so an
     * absent field is a real default, not an oversight to paper over.
     */
    @Test
    fun `prompt_cache_key rides only when the caller sets one`() {
        val bare = encode(ChatRequest(model = "gpt-5.6", messages = listOf(ChatMessage.user("hi"))))
        assertFalse("prompt_cache_key sent on a request that named none", bare.containsKey("prompt_cache_key"))

        val keyed = encode(
            ChatRequest(
                model = "gpt-5.6",
                messages = listOf(ChatMessage.user("hi")),
                promptCacheKey = "conv-7f3a",
            )
        )
        assertEquals("conv-7f3a", keyed["prompt_cache_key"]?.jsonPrimitive?.content)
    }

    // ── reasoning blocks ──────────────────────────────────────────────

    /**
     * The provider's reasoning item is opaque, and its POSITION among the tool
     * calls is the state the provider reads back.
     */
    @Test
    fun `a reasoning block round-trips verbatim and in place`() {
        val wire = """
            {"id":"req_1","model":"gpt-5.6","stop_reason":"tool_use","content":[
              {"type":"reasoning",
               "reasoning":{"id":"rs_abc","summary":[],"encrypted_content":"Zm9v"},
               "minted_by":"gpt-5.6"},
              {"type":"tool_use","id":"call_1","name":"lookup","input":{"q":"x"}}
            ]}
        """.trimIndent()

        val response = json.decodeFromString(ChatResponse.serializer(), wire)
        assertEquals(2, response.content.size)

        val block = response.content[0]
        assertEquals("reasoning", block.blockType)
        assertEquals("gpt-5.6", block.mintedBy)
        val item = block.reasoning!!.jsonObject
        assertEquals("rs_abc", item["id"]?.jsonPrimitive?.content)
        assertEquals("Zm9v", item["encrypted_content"]?.jsonPrimitive?.content)

        // Echoed back on the next turn's assistant message, unchanged and in
        // the same order.
        val message = ChatMessage(role = "assistant", contentBlocks = response.content)
        val echoed = json.encodeToJsonElement(ChatMessage.serializer(), message).jsonObject
        val blocks = json.decodeFromJsonElement(
            ListSerializer(ContentBlock.serializer()),
            echoed["content_blocks"]!!,
        )
        assertEquals(listOf("reasoning", "tool_use"), blocks.map { it.blockType })
        assertEquals(item, blocks[0].reasoning!!.jsonObject)
        assertEquals("gpt-5.6", blocks[0].mintedBy)
    }

    /**
     * Absent is not empty: a null reasoning item is not something a provider
     * will accept back.
     */
    @Test
    fun `a block with no reasoning state sends neither field`() {
        val encoded = json.encodeToJsonElement(
            ContentBlock.serializer(),
            ContentBlock(blockType = "text", text = "hello"),
        ).jsonObject
        for (key in listOf("reasoning", "minted_by", "thought_signature")) {
            assertFalse("$key sent on a plain text block", encoded.containsKey(key))
        }
    }

    // ── thought_signature ─────────────────────────────────────────────

    /** Gemini 3 puts the signature on the TEXT block, not only on tool_use. */
    @Test
    fun `a thought signature rides a text block`() {
        val wire = """
            {"id":"r","model":"gemini-3.5-flash","stop_reason":"end_turn",
             "content":[{"type":"text","text":"hi","thought_signature":"c2ln"}]}
        """.trimIndent()
        val response = json.decodeFromString(ChatResponse.serializer(), wire)
        assertEquals("c2ln", response.content[0].thoughtSignature)

        val echoed = json.encodeToJsonElement(ContentBlock.serializer(), response.content[0]).jsonObject
        assertEquals("c2ln", echoed["thought_signature"]?.jsonPrimitive?.content)
    }

    /**
     * The gateway sends the signature as its own event just before done, and
     * on the atomic tool_use event a streaming tool loop reads.
     */
    @Test
    fun `the thought_signature stream event is parsed`() {
        val standalone = SseClient.parseStreamEvent(
            json.parseToJsonElement("""{"type":"thought_signature","thought_signature":"c2ln"}""").jsonObject
        )
        assertEquals("thought_signature", standalone.type)
        assertEquals("c2ln", standalone.thoughtSignature)

        val toolUse = SseClient.parseStreamEvent(
            json.parseToJsonElement(
                """{"type":"tool_use","id":"call_1","name":"lookup","input":{},"thought_signature":"c2ln"}"""
            ).jsonObject
        )
        assertEquals("c2ln", toolUse.thoughtSignature)
        assertEquals("call_1", toolUse.toolUse?.id)

        val plain = SseClient.parseStreamEvent(
            json.parseToJsonElement("""{"type":"content_delta","delta":{"text":"hi"}}""").jsonObject
        )
        assertNull(plain.thoughtSignature)
    }

    // ── provider_options ──────────────────────────────────────────────

    /**
     * The map is open: nested per-provider objects, the flat region entry, and
     * a key this SDK version never heard of all reach the gateway untouched.
     */
    @Test
    fun `provider_options is an open map`() {
        val options = json.parseToJsonElement(
            """
            {"openai":{"reasoning_summary":"detailed","reasoning_mode":"pro",
                       "verbosity":"low","text_format":"json_object",
                       "a_key_this_sdk_never_heard_of":42},
             "xai":{"native_files":true},
             "region":"europe"}
            """.trimIndent()
        ).jsonObject

        val sent = encode(
            ChatRequest(
                model = "gpt-5.6",
                messages = listOf(ChatMessage.user("hi")),
                providerOptions = options,
            )
        )["provider_options"]!!.jsonObject

        assertEquals(options, sent)
        val openai = sent["openai"]!!.jsonObject
        assertEquals("pro", openai["reasoning_mode"]?.jsonPrimitive?.content)
        assertEquals("42", openai["a_key_this_sdk_never_heard_of"]?.jsonPrimitive?.content)
        assertEquals("true", sent["xai"]!!.jsonObject["native_files"]?.jsonPrimitive?.content)
        assertEquals("europe", sent["region"]?.jsonPrimitive?.content)
    }

    // ── reasoning_effort ──────────────────────────────────────────────

    @Test
    fun `reasoning_effort carries every tier the gateway validates`() {
        for (tier in listOf("none", "low", "medium", "high", "xhigh", "max")) {
            val sent = encode(
                ChatRequest(
                    model = "gpt-5.6",
                    messages = listOf(ChatMessage.user("hi")),
                    reasoningEffort = tier,
                )
            )
            assertEquals(tier, sent["reasoning_effort"]?.jsonPrimitive?.content)
        }
    }

    /**
     * The session lane validates the same tiers; until now this SDK had no
     * field to send them on.
     */
    @Test
    fun `the session request carries reasoning_effort too`() {
        val sent = json.encodeToJsonElement(
            SessionChatRequest.serializer(),
            SessionChatRequest(sessionId = "sess_1", message = "hi", reasoningEffort = "max"),
        ).jsonObject
        assertEquals("max", sent["reasoning_effort"]?.jsonPrimitive?.content)

        // And no prompt_cache_key: the session derives its own from the
        // session ID.
        assertTrue(sent.keys.none { it == "prompt_cache_key" })
    }
}
