package xyz.siggsy.cvek.utils.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import xyz.siggsy.cvek.data.RefreshRequest
import xyz.siggsy.cvek.data.RefreshResponse
import xyz.siggsy.cvek.utils.decodeAccessJWT
import xyz.siggsy.cvek.utils.preferences.AuthPreferences
import xyz.siggsy.cvek.utils.preferences.User
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

/**
 * Wrapper function for executing okhttp in kotlin coroutines
 * @param request - Request to execute
 * @return - OkHttp Response object
 */
suspend fun OkHttpClient.execute(
    request: Request,
): Response = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { continuation ->
        val call = newCall(request)
        call.enqueue(OkHttpCallback(continuation))
        continuation.invokeOnCancellation {
            call.cancel()
        }
    }
}

private class OkHttpCallback(
    private val continuation: CancellableContinuation<Response>
) : Callback {
    override fun onFailure(call: Call, e: IOException) {
        if (continuation.isCancelled) {
            return
        }
        continuation.resumeWithException(e)
    }

    override fun onResponse(call: Call, response: Response) {
        if (!call.isCanceled()) {
            continuation.resumeWith(Result.success(response))
        }
    }
}


/**
 * OkHttpClient.execute() wrapper for requests that contain json in body
 * @param request - Request to execute
 * @return - BodyResponse with generified json parser
 */
suspend inline fun <reified T> OkHttpClient.jsonRequest(
    request: Request
) = execute(request).let { BodyResponse<T>(it) { it.decodeJson() } }

/**
 * Executes multiple requests and awaits their result
 * @param requests - vararg of request objects to execute
 * @return - BatchResponse containing all results
 */
suspend inline fun <reified T> OkHttpClient.batchJsonRequest(
    vararg requests: Request,
) = BatchResponse(
    withContext(Dispatchers.IO) {
        requests.map { async { jsonRequest<T>(it) } }
    }.map { it.await() }
)

/**
 * Default okHttp builder settings
 * - 10s read/write timeout
 * - disabled retry on connection failure
 * - enabled redirect following
 */
fun OkHttpClient.Builder.default() = apply {
    writeTimeout(10, TimeUnit.SECONDS)
    readTimeout(10, TimeUnit.SECONDS)
    retryOnConnectionFailure(false)
    followRedirects(true)
}

/**
 * Logger for okHttp client
 */
fun OkHttpClient.Builder.logger(logLevel: HttpLoggingInterceptor.Level) = apply {
    addNetworkInterceptor(HttpLoggingInterceptor().apply { level = logLevel })
}

/**
 * Authentication interceptor that automatically requests for a new token when expired and adds
 * proper headers saved in shared preferences for auth calls
 * @param context - Context to use when acquiring shared preferences
 * @param refreshUrl - Url to use when refreshing JWT tokens
 */
fun OkHttpClient.Builder.auth(context: Context, refreshUrl: String) = apply {
    val authPref = AuthPreferences(context)
    addInterceptor { chain ->
        val request = chain.request()

        val userAuth = authPref.currentUser
        val accessJwt = userAuth.authToken.decodeAccessJWT()
        if (accessJwt?.isExpired == true) {
            Log.i("Test", "token expired\ntoken: $accessJwt")
            // Request new access token.
            val response = chain.proceed(Request.Builder()
                .defaultHeaders(authRequest = true)
                .url(refreshUrl.toHttpUrl())
                .post(RefreshRequest(userAuth.refreshToken))
                .build())

            if (response.isSuccessful) {
                // Save tokens to preferences.
                val refreshResponse: RefreshResponse = response.decodeJson()
                authPref.currentUser = User(refreshResponse.accessToken.token, refreshResponse.refreshToken)
            } else {
                return@addInterceptor response
            }
        }

        return@addInterceptor chain.proceed(request.newBuilder()
            .defaultHeaders()
            .authHeaders(authPref.currentUserId, authPref.currentUser.authToken)
            .build())
    }
}