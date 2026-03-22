package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User information returned after authentication.
 */
@Serializable
data class AuthUser(
    val id: String = "",
    val name: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

/**
 * Response from authentication endpoints.
 */
@Serializable
data class AuthResponse(
    val token: String = "",
    val user: AuthUser = AuthUser(),
)

/**
 * Request body for Apple Sign-In.
 *
 * @property idToken The Apple identity token (JWT from Sign in with Apple).
 * @property name Optional display name (only provided on first sign-in).
 */
@Serializable
data class AuthAppleRequest(
    @SerialName("id_token") val idToken: String,
    val name: String? = null,
)
