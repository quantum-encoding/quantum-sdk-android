package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Scrape ──────────────────────────────────────────────────────────

@Serializable
data class ScrapeTarget(
    val name: String = "",
    val url: String = "",
    val type: String? = null,
    val selector: String? = null,
    val content: String? = null,
    val notebook: String? = null,
    val recursive: Boolean? = null,
    @SerialName("max_pages") val maxPages: Int? = null,
    @SerialName("delay_ms") val delayMs: Int? = null,
    val ingest: String? = null,
)

@Serializable
data class ScrapeRequest(
    val targets: List<ScrapeTarget> = emptyList(),
)

@Serializable
data class ScrapeResponse(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
    val targets: Int = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── Screenshot ──────────────────────────────────────────────────────

@Serializable
data class ScreenshotURL(
    val url: String = "",
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("full_page") val fullPage: Boolean? = null,
    @SerialName("delay_ms") val delayMs: Int? = null,
)

@Serializable
data class ScreenshotRequest(
    val urls: List<ScreenshotURL> = emptyList(),
)

@Serializable
data class ScreenshotResult(
    val url: String = "",
    val base64: String = "",
    val format: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val error: String? = null,
)

@Serializable
data class ScreenshotResponse(
    val screenshots: List<ScreenshotResult> = emptyList(),
    val count: Int = 0,
)
