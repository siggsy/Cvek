package xyz.siggsy.cvek.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor.*
import xyz.siggsy.cvek.data.ApiError
import xyz.siggsy.cvek.data.HttpRequestError
import xyz.siggsy.cvek.data.LoginResponse
import xyz.siggsy.cvek.data.login
import xyz.siggsy.cvek.utils.network.default
import xyz.siggsy.cvek.utils.network.logger
import xyz.siggsy.cvek.utils.repo

class LoginModel(app: Application) : AndroidViewModel(app) {

    var username = MutableLiveData("")
    var password = MutableLiveData("")
    val error = MutableLiveData<String>(null)

    val loginData by lazy {
        MutableLiveData<LoginResponse>()
    }

    fun login() = viewModelScope.launch {
        repo.getUserAuth(username.value!!, password.value!!)
            .catch {
                error.postValue(when (it) {
                    is ApiError -> it.response.error.userMessage
                    else -> "Nekaj je šlo narobe"
                })
            }
            .collect {
                loginData.postValue(it)
            }
    }

}