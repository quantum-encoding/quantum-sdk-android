package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.*
import ai.quantumencoding.sdk.networking.HttpClient
import ai.quantumencoding.sdk.networking.SseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.OkHttpClient

/**
 * Default Quantum AI API base URL.
 */
const val DEFAULT_BASE_URL = "https://api.quantumencoding.ai"

/**
 * Number of ticks in one US dollar (10 billion).
 */
const val TICKS_PER_USD = 10_000_000_000L

/**
 * QuantumClient is the main entry point for the Cosmic Duck / Quantum AI API.
 *
 * All methods are suspend functions suitable for use with Kotlin coroutines.
 * Streaming methods return [Flow] instances.
 *
 * ## Usage
 * ```kotlin
 * val client = QuantumClient(apiKey = "qai_k_xxx")
 *
 * // Simple chat
 * val response = client.chat(
 *     ChatRequest(
 *         model = "gemini-2.5-flash",
 *         messages = listOf(ChatMessage.user("Hello!"))
 *     )
 * )
 * println(response.text())
 *
 * // Streaming chat
 * client.chatStream(
 *     ChatRequest(
 *         model = "claude-sonnet-4-6",
 *         messages = listOf(ChatMessage.user("Write a poem"))
 *     )
 * ).collect { event ->
 *     print(event.delta?.text ?: "")
 * }
 * ```
 *
 * @param apiKey API key with `qai_` or `qai_k_` prefix.
 * @param baseUrl Override the default API base URL.
 * @param httpClient Optional custom OkHttpClient instance.
 */
