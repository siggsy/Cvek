package xyz.siggsy.cvek.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

inline fun <reified T> T.toJson(): String = json.encodeToString<T>(this)
inline fun <reified T> String.decodeJson(): T = json.decodeFromString(this)