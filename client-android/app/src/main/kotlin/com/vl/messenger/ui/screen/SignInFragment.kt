package com.vl.messenger.ui.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vl.messenger.R
import com.vl.messenger.databinding.FragmentSignInBinding
import com.vl.messenger.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment: Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.signUp.setOnClickListener { viewModel.navigateToSignUp() }
        binding.signIn.setOnClickListener {
            viewModel.signIn(
                binding.username.text.toString().trim(),
                binding.password.text.toString().trim()
            )
        }
        view.findViewTreeLifecycleOwner()!!.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest(this@SignInFragment::updateState)
            }
        }
    }

    private fun updateState(state: AuthViewModel.UiState) {
        when (state) {
            AuthViewModel.UiState.REGULAR -> setState(isButtonEnabled = true)
            AuthViewModel.UiState.LOADING -> setState(isButtonEnabled = false)
            AuthViewModel.UiState.LOGIN_ILLEGAL_CHAR -> setState(loginError = R.string.error_login_char)
            AuthViewModel.UiState.LOGIN_ILLEGAL_LENGTH -> setState(loginError = R.string.error_login_length)
            AuthViewModel.UiState.PASSWORDS_DIFFER -> Unit
            AuthViewModel.UiState.PASSWORD_ILLEGAL_LENGTH -> setState(passwordError = R.string.error_password_length)
        }
    }

    private fun setState(
        @StringRes loginError: Int? = null,
        @StringRes passwordError: Int? = null,
        isButtonEnabled: Boolean = true
    ) {
        fun Int.getString() = getString(this)
        binding.username.error = loginError?.getString()
        binding.password.error = passwordError?.getString()
        binding.signIn.isEnabled = isButtonEnabled
    }
}