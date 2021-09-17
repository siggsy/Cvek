package xyz.siggsy.cvek.utils.network

import okhttp3.Response
import xyz.siggsy.cvek.data.ErrorResponse
import xyz.siggsy.cvek.utils.decodeJson

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