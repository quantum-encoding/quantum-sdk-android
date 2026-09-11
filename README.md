# quantum-sdk

Kotlin client SDK for the [Quantum AI API](https://api.quantumencoding.ai).

### Gradle

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.cosmicduck:quantum-sdk:0.4.0")
}
```

## Quick Start

```kotlin
import dev.cosmicduck.sdk.QuantumClient

val client = QuantumClient("qai_k_your_key_here")
val response = client.chat("gemini-2.5-flash", "Hello! What is quantum computing?")
println(response.text())
```

## Features

- 110+ endpoints across 10 AI providers and 45+ models
- Kotlin coroutines for async operations
- Streaming via `Flow<StreamEvent>`
- kotlinx.serialization for type-safe responses
- Android (minSdk 26) and JVM targets
- Agent orchestration with SSE event streams
- GPU/CPU compute rental (requires per-account admin approval)
- Batch processing (50% discount)

## Examples

### Chat Completion

```kotlin
import dev.cosmicduck.sdk.QuantumClient
import dev.cosmicduck.sdk.models.*

val client = QuantumClient("qai_k_your_key_here")

val response = client.chat(ChatRequest(
    model = "claude-sonnet-4-6",
    messages = listOf(
        ChatMessage.system("You are a helpful assistant."),
        ChatMessage.user("Explain coroutines in Kotlin"),
    ),
    temperature = 0.7,
    maxTokens = 1000,
))
println(response.text())
```

### Streaming

```kotlin
client.chatStream(ChatRequest(
    model = "claude-sonnet-4-6",
    messages = listOf(ChatMessage.user("Write a haiku about Kotlin")),
)).collect { event ->
    when (event.type) {
        "content_delta" -> print(event.delta?.text ?: "")
        "done" -> println("\n[Done]")
    }
}
```

### Reasoning state across a tool loop

Reasoning models on the OpenAI and xAI lanes mint a `reasoning` content block
alongside their `tool_use` blocks. It is the provider's own state, opaque, and
it must go back **unchanged and in the same position** on the next turn's
assistant message — its place among the tool calls is how the provider learns
where the reasoning sat. Drop it and the reasoning tokens are re-billed on
every round of the loop.

The simplest correct thing is to hand the whole `content` list back:

```kotlin
val messages = mutableListOf(ChatMessage.user("What is the weather in Oslo?"))

val response = client.chat(
    ChatRequest(
        model = "gpt-5.6",
        messages = messages,
        // One key per conversation, reused on every turn, so all of them land
        // on the same warm provider cache shard.
        promptCacheKey = "conv-7f3a",
    )
)

