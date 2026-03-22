package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A GPU compute template with pricing.
 */
@Serializable
data class ComputeTemplate(
    val id: String = "",
    val name: String = "",
    @SerialName("gpu_type") val gpuType: String = "",
    @SerialName("gpu_count") val gpuCount: Int = 0,
    val vcpus: Int = 0,
    @SerialName("ram_gb") val ramGb: Int = 0,
    @SerialName("disk_gb") val diskGb: Int = 0,
    @SerialName("price_per_hour") val pricePerHour: Double = 0.0,
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
 *
 * @property templateId Template to provision.
 * @property region Cloud region.
 * @property sshPublicKey SSH public key for access.
 */
@Serializable
data class ProvisionRequest(
    @SerialName("template_id") val templateId: String,
    val region: String? = null,
    @SerialName("ssh_public_key") val sshPublicKey: String? = null,
)

/**
 * Response from provisioning a compute instance.
 */
@Serializable
data class ProvisionResponse(
    @SerialName("instance_id") val instanceId: String = "",
    val status: String = "",
)

/**
 * A compute instance in the instances list.
 */
@Serializable
data class ComputeInstanceInfo(
    val id: String = "",
    val status: String = "",
    @SerialName("template_id") val templateId: String = "",
    @SerialName("created_at") val createdAt: String = "",
)

/**
 * Response from listing compute instances.
 */
@Serializable
data class InstancesResponse(
    val instances: List<ComputeInstanceInfo> = emptyList(),
)

/**
 * Detailed information about a compute instance.
 */
@Serializable
data class InstanceDetailInfo(
    val id: String = "",
    val status: String = "",
    @SerialName("template_id") val templateId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("ssh_host") val sshHost: String? = null,
    @SerialName("ssh_port") val sshPort: Int? = null,
)

/**
 * Response from getting a single compute instance.
 */
@Serializable
data class InstanceResponse(
    val instance: InstanceDetailInfo = InstanceDetailInfo(),
)

/**
 * Request body for injecting an SSH key into a running instance.
 */
@Serializable
data class SSHKeyRequest(
    @SerialName("ssh_public_key") val sshPublicKey: String,
)

/**
 * Response from deleting a compute instance.
 */
@Serializable
data class DeleteResponse(
    val status: String = "",
    @SerialName("cost_ticks") val costTicks: Long? = null,
)
