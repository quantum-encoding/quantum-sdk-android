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
- GPU/CPU compute rental
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
val audio = client.speak(TTSRequest(
    text = "Welcome to Quantum AI!",
    voice = "alloy",
))
println(audio.audioUrl)
```

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
| Compute | 7 | GPU/CPU rental |
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
