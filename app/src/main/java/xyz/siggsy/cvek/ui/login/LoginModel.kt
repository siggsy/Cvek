package xyz.siggsy.cvek.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor.*
import xyz.siggsy.cvek.data.LoginResponse
import xyz.siggsy.cvek.data.login
import xyz.siggsy.cvek.utils.network.default
import xyz.siggsy.cvek.utils.network.logger

class LoginModel : ViewModel() {

    var username = MutableLiveData("")
    var password = MutableLiveData("")
    val error = MutableLiveData<String>(null)

    val loginData = MutableLiveData<LoginResponse>()

    fun login() = viewModelScope.launch {
        val http = OkHttpClient.Builder()
            .default()
            .logger(Level.BODY)
            .build()

        val response = http.login(username.value!!, password.value!!)
        if (response.isSuccessful) {
            loginData.postValue(response.body())
        } else {
            error.value = response.error().error.userMessage ?: "Nekaj je šlo narobe"
        }
    }

}