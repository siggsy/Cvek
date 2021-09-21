package xyz.siggsy.cvek.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Function for encoding serializable object to string
 * @receiver - object to parse
 * @return - parsed object as string
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> T.toJson(): String = json.encodeToString(this)

/**
 * Function for decoding serialized object from string
 * @receiver - string to decode
 * @return - decoded object as T
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> String.decodeJson(): T = json.decodeFromString(this)