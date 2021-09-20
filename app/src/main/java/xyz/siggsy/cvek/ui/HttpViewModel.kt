package xyz.siggsy.cvek.ui

import androidx.lifecycle.ViewModel
import okhttp3.OkHttpClient

open class HttpViewModel(private val http: OkHttpClient) : ViewModel() {
}