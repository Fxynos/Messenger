package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vl.messenger.R
import com.vl.messenger.databinding.ActivityAuthBinding
import com.vl.messenger.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity: AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateToFragment(SignInFragment::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.events.collect(this@AuthActivity::handleEvent) }
            }
        }
    }

    private fun handleEvent(event: AuthViewModel.DataDrivenEvent) {
        when (event) {
            AuthViewModel.DataDrivenEvent.NavigateLoggedIn -> {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
            AuthViewModel.DataDrivenEvent.NavigateSignIn -> navigateToFragment(SignInFragment::class.java)
            AuthViewModel.DataDrivenEvent.NavigateSignUp -> navigateToFragment(SignUpFragment::class.java)
            AuthViewModel.DataDrivenEvent.NotifyLoginTaken -> showCouldNotSignInPopup(
                    getString(R.string.info_login_taken)
                )
            AuthViewModel.DataDrivenEvent.NotifyWrongCredentials -> showCouldNotSignInPopup(
                    getString(R.string.info_wrong_credentials)
                )
            is AuthViewModel.DataDrivenEvent.NotifyError -> showCouldNotSignInPopup(
                    event.message ?: getString(R.string.info_unexpected_error)
                )
        }
    }

    private fun navigateToFragment(fragment: Class<out Fragment>) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.route_placeholder, fragment, null)
            .commit()
    }

    private fun showCouldNotSignInPopup(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_could_not_sign_in)
            .setMessage(message)
            .setCancelable(true)
            .show()
    }
}