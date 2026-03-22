# Quantum SDK for Android

Kotlin SDK for the [Cosmic Duck](https://cosmicduck.dev) / Quantum AI API.

## Installation

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.cosmicduck:quantum-sdk:1.0.0")
}
```

## Quick Start

```kotlin
import dev.cosmicduck.sdk.QuantumClient
import dev.cosmicduck.sdk.models.*

val client = QuantumClient(apiKey = "qai_k_xxx")
```

### Chat

```kotlin
val response = client.chat(
    ChatRequest(
        model = "gemini-2.5-flash",
        messages = listOf(ChatMessage.user("Hello!"))
    )
)
println(response.text())
```

### Streaming Chat

```kotlin
client.chatStream(
    ChatRequest(
        model = "claude-sonnet-4-6",
        messages = listOf(ChatMessage.user("Write a poem"))
    )
).collect { event ->
    when (event.type) {
        "content_delta" -> print(event.delta?.text ?: "")
        "thinking_delta" -> { /* thinking content */ }
        "tool_use" -> { /* handle tool call */ }
        "done" -> println("\n[Done]")
    }
}
```

### Session Chat (server-managed history)

```kotlin
// Start a new session
val resp = client.chatSession(SessionChatRequest(
    message = "Hello!",
    model = "gemini-2.5-flash",
))

// Continue the conversation
val resp2 = client.chatSession(SessionChatRequest(
    sessionId = resp.sessionId,
    message = "Tell me more",
))
```

### Image Generation

```kotlin
val images = client.generateImage(ImageRequest(
    model = "grok-imagine-image",
    prompt = "A cosmic duck in space",
))
println(images.images.first().url)
```

### Text-to-Speech

```kotlin
val tts = client.speak(TTSRequest(
    text = "Welcome to Cosmic Duck!",
    voice = "alloy",
))
println(tts.audioUrl)
```

### Agent Orchestration

```kotlin
client.agentRun(AgentRequest(
    task = "Research the latest AI papers and summarize them"
)).collect { event ->
    println("${event.type}: ${event.content ?: ""}")
}
```

### Async Jobs (Video, 3D, etc.)

```kotlin
val job = client.createJob(JobCreateRequest(
    type = "3d/generate",
    params = mapOf("model" to JsonPrimitive("meshy-6"), "prompt" to JsonPrimitive("a robot"))
))

val result = client.pollJob(job.jobId)
println("Status: ${result.status}")
```

## API Coverage

| Category | Endpoints |
|----------|-----------|
| **Chat** | chat, chatStream, chatSession |
| **Agent** | agentRun, missionRun |
| **Image** | generateImage, editImage |
| **Audio** | speak, transcribe, soundEffects, generateMusic, dialogue, speechToSpeech, isolateVoice, remixVoice, dub, align, voiceDesign, starfishTTS, generateMusicAdvanced, listFinetunes, createFinetune, deleteFinetune |
| **Video** | generateVideo, videoStudio, videoTranslate, videoPhotoAvatar, videoDigitalTwin, videoAvatars, videoTemplates, videoHeygenVoices |
| **Embeddings** | embed |
| **Documents** | extractDocument, chunkDocument, processDocument |
| **RAG** | ragSearch, ragCorpora, surrealRagSearch, surrealRagProviders |
| **Models** | listModels, getPricing |
| **Account** | accountBalance, accountUsage, accountUsageSummary, accountPricing |
| **Jobs** | createJob, getJob, pollJob, listJobs, generate3D |
| **Keys** | createKey, listKeys, revokeKey |
| **Compute** | computeTemplates, computeProvision, computeInstances, computeInstance, computeDelete, computeSSHKey, computeKeepalive |
| **Voices** | listVoices, cloneVoice, deleteVoice, voiceLibrary, addVoiceFromLibrary |
| **Realtime** | realtimeSession, realtimeEnd, realtimeRefresh |
| **Batch** | batchSubmit, batchSubmitJsonl, batchJobs, batchJob |
| **Credits** | creditPacks, creditPurchase, creditBalance, creditTiers, devProgramApply |
| **Auth** | authApple |
| **Contact** | contact |

## Requirements

- Android minSdk 26
- Kotlin 1.9+
- Internet permission (automatically included)

## Dependencies

- kotlinx.coroutines
- kotlinx.serialization
- OkHttp 4

## License

MIT
