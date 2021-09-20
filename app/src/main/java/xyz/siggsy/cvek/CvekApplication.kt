package xyz.siggsy.cvek

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor.*
import xyz.siggsy.cvek.data.REFRESH_TOKEN
import xyz.siggsy.cvek.utils.network.auth
import xyz.siggsy.cvek.utils.network.default
import xyz.siggsy.cvek.utils.network.logger

class CvekApplication : Application() {

    lateinit var http: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        http = OkHttpClient.Builder()
            .default()
            .logger(Level.BODY)
            .auth(this, REFRESH_TOKEN)
            .build()
    }
}