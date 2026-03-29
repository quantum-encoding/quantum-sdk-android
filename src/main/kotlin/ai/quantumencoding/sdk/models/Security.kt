package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Security Requests ────────────────────────────────────────────────

/**
 * Request body for scanning a URL for prompt injection.
 */
@Serializable
data class SecurityScanUrlRequest(
    val url: String,
)

/**
 * Request body for scanning raw HTML content.
 */
@Serializable
data class SecurityScanHtmlRequest(
    val html: String,
    @SerialName("visible_text") val visibleText: String? = null,
    val url: String? = null,
)

/**
 * Request body for reporting a suspicious URL.
 */
@Serializable
data class SecurityReportRequest(
    val url: String,
    val description: String? = null,
    val category: String? = null,
)

// ── Security Responses ───────────────────────────────────────────────

/**
 * Response from a security scan.
 */
@Serializable
data class SecurityScanResponse(
    val assessment: SecurityAssessment,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Threat assessment for a scanned page.
 */
@Serializable
data class SecurityAssessment(
    val url: String = "",
    @SerialName("threat_level") val threatLevel: String = "",
    @SerialName("threat_score") val threatScore: Double = 0.0,
    val findings: List<SecurityFinding> = emptyList(),
    @SerialName("hidden_text_length") val hiddenTextLength: Int = 0,
    @SerialName("visible_text_length") val visibleTextLength: Int = 0,
    @SerialName("hidden_ratio") val hiddenRatio: Double = 0.0,
    val summary: String = "",
)

/**
 * A single detected injection pattern.
 */
@Serializable
data class SecurityFinding(
    val category: String = "",
    val pattern: String = "",
    val content: String = "",
    val location: String = "",
    val threat: String = "",
    val confidence: Double = 0.0,
    val description: String = "",
)

/**
 * Response from checking a URL against the registry.
 */
@Serializable
data class SecurityCheckResponse(
    val url: String = "",
    val blocked: Boolean = false,
    @SerialName("threat_level") val threatLevel: String? = null,
    @SerialName("threat_score") val threatScore: Double? = null,
    val categories: List<String>? = null,
    @SerialName("first_seen") val firstSeen: String? = null,
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("report_count") val reportCount: Int? = null,
    val status: String? = null,
    val message: String? = null,
)

/**
 * Response from the blocklist feed.
 */
@Serializable
data class SecurityBlocklistResponse(
    val entries: List<SecurityBlocklistEntry> = emptyList(),
    val count: Int = 0,
    val status: String = "",
)

/**
 * A single blocklist entry.
 */
@Serializable
data class SecurityBlocklistEntry(
    val id: String? = null,
    val url: String = "",
    val status: String = "",
    @SerialName("threat_level") val threatLevel: String = "",
    @SerialName("threat_score") val threatScore: Double = 0.0,
    val categories: List<String> = emptyList(),
    @SerialName("findings_count") val findingsCount: Int = 0,
    @SerialName("hidden_ratio") val hiddenRatio: Double = 0.0,
    @SerialName("first_seen") val firstSeen: String? = null,
    val summary: String = "",
)

/**
 * Response from reporting a URL.
 */
@Serializable
data class SecurityReportResponse(
    val url: String = "",
    val status: String = "",
    val message: String = "",
    @SerialName("threat_level") val threatLevel: String? = null,
)
