package com.siggsy.cvek

import android.app.Application
import com.siggsy.cvek.data.easistent.EasistentApi
import com.siggsy.cvek.utils.network.auth
import com.siggsy.cvek.utils.network.build
import com.siggsy.cvek.utils.network.default
import com.siggsy.cvek.utils.network.logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor.*

class CvekApp : Application() {

    private lateinit var httpClient: OkHttpClient
    private lateinit var api: EasistentApi

    override fun onCreate() {
        super.onCreate()

        httpClient = OkHttpClient.Builder()
            .default()
            .logger(Level.BODY)
            .auth(this@CvekApp)
            .build()

    }
}