package com.siggsy.cvek.utils.network

import com.siggsy.cvek.data.easistent.ErrorResponse
import com.siggsy.cvek.utils.decodeJson
import okhttp3.Request
import okhttp3.Response

inline fun <reified T> Response.decodeJson(): T =
    body!!.string().decodeJson()

data class BodyResponse<T>(
    val response: Response,
    val body: () -> T?,
) {
    val error: () -> ErrorResponse = response::decodeJson
    val failed get() = response.code > 299
}

data class BatchResponse<T>(
    val responses: List<BodyResponse<T>>
) {
    val failed get() = responses.any { it.failed }
}