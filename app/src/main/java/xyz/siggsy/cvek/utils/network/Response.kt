package xyz.siggsy.cvek.utils.network

import okhttp3.Response
import xyz.siggsy.cvek.data.ErrorResponse
import xyz.siggsy.cvek.utils.decodeJson

/**
 * Returns body as decoded string
 * @return body decoded to generic T
 */
inline fun <reified T> Response.decodeJson(): T =
    body!!.string().decodeJson()

/**
 * Response wrapper for parsing error and generic T
 * @param response object from the original request
 * @param body init lambda function for response body
 */
data class BodyResponse<T>(
    val response: Response,
    val body: () -> T?,
) {
    val error: () -> ErrorResponse = response::decodeJson
    val isSuccessful get() = response.isSuccessful
}

/**
 * Response wrapper for multiple BodyResponses
 * @param responses List of awaited responses
 */
data class BatchResponse<T>(
    val responses: List<BodyResponse<T>>
) {
    val isSuccessful get() = responses.all { it.isSuccessful }
}