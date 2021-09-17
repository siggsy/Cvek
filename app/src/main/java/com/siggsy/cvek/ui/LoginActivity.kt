package com.siggsy.cvek.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.siggsy.cvek.CvekApp
import com.siggsy.cvek.data.easistent.login
import com.siggsy.cvek.ui.ui.theme.CvekTheme
import com.siggsy.cvek.utils.network.default
import com.siggsy.cvek.utils.preferences.AuthPreferences
import com.siggsy.cvek.utils.preferences.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttp
import okhttp3.OkHttpClient

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val http = OkHttpClient.Builder().default().build()
        setContent {
            CvekTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        var username by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("")}
                        Text("Cvek")
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") }
                        )
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") }
                        )
                        Button(onClick = {
                            lifecycleScope.launch {
                                val response = http.login(username, password)
                                val login = response.body()!!
                                with(AuthPreferences(applicationContext)) {
                                    currentUserId = login.user.id.toString()
                                    currentUser = User(login.accessToken.token, login.refreshToken)
                                }
                                withContext(Dispatchers.Main) {
                                    val intent = Intent(this@LoginActivity, TestActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }) {
                            Text("Log in")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CvekTheme {
        Greeting("Android")
    }
}