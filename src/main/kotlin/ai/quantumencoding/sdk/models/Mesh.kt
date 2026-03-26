package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request for a 3D remesh operation.
 */
@Serializable
data class RemeshRequest(
    @SerialName("input_task_id") val inputTaskId: String? = null,
    @SerialName("model_url") val modelUrl: String? = null,
    @SerialName("target_formats") val targetFormats: List<String>? = null,
    val topology: String? = null,
    @SerialName("target_polycount") val targetPolycount: Int? = null,
    @SerialName("resize_height") val resizeHeight: Double? = null,
    @SerialName("origin_at") val originAt: String? = null,
    @SerialName("convert_format_only") val convertFormatOnly: Boolean? = null,
)

/**
 * URLs for each exported format in a remesh result.
 */
@Serializable
data class ModelUrls(
    val glb: String = "",
    val fbx: String = "",
    val obj: String = "",
    val usdz: String = "",
    val stl: String = "",
    val blend: String = "",
)

/**
 * Request for AI retexturing of an existing 3D model.
 */
@Serializable
data class RetextureRequest(
    @SerialName("input_task_id") val inputTaskId: String? = null,
    @SerialName("model_url") val modelUrl: String? = null,
    val prompt: String = "",
    @SerialName("enable_pbr") val enablePbr: Boolean? = null,
    @SerialName("ai_model") val aiModel: String? = null,
)

/**
 * Request for auto-rigging a humanoid 3D model.
 */
@Serializable
data class RigRequest(
    @SerialName("input_task_id") val inputTaskId: String? = null,
    @SerialName("model_url") val modelUrl: String? = null,
    @SerialName("height_meters") val heightMeters: Double? = null,
)

/**
 * Request for applying an animation to a rigged character.
 */
@Serializable
data class AnimateRequest(
    @SerialName("rig_task_id") val rigTaskId: String = "",
    @SerialName("action_id") val actionId: Int = 0,
    @SerialName("post_process") val postProcess: AnimationPostProcess? = null,
)

/**
 * Post-processing options for animation export.
 */
@Serializable
data class AnimationPostProcess(
    @SerialName("operation_type") val operationType: String = "",
    val fps: Int? = null,
)

/**
 * Backwards-compatible alias for [AnimationPostProcess].
 */
typealias PostProcess = AnimationPostProcess

/**
 * Request for 3D model generation (alias for [ImageRequest]).
 */
typealias Generate3DRequest = ImageRequest

/**
 * URLs for basic pre-built animations from a rigging result.
 */
@Serializable
data class BasicAnimations(
    @SerialName("walking_glb") val walkingGlb: String = "",
    @SerialName("walking_fbx") val walkingFbx: String = "",
    @SerialName("running_glb") val runningGlb: String = "",
    @SerialName("running_fbx") val runningFbx: String = "",
    @SerialName("idle_glb") val idleGlb: String = "",
    @SerialName("idle_fbx") val idleFbx: String = "",
)
