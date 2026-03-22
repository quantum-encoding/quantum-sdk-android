package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── TTS ──────────────────────────────────────────────────────────────

/**
 * Request body for text-to-speech.
 *
 * @property text Text to speak.
 * @property model Model for TTS.
 * @property voice Voice ID.
 * @property format Output format (e.g. "mp3", "wav").
 * @property speed Speaking speed.
 */
@Serializable
data class TTSRequest(
    val text: String,
    val model: String? = null,
    val voice: String? = null,
    val format: String? = null,
    val speed: Double? = null,
)

/**
 * Response from text-to-speech.
 */
@Serializable
data class TTSResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    val format: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── STT ──────────────────────────────────────────────────────────────

/**
 * Request body for speech-to-text.
 *
 * @property audio Base64-encoded audio data.
 * @property model Model for STT.
 * @property format Audio format (e.g. "wav", "mp3").
 * @property language BCP-47 language code.
 */
@Serializable
data class STTRequest(
    val audio: String,
    val model: String? = null,
    val format: String? = null,
    val language: String? = null,
)

/**
 * Response from speech-to-text.
 */
@Serializable
data class STTResponse(
    val text: String = "",
    val language: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Music ────────────────────────────────────────────────────────────

/**
 * Request body for music generation.
 *
 * @property prompt Text prompt describing the music.
 * @property duration Duration in seconds.
 * @property model Model to use for music generation.
 */
@Serializable
data class MusicRequest(
    val prompt: String,
    val duration: Int? = null,
    val model: String? = null,
)

/**
 * A generated music clip.
 */
@Serializable
data class MusicClip(
    @SerialName("audio_url") val audioUrl: String = "",
    val title: String? = null,
    val tags: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
)

/**
 * Response from music generation.
 */
@Serializable
data class MusicResponse(
    val clips: List<MusicClip> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Sound Effects ────────────────────────────────────────────────────

/**
 * Request body for sound effect generation (ElevenLabs).
 */
@Serializable
data class SoundEffectRequest(
    val text: String,
    @SerialName("duration_seconds") val durationSeconds: Double? = null,
    @SerialName("prompt_influence") val promptInfluence: Double? = null,
)

/**
 * Response from sound effect generation.
 */
@Serializable
data class SoundEffectResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Dialogue ─────────────────────────────────────────────────────────

/**
 * Request body for multi-speaker dialogue generation (ElevenLabs).
 *
 * @property script Script with speaker names and lines.
 * @property voices Voice mapping (speaker name to voice ID).
 * @property model Model for dialogue generation.
 */
@Serializable
data class DialogueRequest(
    val script: String,
    val voices: Map<String, String>,
    val model: String? = null,
)

/**
 * Response from dialogue generation.
 */
@Serializable
data class DialogueResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Speech-to-Speech ─────────────────────────────────────────────────

/**
 * Request body for voice conversion (ElevenLabs).
 *
 * @property audio Base64-encoded source audio.
 * @property voiceId Target voice ID.
 * @property model Model for voice conversion.
 */
@Serializable
data class SpeechToSpeechRequest(
    val audio: String,
    @SerialName("voice_id") val voiceId: String,
    val model: String? = null,
)

/**
 * Response from voice conversion.
 */
@Serializable
data class SpeechToSpeechResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Voice Isolation ──────────────────────────────────────────────────

/**
 * Request body for voice isolation (ElevenLabs).
 */
@Serializable
data class IsolateVoiceRequest(
    val audio: String,
)

/**
 * Response from voice isolation.
 */
@Serializable
data class IsolateVoiceResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Voice Remix ──────────────────────────────────────────────────────

/**
 * Request body for voice remix (ElevenLabs).
 */
@Serializable
data class RemixVoiceRequest(
    val audio: String,
    @SerialName("voice_id") val voiceId: String,
)

/**
 * Response from voice remix.
 */
@Serializable
data class RemixVoiceResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Dubbing ──────────────────────────────────────────────────────────

/**
 * Request body for dubbing (ElevenLabs).
 */
@Serializable
data class DubRequest(
    val audio: String,
    @SerialName("target_lang") val targetLang: String,
    @SerialName("source_lang") val sourceLang: String? = null,
)

/**
 * Response from dubbing.
 */
@Serializable
data class DubResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Alignment ────────────────────────────────────────────────────────

/**
 * Request body for audio-text alignment (ElevenLabs).
 */
@Serializable
data class AlignRequest(
    val audio: String,
    val text: String,
)

/**
 * A word with timing information from alignment.
 */
@Serializable
data class AlignedWord(
    val word: String = "",
    val start: Double = 0.0,
    val end: Double = 0.0,
)

/**
 * Response from audio-text alignment.
 */
@Serializable
data class AlignResponse(
    val words: List<AlignedWord> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Voice Design ─────────────────────────────────────────────────────

/**
 * Request body for voice design (ElevenLabs).
 *
 * @property description Text description of the desired voice.
 * @property previewText Text to preview the voice with.
 */
@Serializable
data class VoiceDesignRequest(
    val description: String,
    @SerialName("preview_text") val previewText: String? = null,
)

/**
 * A voice preview from voice design.
 */
@Serializable
data class VoicePreview(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("voice_id") val voiceId: String = "",
)

/**
 * Response from voice design.
 */
@Serializable
data class VoiceDesignResponse(
    val previews: List<VoicePreview> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Starfish TTS ─────────────────────────────────────────────────────

/**
 * Request body for HeyGen Starfish TTS.
 *
 * @property text Text to speak.
 * @property referenceAudio Base64-encoded reference audio for voice cloning.
 */
@Serializable
data class StarfishTTSRequest(
    val text: String,
    @SerialName("reference_audio") val referenceAudio: String? = null,
)

/**
 * Response from Starfish TTS.
 */
@Serializable
data class StarfishTTSResponse(
    @SerialName("audio_url") val audioUrl: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── Advanced Music + Finetunes ───────────────────────────────────────

/**
 * Request body for advanced music generation (ElevenLabs Eleven Music).
 */
@Serializable
data class MusicAdvancedRequest(
    val prompt: String,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    val model: String? = null,
    @SerialName("finetune_id") val finetuneId: String? = null,
)

/**
 * A clip from advanced music generation (base64-encoded).
 */
@Serializable
data class MusicAdvancedClip(
    val base64: String = "",
    val format: String = "",
    val size: Long = 0,
)

/**
 * Response from advanced music generation.
 */
@Serializable
data class MusicAdvancedResponse(
    val clips: List<MusicAdvancedClip> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Information about a music finetune.
 */
@Serializable
data class MusicFinetuneInfo(
    @SerialName("finetune_id") val finetuneId: String = "",
    val name: String = "",
    val description: String? = null,
    val status: String = "",
    @SerialName("model_id") val modelId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * Response from listing music finetunes.
 */
@Serializable
data class MusicFinetuneListResponse(
    val finetunes: List<MusicFinetuneInfo> = emptyList(),
)

/**
 * Request body for creating a music finetune.
 */
@Serializable
data class MusicFinetuneCreateRequest(
    val name: String,
    val description: String? = null,
    val samples: List<String>,
)
