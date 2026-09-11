package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── TTS ──────────────────────────────────────────────────────────────

/**
 * ElevenLabs voice tuning. Ignored by every other provider.
 *
 * Every field is nullable because an absent knob leaves the provider default
 * alone, which is not the same as sending 0: 0.0 stability is a real setting
 * the provider honours, so a zeroed object silently retunes the voice.
 */
@Serializable
data class TtsVoiceSettings(
    /** 0.0-1.0. Lower is more expressive and less consistent. */
    val stability: Double? = null,
    /** 0.0-1.0. How closely to track the original voice. */
    @SerialName("similarity_boost") val similarityBoost: Double? = null,
    /** 0.0-1.0 style exaggeration. */
    val style: Double? = null,
    /** Boost resemblance to the original speaker. */
    @SerialName("use_speaker_boost") val useSpeakerBoost: Boolean? = null,
)

/**
 * One voice in a Gemini two-speaker dialogue.
 */
@Serializable
data class TtsSpeaker(
    /**
     * The label this speaker's lines carry in the text, e.g. "Lacey" for
     * lines written as `Lacey: …`.
     */
    val name: String = "",
    /** The prebuilt voice that reads those lines, e.g. "Laomedeia". */
    val voice: String = "",
)

/**
 * Request body for text-to-speech.
 *
 * Only [text] is required. Leaving [model] empty gets the gateway's house
 * voice: `gemini-3.1-flash-tts-preview` with the `Laomedeia` voice — the
 * field is omitted from the body rather than sent as `""`.
 *
 * Most of the steering is prose, not parameters — see [instructions] and the
 * inline audio tags described in the README under "Steering a Gemini voice".
 */
@Serializable
data class TtsRequest(
    /**
     * TTS model. Empty = the gateway default,
     * `gemini-3.1-flash-tts-preview`, paired with the `Laomedeia` voice. Also
     * `gemini-2.5-flash-preview-tts`, `gemini-2.5-pro-preview-tts`, OpenAI
     * `openai-tts-1` / `gpt-4o-mini-tts`, xAI `grok-tts`, ElevenLabs
     * `eleven_*`.
     */
    val model: String = "",
    /**
     * What to say. May carry inline audio tags ("[whispers]", "[excited]", …)
     * and, for dialogue, the speaker labels named in [speakers].
     */
    val text: String = "",
    /**
     * A voice id from `listVoices()`. Gemini's default is `Laomedeia`.
     * Ignored when [speakers] is set.
     */
    val voice: String? = null,
    /** Audio format: "mp3" (default), "wav", "opus", "pcm". */
    @SerialName("format") val outputFormat: String? = null,
    /**
     * Speech rate, 0.7-1.5. xAI only — on Gemini, ask for it in
     * [instructions] ("at a slow, measured pace").
     */
    val speed: Double? = null,
    /**
     * Style direction: tone, pace, accent, character. On Gemini this is
     * prepended to the prompt and is the main way to steer a read, since
     * Gemini exposes no knobs for any of it. On OpenAI only gpt-4o-mini-tts
     * honours it — tts-1/tts-1-hd reject the field and the gateway drops it.
     */
    val instructions: String? = null,
    /**
     * BCP-47 tag, e.g. "en-GB", "es-ES", or "auto". Gemini detects the
     * language on its own; set this to pin the pronunciation or accent
     * family. Also drives xAI pronunciation, where an English default sounds
     * robotic on other languages.
     */
    val language: String? = null,
    /** Output sample rate in Hz, e.g. 24000 or 44100. xAI only. */
    @SerialName("sample_rate") val sampleRate: Int? = null,
    /** Output bit rate in bits/sec, e.g. 128000. xAI only. */
    @SerialName("bit_rate") val bitRate: Int? = null,
    /** ElevenLabs synthesis tuning. Ignored by every other provider. */
    @SerialName("voice_settings") val voiceSettings: TtsVoiceSettings? = null,
    /**
     * Two-voice dialogue on Gemini TTS. Each entry pairs a speaker label used
     * in [text] ("Lacey: …") with the prebuilt voice that reads it. EXACTLY
     * TWO — the gateway rejects any other count with a 400 — and [voice] is
     * then ignored.
     */
    val speakers: List<TtsSpeaker>? = null,
)

/**
 * Response from text-to-speech.
 */
