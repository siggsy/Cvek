package com.siggsy.cvek.utils.network

import android.content.Context
import com.siggsy.cvek.data.easistent.*
import com.siggsy.cvek.utils.decodeAccessJWT
import com.siggsy.cvek.utils.preferences.AuthPreferences
import com.siggsy.cvek.utils.preferences.User
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

internal suspend fun OkHttpClient.execute(
    request: Request,
): Response = suspendCancellableCoroutine { continuation ->
    val call = newCall(request)

    call.enqueue(OkHttpCallback(continuation))

    continuation.invokeOnCancellation {
        call.cancel()
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

    @ExperimentalCoroutinesApi
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
    addInterceptor(HttpLoggingInterceptor().apply { level = logLevel })
}

fun OkHttpClient.Builder.auth(context: Context) = apply {
    val authPref = AuthPreferences(context)
    addInterceptor { chain ->
        val request = chain.request()

        val userAuth = authPref.currentUser
        val accessJwt = userAuth.authToken.decodeAccessJWT()
        if (accessJwt?.isExpired == true) {
            // Request new access token.
            val response = chain.proceed(Request.Builder()
                .defaultHeaders()
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
            .authHeaders(authPref.currentUserId, authPref.currentUser.authToken)
            .build())
    }
}