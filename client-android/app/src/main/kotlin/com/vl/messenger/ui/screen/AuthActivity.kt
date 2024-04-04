package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vl.messenger.R
import com.vl.messenger.data.manager.AuthManager
import com.vl.messenger.data.manager.SessionStore
import com.vl.messenger.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity: AppCompatActivity() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var sessionStore: SessionStore

    private val model: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        model.route.observe(this) { route ->
            when (route!!) {
                AuthViewModel.Route.SIGN_IN -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.route_placeholder, SignInFragment())
                    .commit()
                AuthViewModel.Route.SIGN_UP -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.route_placeholder, SignUpFragment())
                    .commit()
                AuthViewModel.Route.CLOSE -> onLoggedIn()
            }
        }
        model.popup.observe(this) { popup ->
            if (popup == null)
                return@observe
            AlertDialog.Builder(this)
                .setTitle(popup.title)
                .setMessage(popup.text)
                .setCancelable(true)
                .setOnDismissListener { popup.hide() }
                .show()
        }
    }

    private fun onLoggedIn() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }
}