class QuantumClient(
    apiKey: String,
    baseUrl: String = DEFAULT_BASE_URL,
    httpClient: OkHttpClient? = null,
) {
    private val http = HttpClient(apiKey, baseUrl, httpClient)

    // ── Chat ─────────────────────────────────────────────────────────

    /**
     * Send a non-streaming chat request.
     *
     * @param request Chat request with model, messages, and options.
     * @return Chat response with content, usage, and cost.
     */
    suspend fun chat(request: ChatRequest): ChatResponse {
        val body = request.copy(stream = false)
        val (data, meta) = http.doJson<ChatResponse>("POST", "/qai/v1/chat", body)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty() && meta.requestId.isNotEmpty()) it.copy(requestId = meta.requestId) else it
        }.let {
            if (it.model.isEmpty() && meta.model.isNotEmpty()) it.copy(model = meta.model) else it
        }
    }

    /**
     * Send a streaming chat request. Returns a [Flow] of [StreamEvent].
     *
     * The last event will have `done = true`. Cancel the flow collection to abort.
     *
     * ```kotlin
     * client.chatStream(request).collect { event ->
     *     when (event.type) {
     *         "content_delta" -> print(event.delta?.text ?: "")
     *         "thinking_delta" -> { /* thinking content */ }
     *         "tool_use" -> { /* handle tool call */ }
     *         "usage" -> { /* final usage stats */ }
     *         "done" -> { /* stream complete */ }
     *     }
     * }
     * ```
     */
    fun chatStream(request: ChatRequest): Flow<StreamEvent> {
        val body = request.copy(stream = true)
        return SseClient.parseChatStreamFromClient(http, "/qai/v1/chat", body)
    }

    // ── Session Chat ─────────────────────────────────────────────────

    /**
     * Send a session-based chat request. The server manages conversation history.
     *
     * ```kotlin
     * // Start a new session
     * val resp = client.chatSession(SessionChatRequest(
     *     message = "Hello!",
     *     model = "claude-sonnet-4-6",
     * ))
     *
     * // Continue the conversation
     * val resp2 = client.chatSession(SessionChatRequest(
     *     sessionId = resp.sessionId,
     *     message = "Tell me more",
     * ))
     * ```
     */
    suspend fun chatSession(request: SessionChatRequest): SessionChatResponse {
        val body = request.copy(stream = false)
        val (data, meta) = http.doJson<SessionChatResponse>("POST", "/qai/v1/chat/session", body)
        return data
    }

    // ── Agent ────────────────────────────────────────────────────────

    /**
     * Run a server-side agent orchestration. Returns a [Flow] of [AgentEvent].
     *
     * ```kotlin
     * client.agentRun(AgentRequest(
     *     task = "Research the latest AI papers and summarize them"
     * )).collect { event ->
     *     println("${event.type}: ${event.content ?: ""}")
     * }
     * ```
     */
    fun agentRun(request: AgentRequest): Flow<AgentEvent> {
        return SseClient.parseAgentStreamFromClient(http, "/qai/v1/agent", request)
    }

    /**
     * Run a full mission orchestration. Returns a [Flow] of [MissionEvent].
     *
     * ```kotlin
     * client.missionRun(MissionRequest(
     *     goal = "Build a REST API server in Go"
     * )).collect { event ->
     *     println("${event.type}: ${event.content ?: ""}")
     * }
     * ```
     */
    fun missionRun(request: MissionRequest): Flow<MissionEvent> {
        return SseClient.parseMissionStreamFromClient(http, "/qai/v1/missions", request)
    }

    // ── Image ────────────────────────────────────────────────────────

    /**
     * Generate images from a text prompt.
     *
     * @param request Image generation request with model and prompt.
     * @return Image response with generated image URLs.
     */
    suspend fun generateImage(request: ImageRequest): ImageResponse {
        val (data, meta) = http.doJson<ImageResponse>("POST", "/qai/v1/images/generate", request)
        return backfill(data, meta)
    }

    /**
     * Edit images using an AI model.
     *
     * @param request Image edit request with source image and prompt.
     * @return Image edit response with modified image URLs.
     */
    suspend fun editImage(request: ImageEditRequest): ImageEditResponse {
        val (data, meta) = http.doJson<ImageEditResponse>("POST", "/qai/v1/images/edit", request)
        return backfillEdit(data, meta)
    }

    // ── Audio: TTS / STT ─────────────────────────────────────────────

    /**
     * Generate speech from text.
     */
    suspend fun speak(request: TTSRequest): TTSResponse {
        val (data, meta) = http.doJson<TTSResponse>("POST", "/qai/v1/audio/tts", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Convert speech to text.
     */
    suspend fun transcribe(request: STTRequest): STTResponse {
        val (data, meta) = http.doJson<STTResponse>("POST", "/qai/v1/audio/stt", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Generate sound effects from a text prompt (ElevenLabs).
     */
    suspend fun soundEffects(request: SoundEffectRequest): SoundEffectResponse {
        val (data, meta) = http.doJson<SoundEffectResponse>("POST", "/qai/v1/audio/sound-effects", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Generate music from a text prompt.
     */
    suspend fun generateMusic(request: MusicRequest): MusicResponse {
        val (data, meta) = http.doJson<MusicResponse>("POST", "/qai/v1/audio/music", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Generate multi-speaker dialogue audio (ElevenLabs).
     */
    suspend fun dialogue(request: DialogueRequest): DialogueResponse {
        val (data, meta) = http.doJson<DialogueResponse>("POST", "/qai/v1/audio/dialogue", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Convert speech audio to a different voice (ElevenLabs).
     */
    suspend fun speechToSpeech(request: SpeechToSpeechRequest): SpeechToSpeechResponse {
        val (data, meta) = http.doJson<SpeechToSpeechResponse>("POST", "/qai/v1/audio/speech-to-speech", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Remove background noise and isolate speech (ElevenLabs).
     */
    suspend fun isolateVoice(request: IsolateVoiceRequest): IsolateVoiceResponse {
        val (data, meta) = http.doJson<IsolateVoiceResponse>("POST", "/qai/v1/audio/isolate", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Transform a voice by modifying attributes (ElevenLabs).
     */
    suspend fun remixVoice(request: RemixVoiceRequest): RemixVoiceResponse {
        val (data, meta) = http.doJson<RemixVoiceResponse>("POST", "/qai/v1/audio/remix", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Dub audio/video into a target language (ElevenLabs).
     */
    suspend fun dub(request: DubRequest): DubResponse {
        val (data, meta) = http.doJson<DubResponse>("POST", "/qai/v1/audio/dub", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Get word-level timestamps for audio+text alignment (ElevenLabs).
     */
    suspend fun align(request: AlignRequest): AlignResponse {
        val (data, meta) = http.doJson<AlignResponse>("POST", "/qai/v1/audio/align", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Generate voice previews from a text description (ElevenLabs).
     */
    suspend fun voiceDesign(request: VoiceDesignRequest): VoiceDesignResponse {
        val (data, meta) = http.doJson<VoiceDesignResponse>("POST", "/qai/v1/audio/voice-design", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Generate speech using HeyGen's Starfish TTS model.
     */
    suspend fun starfishTTS(request: StarfishTTSRequest): StarfishTTSResponse {
        val (data, meta) = http.doJson<StarfishTTSResponse>("POST", "/qai/v1/audio/starfish-tts", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Generate music via ElevenLabs Eleven Music (advanced: finetunes, etc).
     */
    suspend fun generateMusicAdvanced(request: MusicAdvancedRequest): MusicAdvancedResponse {
        val (data, meta) = http.doJson<MusicAdvancedResponse>("POST", "/qai/v1/audio/music/advanced", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * List all music finetunes for the authenticated user.
     */
    suspend fun listFinetunes(): MusicFinetuneListResponse {
        val (data, _) = http.doJson<MusicFinetuneListResponse>("GET", "/qai/v1/audio/finetunes")
        return data
    }

    /**
     * Create a new music finetune from audio samples.
     */
    suspend fun createFinetune(request: MusicFinetuneCreateRequest): MusicFinetuneInfo {
        val (data, _) = http.doJson<MusicFinetuneInfo>("POST", "/qai/v1/audio/finetunes", request)
        return data
    }

    /**
     * Delete a music finetune by ID.
     */
    suspend fun deleteFinetune(id: String): StatusResponse {
        val (data, _) = http.doJson<StatusResponse>("DELETE", "/qai/v1/audio/finetunes/$id")
        return data
    }

    // ── Video ────────────────────────────────────────────────────────

    /**
     * Generate a video from a text prompt.
     *
     * Video generation is slow (30s-5min). For production use, consider
     * submitting via the Jobs API instead.
     */
    suspend fun generateVideo(request: VideoRequest): VideoResponse {
        val (data, meta) = http.doJson<VideoResponse>("POST", "/qai/v1/video/generate", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Create a talking-head video via HeyGen Studio. Returns an async job.
     */
    suspend fun videoStudio(request: VideoStudioRequest): AsyncJobResponse {
        val (data, _) = http.doJson<AsyncJobResponse>("POST", "/qai/v1/video/studio", request)
        return data
    }

    /**
     * Submit a video translation job via HeyGen. Returns an async job.
     */
    suspend fun videoTranslate(request: VideoTranslateRequest): AsyncJobResponse {
        val (data, _) = http.doJson<AsyncJobResponse>("POST", "/qai/v1/video/translate", request)
        return data
    }

    /**
     * Create a photo avatar via HeyGen. Returns an async job.
     */
    suspend fun videoPhotoAvatar(request: PhotoAvatarRequest): AsyncJobResponse {
        val (data, _) = http.doJson<AsyncJobResponse>("POST", "/qai/v1/video/photo-avatar", request)
        return data
    }

    /**
     * Create a digital twin via HeyGen. Returns an async job.
     */
    suspend fun videoDigitalTwin(request: DigitalTwinRequest): AsyncJobResponse {
        val (data, _) = http.doJson<AsyncJobResponse>("POST", "/qai/v1/video/digital-twin", request)
        return data
    }

    /**
     * List available HeyGen avatars.
     */
    suspend fun videoAvatars(): AvatarsResponse {
        val (data, _) = http.doJson<AvatarsResponse>("GET", "/qai/v1/video/avatars")
        return data
    }

    /**
     * List available HeyGen templates.
     */
    suspend fun videoTemplates(): HeyGenTemplatesResponse {
        val (data, _) = http.doJson<HeyGenTemplatesResponse>("GET", "/qai/v1/video/templates")
        return data
    }

    /**
     * List available HeyGen voices.
     */
    suspend fun videoHeygenVoices(): HeyGenVoicesResponse {
        val (data, _) = http.doJson<HeyGenVoicesResponse>("GET", "/qai/v1/video/heygen-voices")
        return data
    }

    // ── Embeddings ───────────────────────────────────────────────────

    /**
     * Generate text embeddings for the given inputs.
     *
     * @param request Embedding request with input text(s).
     * @return Embedding vectors.
     */
    suspend fun embed(request: EmbedRequest): EmbedResponse {
        val (data, meta) = http.doJson<EmbedResponse>("POST", "/qai/v1/embeddings", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    // ── Documents ────────────────────────────────────────────────────

    /**
     * Extract text content from a document (PDF, image, etc.).
     */
    suspend fun extractDocument(request: DocumentRequest): DocumentResponse {
        val (data, meta) = http.doJson<DocumentResponse>("POST", "/qai/v1/documents/extract", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Chunk a document into smaller pieces for embedding or processing.
     */
    suspend fun chunkDocument(request: ChunkDocumentRequest): ChunkDocumentResponse {
        val (data, meta) = http.doJson<ChunkDocumentResponse>("POST", "/qai/v1/documents/chunk", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * Process a document with extraction + optional instructions.
     */
    suspend fun processDocument(request: ProcessDocumentRequest): ProcessDocumentResponse {
        val (data, meta) = http.doJson<ProcessDocumentResponse>("POST", "/qai/v1/documents/process", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    // ── RAG ──────────────────────────────────────────────────────────

    /**
     * Search Vertex AI RAG corpora for relevant documentation.
     */
    suspend fun ragSearch(request: RAGSearchRequest): RAGSearchResponse {
        val (data, meta) = http.doJson<RAGSearchResponse>("POST", "/qai/v1/rag/search", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * List available Vertex AI RAG corpora.
     */
    suspend fun ragCorpora(): List<RAGCorpus> {
        val (data, _) = http.doJson<RAGCorporaResponse>("GET", "/qai/v1/rag/corpora")
        return data.corpora
    }

    /**
     * Search provider API documentation via SurrealDB vector search.
     */
    suspend fun surrealRagSearch(request: SurrealRAGSearchRequest): SurrealRAGSearchResponse {
        val (data, meta) = http.doJson<SurrealRAGSearchResponse>("POST", "/qai/v1/rag/surreal/search", request)
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    /**
     * List available documentation providers in SurrealDB RAG.
     */
    suspend fun surrealRagProviders(): SurrealRAGProvidersResponse {
        val (data, _) = http.doJson<SurrealRAGProvidersResponse>("GET", "/qai/v1/rag/surreal/providers")
        return data
    }

    // ── Models ────────────────────────────────────────────────────────

    /**
     * List all available models with provider and pricing information.
     */
    suspend fun listModels(): List<ModelInfo> {
        val (data, _) = http.doJson<ModelsResponseBody>("GET", "/qai/v1/models")
        return data.models
    }

    /**
     * Get the complete pricing table for all models.
     */
    suspend fun getPricing(): List<PricingInfo> {
        val (data, _) = http.doJson<PricingResponseBody>("GET", "/qai/v1/pricing")
        return data.pricing
    }

    // ── Account ──────────────────────────────────────────────────────

    /**
     * Get the account credit balance.
     */
    suspend fun accountBalance(): BalanceResponse {
        val (data, _) = http.doJson<BalanceResponse>("GET", "/qai/v1/account/balance")
        return data
    }

    /**
     * Get paginated usage history.
     *
     * @param query Optional query parameters for pagination.
     */
    suspend fun accountUsage(query: UsageQuery? = null): UsageResponse {
        val params = buildString {
            val parts = mutableListOf<String>()
            query?.limit?.let { parts.add("limit=$it") }
            query?.startAfter?.let { parts.add("start_after=$it") }
            if (parts.isNotEmpty()) {
                append("?")
                append(parts.joinToString("&"))
            }
        }
        val (data, _) = http.doJson<UsageResponse>("GET", "/qai/v1/account/usage$params")
        return data
    }

    /**
     * Get monthly usage summary.
     *
     * @param months Number of months to include (default: server decides).
     */
    suspend fun accountUsageSummary(months: Int? = null): UsageSummaryResponse {
        val path = if (months != null) "/qai/v1/account/usage/summary?months=$months"
        else "/qai/v1/account/usage/summary"
        val (data, _) = http.doJson<UsageSummaryResponse>("GET", path)
        return data
    }

    /**
     * Get the full pricing table (model ID to pricing entry map).
     */
    suspend fun accountPricing(): AccountPricingResponse {
        val (data, _) = http.doJson<AccountPricingResponse>("GET", "/qai/v1/pricing")
        return data
    }

    // ── Jobs ─────────────────────────────────────────────────────────

    /**
     * Create an async job. Returns the job ID for polling.
     */
    suspend fun createJob(request: JobCreateRequest): JobCreateResponse {
        val (data, _) = http.doJson<JobCreateResponse>("POST", "/qai/v1/jobs", request)
        return data
    }

    /**
     * Check the status of an async job.
     */
    suspend fun getJob(jobId: String): JobStatusResponse {
        val (data, _) = http.doJson<JobStatusResponse>("GET", "/qai/v1/jobs/$jobId")
        return data
    }

    /**
     * Poll a job until completion or timeout.
     *
     * @param jobId Job ID to poll.
     * @param intervalMs Polling interval in milliseconds (default 2000).
     * @param maxAttempts Maximum poll attempts before timeout (default 150).
     */
    suspend fun pollJob(
        jobId: String,
        intervalMs: Long = 2000,
        maxAttempts: Int = 150,
    ): JobStatusResponse {
        for (i in 0 until maxAttempts) {
            delay(intervalMs)
            val status = getJob(jobId)
            if (status.status == "completed" || status.status == "failed") {
                return status
            }
        }
        return JobStatusResponse(
            jobId = jobId,
            status = "timeout",
            error = "Job polling timed out after $maxAttempts attempts",
        )
    }

    /**
     * List all jobs for the authenticated user.
     */
    suspend fun listJobs(): JobListResponse {
        val (data, _) = http.doJson<JobListResponse>("GET", "/qai/v1/jobs")
        return data
    }

    /**
     * Submit a 3D model generation job via the async jobs system.
     *
     * @param model Model for 3D generation (e.g. "meshy-6").
     * @param prompt Text prompt.
     * @param imageUrl Image URL for image-to-3D.
     */
    suspend fun generate3D(
        model: String,
        prompt: String? = null,
        imageUrl: String? = null,
    ): JobCreateResponse {
        val params = mutableMapOf<String, JsonElement>(
            "model" to JsonPrimitive(model)
        )
        prompt?.let { params["prompt"] = JsonPrimitive(it) }
        imageUrl?.let { params["image_url"] = JsonPrimitive(it) }

        return createJob(JobCreateRequest(type = "3d/generate", params = params))
    }

    // ── API Keys ─────────────────────────────────────────────────────

    /**
     * Create a scoped API key.
     */
    suspend fun createKey(request: CreateKeyRequest): CreateKeyResponse {
        val (data, _) = http.doJson<CreateKeyResponse>("POST", "/qai/v1/keys", request)
        return data
    }

    /**
     * List all API keys for the authenticated user.
     */
    suspend fun listKeys(): ListKeysResponse {
        val (data, _) = http.doJson<ListKeysResponse>("GET", "/qai/v1/keys")
        return data
    }

    /**
     * Revoke an API key.
     */
    suspend fun revokeKey(id: String): StatusResponse {
        val (data, _) = http.doJson<StatusResponse>("DELETE", "/qai/v1/keys/$id")
        return data
    }

    // ── Compute ──────────────────────────────────────────────────────

    /**
     * Get available compute templates with pricing.
     */
    suspend fun computeTemplates(): TemplatesResponse {
        val (data, _) = http.doJson<TemplatesResponse>("GET", "/qai/v1/compute/templates")
        return data
    }

    /**
     * Provision a new GPU compute instance.
     */
    suspend fun computeProvision(request: ProvisionRequest): ProvisionResponse {
        val (data, _) = http.doJson<ProvisionResponse>("POST", "/qai/v1/compute/provision", request)
        return data
    }

    /**
     * List all compute instances for the authenticated user.
     */
    suspend fun computeInstances(): InstancesResponse {
        val (data, _) = http.doJson<InstancesResponse>("GET", "/qai/v1/compute/instances")
        return data
    }

    /**
     * Get full status of a single compute instance.
     */
    suspend fun computeInstance(id: String): InstanceResponse {
        val (data, _) = http.doJson<InstanceResponse>("GET", "/qai/v1/compute/instance/$id")
        return data
    }

    /**
     * Tear down a compute instance and finalize billing.
     */
    suspend fun computeDelete(id: String): DeleteResponse {
        val (data, _) = http.doJson<DeleteResponse>("DELETE", "/qai/v1/compute/instance/$id")
        return data
    }

    /**
     * Inject an SSH public key into a running instance.
     */
    suspend fun computeSSHKey(id: String, request: SSHKeyRequest): StatusResponse {
        val (data, _) = http.doJson<StatusResponse>("POST", "/qai/v1/compute/instance/$id/ssh-key", request)
        return data
    }

    /**
     * Reset the inactivity timer on a compute instance.
     */
    suspend fun computeKeepalive(id: String): StatusResponse {
        val (data, _) = http.doJson<StatusResponse>("POST", "/qai/v1/compute/instance/$id/keepalive")
        return data
    }

    // ── Voice Management ─────────────────────────────────────────────

    /**
     * List all available voices (ElevenLabs).
     */
    suspend fun listVoices(): VoicesResponse {
        val (data, _) = http.doJson<VoicesResponse>("GET", "/qai/v1/voices")
        return data
    }

    /**
     * Create an instant voice clone from audio samples (ElevenLabs).
     */
    suspend fun cloneVoice(request: CloneVoiceRequest): CloneVoiceResponse {
        val (data, _) = http.doJson<CloneVoiceResponse>("POST", "/qai/v1/voices/clone", request)
        return data
    }

    /**
     * Delete a cloned voice (ElevenLabs).
     */
    suspend fun deleteVoice(id: String): StatusResponse {
        val (data, _) = http.doJson<StatusResponse>("DELETE", "/qai/v1/voices/$id")
        return data
    }

    /**
     * Browse the shared voice library with optional filters.
     */
    suspend fun voiceLibrary(query: VoiceLibraryQuery? = null): SharedVoicesResponse {
        val params = buildString {
            val parts = mutableListOf<String>()
            query?.query?.let { parts.add("query=$it") }
            query?.pageSize?.let { parts.add("page_size=$it") }
            query?.cursor?.let { parts.add("cursor=$it") }
            query?.gender?.let { parts.add("gender=$it") }
            query?.language?.let { parts.add("language=$it") }
            query?.useCase?.let { parts.add("use_case=$it") }
            if (parts.isNotEmpty()) {
                append("?")
                append(parts.joinToString("&"))
            }
        }
        val (data, _) = http.doJson<SharedVoicesResponse>("GET", "/qai/v1/voices/library$params")
        return data
    }

    /**
     * Add a shared voice from the library to the user's account.
     */
    suspend fun addVoiceFromLibrary(request: AddVoiceFromLibraryRequest): AddVoiceFromLibraryResponse {
        val (data, _) = http.doJson<AddVoiceFromLibraryResponse>("POST", "/qai/v1/voices/library/add", request)
        return data
    }

    // ── Realtime Voice ───────────────────────────────────────────────

    /**
     * Request an ephemeral token for direct voice WebSocket connection.
     *
     * @param provider Optional provider ("xai" default, "elevenlabs").
     */
    suspend fun realtimeSession(provider: String? = null): RealtimeSession {
        val body = if (provider != null) mapOf("provider" to provider) else emptyMap<String, String>()
        val (data, _) = http.doJson<RealtimeSession>("POST", "/qai/v1/realtime/session", body)
        return data
    }

    /**
     * End a realtime session and finalize billing.
     *
     * @param sessionId Session ID from [realtimeSession].
     * @param durationSeconds Duration of the session in seconds.
     */
    suspend fun realtimeEnd(sessionId: String, durationSeconds: Double) {
        http.doJson<StatusResponse>("POST", "/qai/v1/realtime/end", mapOf(
            "session_id" to sessionId,
            "duration_seconds" to durationSeconds,
        ))
    }

    /**
     * Refresh an ephemeral token for long sessions (>4 min).
     *
     * @param sessionId Session ID to refresh.
     * @return New ephemeral token string.
     */
    suspend fun realtimeRefresh(sessionId: String): String {
        val (data, _) = http.doJson<RealtimeRefreshResponse>(
            "POST", "/qai/v1/realtime/refresh",
            mapOf("session_id" to sessionId)
        )
        return data.ephemeralToken
    }

    // ── Batch Processing ─────────────────────────────────────────────

    /**
     * Submit a batch of jobs for processing.
     */
    suspend fun batchSubmit(request: BatchSubmitRequest): BatchSubmitResponse {
        val (data, _) = http.doJson<BatchSubmitResponse>("POST", "/qai/v1/batch", request)
        return data
    }

    /**
     * Submit a batch of jobs using JSONL format.
     *
     * @param jsonl JSONL string with one job per line.
     */
    suspend fun batchSubmitJsonl(jsonl: String): BatchJsonlResponse {
        val (data, _) = http.doJson<BatchJsonlResponse>("POST", "/qai/v1/batch/jsonl", mapOf("jsonl" to jsonl))
        return data
    }

    /**
     * List all batch jobs for the account.
     */
    suspend fun batchJobs(): BatchJobsResponse {
        val (data, _) = http.doJson<BatchJobsResponse>("GET", "/qai/v1/batch/jobs")
        return data
    }

    /**
     * Get the status and result of a single batch job.
     */
    suspend fun batchJob(id: String): BatchJobInfo {
        val (data, _) = http.doJson<BatchJobInfo>("GET", "/qai/v1/batch/jobs/$id")
        return data
    }

    // ── Credits ──────────────────────────────────────────────────────

    /**
     * List available credit packs (no auth required).
     */
    suspend fun creditPacks(): CreditPacksResponse {
        val (data, _) = http.doJson<CreditPacksResponse>("GET", "/qai/v1/credits/packs")
        return data
    }

    /**
     * Purchase a credit pack. Returns a checkout URL for payment.
     */
    suspend fun creditPurchase(request: CreditPurchaseRequest): CreditPurchaseResponse {
        val (data, _) = http.doJson<CreditPurchaseResponse>("POST", "/qai/v1/credits/purchase", request)
        return data
    }

    /**
     * Get the current credit balance.
     */
    suspend fun creditBalance(): CreditBalanceResponse {
        val (data, _) = http.doJson<CreditBalanceResponse>("GET", "/qai/v1/credits/balance")
        return data
    }

    /**
     * List available credit tiers (no auth required).
     */
    suspend fun creditTiers(): CreditTiersResponse {
        val (data, _) = http.doJson<CreditTiersResponse>("GET", "/qai/v1/credits/tiers")
        return data
    }

    /**
     * Apply for the developer program.
     */
    suspend fun devProgramApply(request: DevProgramApplyRequest): DevProgramApplyResponse {
        val (data, _) = http.doJson<DevProgramApplyResponse>("POST", "/qai/v1/credits/dev-program", request)
        return data
    }

    // ── Search (Brave) ───────────────────────────────────────────────

    /** Perform a web search via Brave Search. */
    suspend fun webSearch(request: WebSearchRequest): WebSearchResponse {
        val (data, _) = http.doJson<WebSearchResponse>("POST", "/qai/v1/search/web", request)
        return data
    }

    /** Fetch LLM-optimized content chunks for grounding. */
    suspend fun searchContext(request: SearchContextRequest): SearchContextResponse {
        val (data, _) = http.doJson<SearchContextResponse>("POST", "/qai/v1/search/context", request)
        return data
    }

    /** Get a grounded AI answer with citations. */
    suspend fun searchAnswer(request: SearchAnswerRequest): SearchAnswerResponse {
        val (data, _) = http.doJson<SearchAnswerResponse>("POST", "/qai/v1/search/answer", request)
        return data
    }

    // ── Auth ─────────────────────────────────────────────────────────

    /**
     * Authenticate with Apple Sign-In.
     */
    suspend fun authApple(request: AuthAppleRequest): AuthResponse {
        val (data, _) = http.doJson<AuthResponse>("POST", "/qai/v1/auth/apple", request)
        return data
    }

    // ── Contact ──────────────────────────────────────────────────────

    /**
     * Send a contact form message (public endpoint, no auth required).
     */
    suspend fun contact(request: ContactRequest): StatusResponse {
        val (data, _) = http.doJson<StatusResponse>("POST", "/qai/v1/contact", request)
        return data
    }

    // ── Private helpers ──────────────────────────────────────────────

    private fun backfill(data: ImageResponse, meta: ResponseMeta): ImageResponse {
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }

    private fun backfillEdit(data: ImageEditResponse, meta: ResponseMeta): ImageEditResponse {
        return data.let {
            if (it.costTicks == 0L && meta.costTicks > 0) it.copy(costTicks = meta.costTicks) else it
        }.let {
            if (it.requestId.isEmpty()) it.copy(requestId = meta.requestId) else it
        }
    }
}
