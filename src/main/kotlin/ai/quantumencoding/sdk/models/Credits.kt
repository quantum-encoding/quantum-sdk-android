package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A credit pack available for purchase.
 */
@Serializable
data class CreditPack(
    val id: String = "",
    val name: String? = null,
    @SerialName("price_usd") val priceUsd: Double = 0.0,
    @SerialName("credit_ticks") val creditTicks: Long = 0,
    val description: String? = null,
)

/**
 * Response from listing credit packs.
 */
@Serializable
data class CreditPacksResponse(
    val packs: List<CreditPack> = emptyList(),
)

/**
 * Request body for purchasing a credit pack.
 *
 * @property packId Credit pack ID to purchase.
 * @property successUrl Redirect URL on successful payment.
 * @property cancelUrl Redirect URL on cancelled payment.
 */
@Serializable
data class CreditPurchaseRequest(
    @SerialName("pack_id") val packId: String,
    @SerialName("success_url") val successUrl: String? = null,
    @SerialName("cancel_url") val cancelUrl: String? = null,
)

/**
 * Response from purchasing a credit pack.
 */
@Serializable
data class CreditPurchaseResponse(
    @SerialName("checkout_url") val checkoutUrl: String = "",
)

/**
 * Response from checking credit balance.
 */
@Serializable
data class CreditBalanceResponse(
    @SerialName("balance_ticks") val balanceTicks: Long = 0,
    @SerialName("balance_usd") val balanceUsd: Double = 0.0,
)

/**
 * A credit tier.
 */
@Serializable
data class CreditTier(
    val name: String? = null,
    @SerialName("min_balance") val minBalance: Long? = null,
    @SerialName("discount_percent") val discountPercent: Double? = null,
)

/**
 * Response from listing credit tiers.
 */
@Serializable
data class CreditTiersResponse(
    val tiers: List<CreditTier> = emptyList(),
)

/**
 * Request body for applying to the developer program.
 */
@Serializable
data class DevProgramApplyRequest(
    @SerialName("use_case") val useCase: String,
    val company: String? = null,
    @SerialName("expected_usd") val expectedUsd: Double? = null,
    val website: String? = null,
)

/**
 * Response from developer program application.
 */
@Serializable
data class DevProgramApplyResponse(
    val status: String = "",
)
