package com.vl.messenger.ui.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StringRes
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.events.collect(this@AuthActivity::handleEvent) }
            }
        }
    }

    private fun handleEvent(event: AuthViewModel.DataDrivenEvent) {
        when (event) {
            AuthViewModel.DataDrivenEvent.LOGGED_IN -> {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
            AuthViewModel.DataDrivenEvent.NAVIGATE_SIGN_IN -> navigateToFragment(SignInFragment::class.java)
            AuthViewModel.DataDrivenEvent.NAVIGATE_SIGN_UP -> navigateToFragment(SignUpFragment::class.java)
            AuthViewModel.DataDrivenEvent.NOTIFY_LOGIN_TAKEN -> showPopup(
                    R.string.title_could_not_sign_in,
                    R.string.info_login_taken
                )
            AuthViewModel.DataDrivenEvent.NOTIFY_WRONG_CREDENTIALS -> showPopup(
                    R.string.title_could_not_sign_in,
                    R.string.info_wrong_credentials
                )
            AuthViewModel.DataDrivenEvent.NOTIFY_ERROR -> showPopup(
                    R.string.title_could_not_sign_in,
                    R.string.info_unexpected_error
                )
        }
    }

    private fun navigateToFragment(fragment: Class<out Fragment>) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.route_placeholder, fragment, null)
            .commit()
    }

    private fun showPopup(@StringRes title: Int, @StringRes message: Int) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .show()
    }
}