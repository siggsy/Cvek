package com.siggsy.cvek.utils.network

import com.siggsy.cvek.utils.toJson
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

val jsonMedia = "application/json".toMediaType()

fun Request.Builder.defaultHeaders(authRequest: Boolean = false) = apply {
    // Login requests require android platform
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

fun Request.Builder.authHeaders(child: String, token: String) = apply {
    header("x-child-id", child)
    header("authorization", "Bearer $token")
}

fun String.toHttpUrl(vararg params: Pair<String, String>): HttpUrl {
    val builder = toHttpUrl().newBuilder()
    params.forEach { (key, value) ->
        builder.addQueryParameter(key, value)
    }
    return builder.build()
}

fun String.toRequest(build: Request.Builder.() -> Unit = { }) =
    Request.Builder()
        .url(this)
        .apply(build)
        .build()

fun String.addParams(vararg params: Pair<String, String>) =
    "$this?${params.joinToString(separator = "&") { (key, value) -> "$key=$value" }}"

inline fun <reified T> Request.Builder.post(body: T) = apply {
    post(body.toJson().toRequestBody(jsonMedia))
}