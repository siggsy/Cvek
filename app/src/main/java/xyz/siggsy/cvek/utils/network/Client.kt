package xyz.siggsy.cvek.utils.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import xyz.siggsy.cvek.data.RefreshRequest
import xyz.siggsy.cvek.data.RefreshResponse
import xyz.siggsy.cvek.utils.decodeAccessJWT
import xyz.siggsy.cvek.utils.preferences.AuthPreferences
import xyz.siggsy.cvek.utils.preferences.User
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

val okHttpJob = Job()
/**
 * Wrapper function for executing okhttp in kotlin coroutines
 * @param request to execute
 * @return OkHttp Response object
 */
suspend fun OkHttpClient.execute(
    request: Request,
): Response = withContext(Dispatchers.IO + okHttpJob) {
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
 * @param request Request to execute
 * @return BodyResponse with generified json parser
 */
suspend inline fun <reified T> OkHttpClient.jsonRequest(
    request: Request
) = execute(request).let { BodyResponse<T>(it) { it.decodeJson() } }

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
 * @param logLevel to apply to the HttpLoggingInterceptor
 */
fun OkHttpClient.Builder.logger(logLevel: HttpLoggingInterceptor.Level) = apply {
    addNetworkInterceptor(HttpLoggingInterceptor().apply { level = logLevel })
}

/**
 * Authentication interceptor that automatically requests for a new token when expired and adds
 * proper headers saved in shared preferences for auth calls
 * @param context to use when acquiring shared preferences
 * @param refreshUrl to use when refreshing JWT tokens
 */
fun OkHttpClient.Builder.auth(context: Context, refreshUrl: String) = apply {
    val authPref = AuthPreferences(context)
    addInterceptor { chain ->
        val request = chain.request()

        synchronized(this) {
            val userAuth = authPref.currentUser
            val accessJwt = userAuth?.authToken?.decodeAccessJWT()
            if (accessJwt?.isExpired == true) {
                Log.i("Test", "token expired\ntoken: $accessJwt")
                // Request new access token.
                val response = chain.proceed(refreshUrl.toRequest {
                    defaultHeaders(authRequest = true)
                    post(RefreshRequest(userAuth.refreshToken))
                })

                if (response.isSuccessful) {
                    // Save tokens to preferences.
                    val refreshResponse: RefreshResponse = response.decodeJson()
                    authPref.users += (authPref.currentUserId to User(
                            refreshResponse.accessToken.token,
                            refreshResponse.refreshToken
                        ))
                } else {
                    return@addInterceptor response
                }
            } else if (accessJwt == null) {
                return@addInterceptor Response.Builder()
                    .code(401)
                    .body("{}".toResponseBody())
                    .build()
            }
        }

        return@addInterceptor chain.proceed(request.newBuilder()
            .defaultHeaders()
            .authHeaders(authPref.currentUserId, authPref.currentUser!!.authToken)
            .build())
    }
}