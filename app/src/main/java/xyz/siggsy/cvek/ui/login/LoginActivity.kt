package xyz.siggsy.cvek.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import xyz.siggsy.cvek.R
import xyz.siggsy.cvek.databinding.ActivityLoginBinding
import xyz.siggsy.cvek.ui.CvekActivity
import xyz.siggsy.cvek.ui.main.MainActivity
import xyz.siggsy.cvek.utils.preferences.AuthPreferences
import xyz.siggsy.cvek.utils.preferences.User

class LoginActivity : CvekActivity() {

    private val loginModel: LoginModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_login)
        binding.loginModel = loginModel
        binding.lifecycleOwner = this

        loginModel.loginData.observe(this) {
            it?.apply {
                repo.saveUser(
                    user.id.toString(),
                    User(
                        accessToken.token,
                        refreshToken
                    )
                )
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

}