package com.siggsy.cvek.utils.preferences

import android.content.Context
import com.siggsy.cvek.data.easistent.UserDoesNotExist
import com.siggsy.cvek.utils.CvekPreferences
import com.siggsy.cvek.utils.serializablePreference
import com.siggsy.cvek.utils.stringPreference
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val authToken: String,
    val refreshToken: String,
)

class AuthPreferences(context: Context) : CvekPreferences(context, "AUTH") {
    var currentUserId: String by stringPreference("current_user")
    var users: Map<String, User> by serializablePreference("users") { mapOf() }
    var currentUser: User
        get() = users[currentUserId] ?: throw UserDoesNotExist()
        set(value) {
            users = users.toMutableMap().apply { this[currentUserId] = value }
        }
}