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

/**
 * Detailed compute instance info with GPU, cost, and uptime details.
 */
@Serializable
data class ComputeInstanceInfo(
    @SerialName("instance_id") val instanceId: String = "",
    val template: String = "",
    val status: String = "",
    @SerialName("gcp_status") val gcpStatus: String? = null,
    val zone: String = "",
    @SerialName("machine_type") val machineType: String? = null,
    @SerialName("external_ip") val externalIp: String? = null,
    @SerialName("gpu_type") val gpuType: String? = null,
    @SerialName("gpu_count") val gpuCount: Int? = null,
    val spot: Boolean = false,
    @SerialName("hourly_usd") val hourlyUsd: Double = 0.0,
    @SerialName("cost_usd") val costUsd: Double = 0.0,
    @SerialName("uptime_minutes") val uptimeMinutes: Int = 0,
    @SerialName("auto_teardown_minutes") val autoTeardownMinutes: Int = 0,
    @SerialName("ssh_username") val sshUsername: String? = null,
    @SerialName("last_active_at") val lastActiveAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("terminated_at") val terminatedAt: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
)
