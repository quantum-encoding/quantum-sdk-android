package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── TTS ──────────────────────────────────────────────────────────────

/**
 * Request body for text-to-speech.
 */
@Serializable
data class TtsRequest(
    val model: String = "",
    val text: String = "",
    val voice: String? = null,
    @SerialName("format") val outputFormat: String? = null,
    val speed: Double? = null,
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
