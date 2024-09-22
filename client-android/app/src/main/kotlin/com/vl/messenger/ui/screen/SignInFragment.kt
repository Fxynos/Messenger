package com.vl.messenger.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.vl.messenger.R
import com.vl.messenger.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment: Fragment(), View.OnClickListener {

    private val model: AuthViewModel by activityViewModels()
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_sign_in, container, false)
        username = root.findViewById(R.id.username)
        password = root.findViewById(R.id.password)
        button = root.findViewById(R.id.sign_in)
        button.setOnClickListener(this)
        root.findViewById<Button>(R.id.sign_up).setOnClickListener(this)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lifecycleOwner = view.findViewTreeLifecycleOwner()!!
        model.isButtonEnabled.observe(lifecycleOwner) { button.isEnabled = it }
        model.loginError.observe(lifecycleOwner) { username.error = it }
        model.passwordError.observe(lifecycleOwner) { password.error = it }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sign_in -> model.signIn(username.text.toString(), password.text.toString())
            R.id.sign_up -> model.navigateToSignUp()
        }
    }
}