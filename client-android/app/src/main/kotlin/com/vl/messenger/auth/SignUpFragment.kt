package com.vl.messenger.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.vl.messenger.R

class SignUpFragment: Fragment(), View.OnClickListener {

    private lateinit var model: AuthViewModel
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var repeatPassword: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_sign_up, container, false)
        username = root.findViewById(R.id.username)
        password = root.findViewById(R.id.password)
        repeatPassword = root.findViewById(R.id.repeat_password)
        button = root.findViewById(R.id.sign_up)
        button.setOnClickListener(this)
        root.findViewById<Button>(R.id.sign_in).setOnClickListener(this)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lifecycleOwner = view.findViewTreeLifecycleOwner()!!
        model.isButtonEnabled.observe(lifecycleOwner) { button.isEnabled = it }
        model.loginError.observe(lifecycleOwner) { username.error = it }
        model.passwordError.observe(lifecycleOwner) { password.error = it }
        model.repeatPasswordError.observe(lifecycleOwner) { repeatPassword.error = it }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sign_in -> model.navigateToSignIn()
            R.id.sign_up -> model.attemptSignUp(
                username.text.toString(),
                password.text.toString(),
                repeatPassword.text.toString()
            )
        }
    }
}