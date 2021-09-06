package com.siggsy.cvek.utils.network

import com.siggsy.cvek.utils.toJson
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

val jsonMedia = "application/json".toMediaType()

fun Request.Builder.defaultHeaders() = apply {
    header("user-agent", "Mozilla/5.0")
    header("accept", "application/json, text/html")
    header("accept-language", "sl-SI,sl;q=0.9,en-GB;q=0.8,en;q=0.7,de;q=0.6")
    header("x-client-platform", "web")
    header("x-client-version", "13")
    header("x-requested-with", "XMLHttpRequest")
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

inline fun <reified T> Request.Builder.post(body: T) = apply {
    body.toJson().toRequestBody(jsonMedia)
}