package xyz.siggsy.cvek.ui.launcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xyz.siggsy.cvek.R
import xyz.siggsy.cvek.ui.login.LoginActivity
import xyz.siggsy.cvek.ui.main.MainActivity
import xyz.siggsy.cvek.utils.preferences.AuthPreferences

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // Start activity based on saved credentials
        val authPref = AuthPreferences(this)
        startActivity(Intent(this, when {
            authPref.currentUser != null -> MainActivity::class.java
            authPref.users.isNotEmpty() -> MainActivity::class.java // TODO: create select user activity
            else -> LoginActivity::class.java
        }))
        finish()
    }
}