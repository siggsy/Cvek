package xyz.siggsy.cvek.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * File for JSON to Kotlin request object mapping
 */

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("supported_user_types") val supportedUserTypes: List<String>
)