// Verbatim, in order: reasoning blocks, tool_use blocks, text blocks.
messages += ChatMessage(role = "assistant", contentBlocks = response.content)
// ... then add one tool-result message per tool_use block and loop.
```

`ContentBlock.reasoning` is a `JsonElement` holding the provider's item as it
arrived — never inspect or rebuild it. A `thinking` block is the
human-readable summary of the same turn: render that one, replay this one.

On Gemini 3 the equivalent state is `ContentBlock.thoughtSignature`, and it now
rides the **text** block of a turn that ended in text as well as the `tool_use`
blocks. Streaming delivers it as a `thought_signature` event just before
`done`, on `StreamEvent.thoughtSignature`.

### Provider options

`providerOptions` is an open `JsonObject`, so a key the gateway documents but
this SDK version does not name still rides through — as does the flat `region`
entry:

```kotlin
ChatRequest(
    model = "gpt-5.6",
    messages = messages,
    providerOptions = buildJsonObject {
        putJsonObject("openai") {
            put("reasoning_summary", "detailed")   // auto | concise | detailed | none
            put("reasoning_mode", "pro")           // standard | pro
            put("verbosity", "low")                // low | medium | high
            put("text_format", "json_object")      // text | json_object
        }
        putJsonObject("xai") { put("native_files", true) }
        put("region", "europe")                    // americas | europe | asia
    },
)
```

`reasoningEffort` accepts `none`, `low`, `medium`, `high`, `xhigh` and `max` on
both the chat and session lanes; each adapter folds a tier its model lacks onto
the nearest one.

### Image Generation

```kotlin
val images = client.generateImage(ImageRequest(
    model = "grok-imagine-image",
    prompt = "A cosmic duck in space",
))
for (image in images.images) {
    println(image.url ?: "base64")
}
```

### Text-to-Speech

```kotlin
val audio = client.speak(TtsRequest(
    model = "gpt-4o-mini-tts",
    text = "Welcome to Quantum AI!",
    voice = "alloy",
    outputFormat = "mp3",
))
// The audio arrives inline, base64-encoded — there is no URL to fetch.
println("${audio.sizeBytes} bytes of ${audio.format}")
```

### Steering a Gemini voice

The gateway's house voice is **Gemini 3.1 Flash TTS**
(`gemini-3.1-flash-tts-preview`) with the **Laomedeia** voice; both apply when
the request names neither, so `text` alone is a complete request.

Gemini has no knobs for tone, accent or pace. You steer it in prose — with
`instructions` for the whole read, and with inline tags inside `text` for
moment-to-moment inflection.

```kotlin
val audio = client.speak(TtsRequest(
    // No model: the gateway supplies Gemini 3.1 Flash TTS + Laomedeia.
    text = "Hi, this is Lacey from CRG Direct. [warmly] How can I help today?",
    instructions = "Read aloud as a friendly, professional customer-service " +
        "assistant with a natural British accent, at a natural easy pace",
    language = "en-GB",
))
```

`instructions` carries tone and character ("like telling a friend about
something you love"), accent ("with a natural British accent" — pair it with
`language` so the pronunciation family matches), and pace ("slow down on the
phone number"). Spell digits with separators — `0-1-2-3, 4-5-6` — to have them
read one at a time.

**Inline tags** go in the text itself: `[amazed] [crying] [curious] [excited]
[sighs] [gasp] [giggles] [laughs] [mischievously] [panicked] [sarcastic]
[serious] [shouting] [tired] [trembling] [whispers]`, plus free-form ones like
`[like a cartoon dog]`.

**Two-speaker dialogue** replaces `voice` with `speakers`. Exactly two — the
gateway rejects any other count with a 400 — and the text carries each
speaker's lines under the matching label:

```kotlin
val audio = client.speak(TtsRequest(
    text = "Lacey: Hi, this is Lacey from CRG Direct. How can I help?\n" +
        "Customer: [excited] Hi! I'm calling about Tuesday's installation.",
    instructions = "Lacey is calm and professional; the customer is cheerful",
    speakers = listOf(
        TtsSpeaker(name = "Lacey", voice = "Laomedeia"),
        TtsSpeaker(name = "Customer", voice = "Puck"),
    ),
))
```

All 30 Gemini prebuilt voices (Zephyr, Puck, Charon, Kore, Laomedeia,
Sulafat, …) work on every Gemini TTS model. `client.listVoices()` returns the
catalogue with each voice's `provider` and the `model` to pass back for it, so
a picker never hardcodes the provider-to-model mapping.

Limits: 32k-token session context, two speakers maximum, and quality drifts
past a few minutes of audio — split long scripts.

`speed`, `sampleRate` and `bitRate` are xAI-only; `voiceSettings` is
ElevenLabs-only. On Gemini, ask for pace in `instructions` instead.

### Web Search

```kotlin
val results = client.webSearch("latest Kotlin releases 2026")
for (result in results.results) {
    println("${result.title}: ${result.url}")
}
```

### Agent Orchestration

```kotlin
client.agentRun(AgentRequest(
    task = "Research quantum computing breakthroughs"
)).collect { event ->
    when (event.type) {
        "content_delta" -> print(event.content ?: "")
        "done" -> println("\n--- Done ---")
    }
}
```

## All Endpoints

| Category | Endpoints | Description |
|----------|-----------|-------------|
| Chat | 2 | Text generation + session chat |
| Agent | 2 | Multi-step orchestration + missions |
| Images | 2 | Generation + editing |
| Video | 7 | Generation, studio, translation, avatars |
| Audio | 13 | TTS, STT, music, dialogue, dubbing, voice design |
| Voices | 5 | Clone, list, delete, library, design |
| Embeddings | 1 | Text embeddings |
| RAG | 4 | Vertex AI + SurrealDB search |
| Documents | 3 | Extract, chunk, process |
| Search | 3 | Web search, context, answers |
| Scanner | 11 | Code scanning, type queries, diffs |
| Scraper | 2 | Doc scraping + screenshots |
| Jobs | 3 | Async job management |
| Compute | 7 | GPU/CPU rental (admin-approved accounts only) |
| Keys | 3 | API key management |
| Account | 3 | Balance, usage, summary |
| Credits | 6 | Packs, tiers, lifetime, purchase |
| Batch | 4 | 50% discount batch processing |
| Realtime | 3 | Voice sessions |
| Models | 2 | Model list + pricing |

## Authentication

Pass your API key when creating the client:

```kotlin
val client = QuantumClient("qai_k_your_key_here")
```

The SDK sends it as the `X-API-Key` header. Both `qai_...` (primary) and `qai_k_...` (scoped) keys are supported. You can also use `Authorization: Bearer <key>`.

Get your API key at [cosmicduck.dev](https://cosmicduck.dev).

## Pricing

See [api.quantumencoding.ai/pricing](https://api.quantumencoding.ai/pricing) for current rates.

The **Lifetime tier** offers 0% margin at-cost pricing via a one-time payment.

## Other SDKs

All SDKs are at v0.4.0 with type parity verified by scanner.

| Language | Package | Install |
|----------|---------|---------|
| Rust | quantum-sdk | `cargo add quantum-sdk` |
| Go | quantum-sdk | `go get github.com/quantum-encoding/quantum-sdk` |
| TypeScript | @quantum-encoding/quantum-sdk | `npm i @quantum-encoding/quantum-sdk` |
| Python | quantum-sdk | `pip install quantum-sdk` |
| Swift | QuantumSDK | Swift Package Manager |
| **Kotlin** | quantum-sdk | Gradle dependency |

MCP server: `npx @quantum-encoding/ai-conductor-mcp`

## API Reference

- Interactive docs: [api.quantumencoding.ai/docs](https://api.quantumencoding.ai/docs)
- OpenAPI spec: [api.quantumencoding.ai/openapi.yaml](https://api.quantumencoding.ai/openapi.yaml)
- LLM context: [api.quantumencoding.ai/llms.txt](https://api.quantumencoding.ai/llms.txt)

## License

MIT