@Serializable
data class TtsResponse(
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── STT ──────────────────────────────────────────────────────────────

/**
 * Request body for speech-to-text.
 */
@Serializable
data class SttRequest(
    val model: String = "",
    @SerialName("audio_base64") val audioBase64: String = "",
    val filename: String? = null,
    val language: String? = null,
)

/**
 * Response from speech-to-text.
 */
@Serializable
data class SttResponse(
    val text: String = "",
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── Music ────────────────────────────────────────────────────────────

/**
 * Request body for music generation.
 */
@Serializable
data class MusicRequest(
    val model: String = "",
    val prompt: String = "",
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
)

/**
 * A generated music clip.
 */
@Serializable
data class MusicClip(
    val base64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val index: Int = 0,
)

/**
 * Response from music generation.
 */
@Serializable
data class MusicResponse(
    @SerialName("audio_clips") val audioClips: List<MusicClip> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── Sound Effects ────────────────────────────────────────────────────

/**
 * Request body for sound effect generation (ElevenLabs).
 */
@Serializable
data class SoundEffectRequest(
    val prompt: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double? = null,
)

/**
 * Response from sound effect generation.
 */
@Serializable
data class SoundEffectResponse(
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── AudioResponse (generic) ─────────────────────────────────────────

/**
 * Generic audio response used by multiple advanced audio endpoints.
 */
@Serializable
data class AudioResponse(
    @SerialName("audio_base64") val audioBase64: String? = null,
    val format: String? = null,
    @SerialName("size_bytes") val sizeBytes: Long? = null,
    val model: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── Dialogue ─────────────────────────────────────────────────────────

/**
 * A single dialogue turn.
 */
@Serializable
data class DialogueTurn(
    val speaker: String = "",
    val text: String = "",
    val voice: String? = null,
)

/**
 * Voice mapping for dialogue.
 */
@Serializable
data class DialogueVoice(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
)

/**
 * Request body for multi-speaker dialogue generation.
 */
@Serializable
data class DialogueRequest(
    val text: String = "",
    val voices: List<DialogueVoice> = emptyList(),
    val model: String? = null,
    @SerialName("output_format") val outputFormat: String? = null,
    val seed: Int? = null,
)

// ── Speech-to-Speech ─────────────────────────────────────────────────

/**
 * Request body for speech-to-speech conversion.
 */
@Serializable
data class SpeechToSpeechRequest(
    val model: String? = null,
    @SerialName("audio_base64") val audioBase64: String = "",
    val voice: String? = null,
    @SerialName("format") val outputFormat: String? = null,
)

// ── Voice Isolation ──────────────────────────────────────────────────

/**
 * Request body for voice isolation.
 */
@Serializable
data class IsolateRequest(
    @SerialName("audio_base64") val audioBase64: String = "",
    @SerialName("format") val outputFormat: String? = null,
)

// ── Voice Remix ──────────────────────────────────────────────────────

/**
 * Request body for voice remixing.
 */
@Serializable
data class RemixRequest(
    @SerialName("audio_base64") val audioBase64: String = "",
    val voice: String? = null,
    val model: String? = null,
    @SerialName("format") val outputFormat: String? = null,
)

// ── Dubbing ──────────────────────────────────────────────────────────

/**
 * Request body for audio dubbing.
 */
@Serializable
data class DubRequest(
    @SerialName("audio_base64") val audioBase64: String = "",
    val filename: String? = null,
    @SerialName("target_language") val targetLanguage: String = "",
    @SerialName("source_language") val sourceLanguage: String? = null,
)

// ── Alignment ────────────────────────────────────────────────────────

/**
 * Request body for audio-text alignment.
 */
@Serializable
data class AlignRequest(
    @SerialName("audio_base64") val audioBase64: String = "",
    val text: String = "",
    val language: String? = null,
)

/**
 * A single alignment segment.
 */
@Serializable
data class AlignmentSegment(
    val text: String = "",
    val start: Double = 0.0,
    val end: Double = 0.0,
)

/**
 * Response from audio alignment.
 */
@Serializable
data class AlignResponse(
    val segments: List<AlignmentSegment> = emptyList(),
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── Voice Design ─────────────────────────────────────────────────────

/**
 * Request body for voice design (generating a voice from a description).
 */
@Serializable
data class VoiceDesignRequest(
    @SerialName("voice_description") val description: String = "",
    @SerialName("sample_text") val text: String = "",
    @SerialName("format") val outputFormat: String? = null,
)

// ── Starfish TTS ─────────────────────────────────────────────────────

/**
 * Request body for Starfish TTS (HeyGen).
 */
@Serializable
data class StarfishTTSRequest(
    val text: String = "",
    val voice: String? = null,
    @SerialName("format") val outputFormat: String? = null,
    val speed: Double? = null,
)

// ── Advanced Music + Finetunes ───────────────────────────────────────

/**
 * A section within an Eleven Music generation request.
 */
@Serializable
data class MusicSection(
    @SerialName("section_type") val sectionType: String = "",
    val lyrics: String? = null,
    val style: String? = null,
    @SerialName("style_exclude") val styleExclude: String? = null,
)

/**
 * Request body for advanced music generation (ElevenLabs Eleven Music).
 */
@Serializable
data class ElevenMusicRequest(
    val model: String = "",
    val prompt: String = "",
    val sections: List<MusicSection>? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    val language: String? = null,
    val vocals: Boolean? = null,
    val style: String? = null,
    @SerialName("style_exclude") val styleExclude: String? = null,
    @SerialName("finetune_id") val finetuneId: String? = null,
    @SerialName("edit_reference_id") val editReferenceId: String? = null,
    @SerialName("edit_instruction") val editInstruction: String? = null,
)

/**
 * A single music clip from advanced generation.
 */
@Serializable
data class ElevenMusicClip(
    val base64: String = "",
    val format: String = "",
    val size: Long = 0,
)

/**
 * Response from advanced music generation.
 */
@Serializable
data class ElevenMusicResponse(
    val clips: List<ElevenMusicClip> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Info about a music finetune.
 */
@Serializable
data class FinetuneInfo(
    @SerialName("finetune_id") val finetuneId: String = "",
    val name: String = "",
    val status: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * Response from listing finetunes.
 */
@Serializable
data class ListFinetunesResponse(
    val finetunes: List<FinetuneInfo> = emptyList(),
)

// ── Cross-SDK parity types ──────────────────────────────────────────

typealias TextToSpeechRequest = TtsRequest
typealias TextToSpeechResponse = TtsResponse
typealias SpeechToTextRequest = SttRequest
typealias SpeechToTextResponse = SttResponse
typealias IsolateVoiceRequest = IsolateRequest
typealias RemixVoiceRequest = RemixRequest

@Serializable
data class AlignedWord(
    val text: String = "",
    @SerialName("start_time") val startTime: Double = 0.0,
    @SerialName("end_time") val endTime: Double = 0.0,
    val confidence: Double = 0.0,
)

@Serializable
data class DialogueResponse(
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class SpeechToSpeechResponse(
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class IsolateVoiceResponse(
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class RemixVoiceResponse(
    @SerialName("audio_base64") val audioBase64: String? = null,
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    @SerialName("voice_id") val voiceId: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class DubResponse(
    @SerialName("dubbing_id") val dubbingId: String = "",
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
    @SerialName("target_lang") val targetLang: String = "",
    val status: String = "",
    @SerialName("processing_time_seconds") val processingTimeSeconds: Double = 0.0,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class VoiceDesignResponse(
    val previews: List<VoicePreview> = emptyList(),
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class VoicePreview(
    @SerialName("generated_voice_id") val generatedVoiceId: String = "",
    @SerialName("audio_base64") val audioBase64: String = "",
    val format: String = "",
)

@Serializable
data class StarfishTTSResponse(
    @SerialName("audio_base64") val audioBase64: String? = null,
    val url: String? = null,
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val duration: Double = 0.0,
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class MusicAdvancedRequest(
    val prompt: String = "",
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    val model: String? = null,
    @SerialName("finetune_id") val finetuneId: String? = null,
)

@Serializable
data class MusicAdvancedClip(
    val base64: String = "",
    val format: String = "",
    val size: Long = 0,
)

@Serializable
data class MusicAdvancedResponse(
    val clips: List<MusicAdvancedClip> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

@Serializable
data class MusicFinetuneInfo(
    @SerialName("finetune_id") val finetuneId: String = "",
    val name: String = "",
    val description: String? = null,
    val status: String = "",
    @SerialName("model_id") val modelId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class MusicFinetuneListResponse(
    val finetunes: List<MusicFinetuneInfo> = emptyList(),
)

@Serializable
data class MusicFinetuneCreateRequest(
    val name: String = "",
    val description: String? = null,
    val samples: List<String> = emptyList(),
)

// ── HeyGen Sounds Search (background music + sound effects) ─────────

/**
 * Query parameters for searching the sounds catalog.
 */
data class AudioSoundsQuery(
    /** Natural-language description of the sound wanted (required). */
    val query: String,
    /**
     * Catalog to search: "music" | "sound_effects" (API default: "music").
     * Wire param: `type`.
     */
    val soundType: String? = null,
    /** Max results, 1–50 (API default 10). */
    val limit: Int? = null,
    /** Minimum similarity score, 0–1 (API default 0.7). */
    val minScore: Double? = null,
    /** Opaque cursor from a previous response's `nextToken`. */
    val token: String? = null,
)

/**
 * A track from the sounds catalog.
 */
@Serializable
data class AudioSound(
    /** Track identifier. */
    val id: String = "",
    /** Track name. */
    val name: String = "",
    /** Track description. */
    val description: String = "",
    /**
     * Pre-signed WAV URL with a limited lifetime — download promptly,
     * do not cache.
     */
    @SerialName("audio_url") val audioUrl: String = "",
    /** Duration in seconds. */
    val duration: Double = 0.0,
    /** Similarity score 0–1 (best first). */
    val score: Double = 0.0,
    /** "music" | "sound_effects". Wire field: `type`. */
    @SerialName("type") val soundType: String = "",
)

/**
 * Response from searching the sounds catalog (unbilled).
 */
@Serializable
data class AudioSoundsResponse(
    /** Matching tracks, best score first (empty page → `[]`). */
    val sounds: List<AudioSound> = emptyList(),
    /** More pages exist. */
    @SerialName("has_more") val hasMore: Boolean = false,
    /** Pass as `token` for the next page (may be empty). */
    @SerialName("next_token") val nextToken: String = "",
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)
