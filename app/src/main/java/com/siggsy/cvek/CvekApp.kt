package com.siggsy.cvek

import android.app.Application
import com.siggsy.cvek.utils.network.auth
import com.siggsy.cvek.utils.network.default
import com.siggsy.cvek.utils.network.logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor.*

class CvekApp : Application() {

    lateinit var httpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        httpClient = OkHttpClient.Builder()
            .default()
            .logger(Level.BODY)
            .auth(this@CvekApp)
            .build()
    }
}