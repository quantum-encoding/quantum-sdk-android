package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A compute instance template describing available GPU configurations.
 */
@Serializable
data class ComputeTemplate(
    val id: String = "",
    val name: String? = null,
    val gpu: String? = null,
    @SerialName("gpu_count") val gpuCount: Int? = null,
    @SerialName("vram_gb") val vramGb: Int? = null,
    val vcpus: Int? = null,
    @SerialName("ram_gb") val ramGb: Int? = null,
    @SerialName("price_per_hour_usd") val pricePerHourUsd: Double? = null,
    val zones: List<String>? = null,
)

/**
 * Response from listing compute templates.
 */
@Serializable
data class TemplatesResponse(
    val templates: List<ComputeTemplate> = emptyList(),
)

/**
 * Request body for provisioning a GPU compute instance.
 */
@Serializable
data class ProvisionRequest(
    val template: String = "",
    val zone: String? = null,
    val spot: Boolean? = null,
    @SerialName("auto_teardown_minutes") val autoTeardownMinutes: Int? = null,
    @SerialName("ssh_public_key") val sshPublicKey: String? = null,
)

/**
 * Response from provisioning a compute instance.
 */
@Serializable
data class ProvisionResponse(
    @SerialName("instance_id") val instanceId: String = "",
    val status: String = "",
    val template: String? = null,
    val zone: String? = null,
    @SerialName("ssh_address") val sshAddress: String? = null,
    @SerialName("price_per_hour_usd") val pricePerHourUsd: Double? = null,
)

/**
 * A running compute instance.
 */
@Serializable
data class ComputeInstance(
    val id: String = "",
    val status: String = "",
    val template: String? = null,
    val zone: String? = null,
    @SerialName("ssh_address") val sshAddress: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("price_per_hour_usd") val pricePerHourUsd: Double? = null,
    @SerialName("auto_teardown_minutes") val autoTeardownMinutes: Int? = null,
)

/**
 * Response from listing compute instances.
 */
@Serializable
data class InstancesResponse(
    val instances: List<ComputeInstance> = emptyList(),
)

/**
 * Response from getting a single compute instance.
 */
@Serializable
data class InstanceResponse(
    val instance: ComputeInstance = ComputeInstance(),
)

/**
 * Response from deleting a compute instance.
 */
@Serializable
data class DeleteResponse(
    val status: String = "",
    @SerialName("instance_id") val instanceId: String? = null,
)

/**
 * Request body for adding an SSH key to an instance.
 */
@Serializable
data class SSHKeyRequest(
    @SerialName("ssh_public_key") val sshPublicKey: String = "",
)
