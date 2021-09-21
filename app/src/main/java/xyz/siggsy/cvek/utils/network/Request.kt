package xyz.siggsy.cvek.utils.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.siggsy.cvek.utils.toJson

/**
 * Already defined application/json content type for use on all requests
 */
val jsonMedia = "application/json".toMediaType()

/**
 * Apply default headers to the request required by the API
 * @param authRequest boolean to indicate if request is for authentication purposes
 */
fun Request.Builder.defaultHeaders(authRequest: Boolean = false) = apply {
    val (platform, version) = if (authRequest) {
        Pair("android","99999")
    } else {
        Pair("web", "13")
    }

    header("user-agent", "Mozilla/5.0")
    header("accept", "application/json, text/html")
    header("accept-language", "sl-SI,sl;q=0.9,en-GB;q=0.8,en;q=0.7,de;q=0.6")
    header("x-client-platform", platform)
    header("x-client-version", version)
    header("x-app-name", "family")
}

/**
 * Authentication headers required by auth blocked API requests
 * @param child value in 'x-child-id' header
 * @param token value in 'authorization' header. Bearer is prepended
 */
fun Request.Builder.authHeaders(child: String, token: String) = apply {
    header("x-child-id", child)
    header("authorization", "Bearer $token")
}

/**
 * Function for creating Request from url string
 * @param build block for additional builder modification
 */
fun String.toRequest(build: Request.Builder.() -> Unit = { }) =
    Request.Builder()
        .url(this)
        .apply(build)
        .build()

/**
 * Encode params to url string
 * @param params pair of key value to encode in url; eg. ?key1=value1&key2=value2
 */
fun String.addParams(vararg params: Pair<String, String>) =
    "$this?${params.joinToString(separator = "&") { (key, value) -> "$key=$value" }}"

/**
 * Extension function for the builder for creating post requests with json
 * request body
 * @param body json serializable object to be parsed to RequestBody.
 */
inline fun <reified T> Request.Builder.post(body: T) = apply {
    post(body.toJson().toRequestBody(jsonMedia))
}