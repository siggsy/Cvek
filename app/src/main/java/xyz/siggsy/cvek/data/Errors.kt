package xyz.siggsy.cvek.data

import xyz.siggsy.cvek.utils.network.BodyResponse

data class HttpRequestError(val code: Int) : Throwable()
data class ApiError(val response: ErrorResponse) : Throwable()

fun <T> apiError(response: BodyResponse<T>): Throwable {
    val error = runCatching { response.error() }
    return ApiError(error.getOrElse { return HttpRequestError(response.response.code) })
}