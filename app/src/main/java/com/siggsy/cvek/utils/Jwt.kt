package com.siggsy.cvek.utils

import android.util.Base64
import kotlinx.serialization.Serializable
import java.time.ZoneId

@Serializable
data class JWT(
    val consumerKey: String,
    val userId: String,
    val userType: String,
    val schoolId: String,
    val sessionId: String,
    val issuedAt: String,
    val ttl: Long
) {
    val isExpired: Boolean get() =
        issuedAt
            .toDate()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() + (ttl * 1000) <= System.currentTimeMillis()

}

fun String.decodeAccessJWT(): JWT? {
    return if (matches(Regex("[\\w-]+\\.[\\w-]+\\.[\\w-]+"))) {
        val payload = split('.')[1].let {
            Base64.decode(it, Base64.URL_SAFE).toString(charset("UTF-8"))
        }
        payload.decodeJson()
    } else {
        null
    }
}