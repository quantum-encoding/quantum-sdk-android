package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class RigRequest(
    @SerialName("input_task_id") val inputTaskId: String? = null,
    @SerialName("model_url") val modelUrl: String? = null,
    @SerialName("height_meters") val heightMeters: Double? = null,
    @SerialName("texture_image_url") val textureImageUrl: String? = null,
)

@Serializable
data class AnimateRequest(
    @SerialName("rig_task_id") val rigTaskId: String,
    @SerialName("action_id") val actionId: Int,
    @SerialName("post_process") val postProcess: PostProcessOptions? = null,
)

@Serializable
data class PostProcessOptions(
    @SerialName("operation_type") val operationType: String,
    val fps: Int? = null,
)

@kotlinx.serialization.Serializable
data class RetextureRequest(
    @kotlinx.serialization.SerialName("input_task_id") val inputTaskId: String? = null,
    @kotlinx.serialization.SerialName("model_url") val modelUrl: String? = null,
    @kotlinx.serialization.SerialName("text_style_prompt") val textStylePrompt: String? = null,
    @kotlinx.serialization.SerialName("image_style_url") val imageStyleUrl: String? = null,
    @kotlinx.serialization.SerialName("ai_model") val aiModel: String? = null,
    @kotlinx.serialization.SerialName("enable_original_uv") val enableOriginalUv: Boolean? = null,
    @kotlinx.serialization.SerialName("enable_pbr") val enablePbr: Boolean? = null,
    @kotlinx.serialization.SerialName("remove_lighting") val removeLighting: Boolean? = null,
    @kotlinx.serialization.SerialName("target_formats") val targetFormats: List<String>? = null,
)
