package com.vl.messenger.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.vl.messenger.App
import com.vl.messenger.R

class AuthActivity: AppCompatActivity() {

    private lateinit var model: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        model = ViewModelProvider(
            this,
            AuthViewModel.Factory(application as App)
        )[AuthViewModel::class.java]
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
                AuthViewModel.Route.CLOSE -> finish()
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
}