package com.siggsy.cvek.utils.network

import android.content.Context
import android.util.Log
import com.siggsy.cvek.data.easistent.*
import com.siggsy.cvek.utils.decodeAccessJWT
import com.siggsy.cvek.utils.preferences.AuthPreferences
import com.siggsy.cvek.utils.preferences.User
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

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

suspend inline fun <reified T> OkHttpClient.await(
    request: Request
) = execute(request).let { BodyResponse<T>(it) { it.decodeJson() } }

suspend inline fun <reified T> OkHttpClient.batchAwait(
    vararg requests: Request,
) = BatchResponse(
    withContext(Dispatchers.IO) {
        requests.map { async { await<T>(it) } }
    }.map { it.await() }
)

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

fun OkHttpClient.Builder.default() = apply {
    writeTimeout(10, TimeUnit.SECONDS)
    readTimeout(10, TimeUnit.SECONDS)
    retryOnConnectionFailure(false)
    followRedirects(true)
}

fun OkHttpClient.Builder.logger(logLevel: HttpLoggingInterceptor.Level) = apply {
    addNetworkInterceptor(HttpLoggingInterceptor().apply { level = logLevel })
}

fun OkHttpClient.Builder.auth(context: Context) = apply {
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
                .url(REFRESH_TOKEN.toHttpUrl())
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