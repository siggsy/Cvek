package xyz.siggsy.cvek.utils.preferences

import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val authToken: String,
    val refreshToken: String,
)

class AuthPreferences(context: Context) : CvekPreferences(context, "AUTH") {
    var currentUserId: String by stringPreference("current_user")
    var users: Map<String, User> by serializablePreference("users") { mapOf() }
    val currentUser: User?
        get() = users[currentUserId]
}