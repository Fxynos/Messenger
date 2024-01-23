package com.vl.messenger.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.vl.messenger.R

class SignInFragment: Fragment(), View.OnClickListener {

    private lateinit var model: AuthViewModel
    private lateinit var username: EditText
    private lateinit var password: EditText

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
            .inflate(R.layout.fragment_sign_in, container, false)
        username = root.findViewById(R.id.username)
        password = root.findViewById(R.id.password)
        root.findViewById<Button>(R.id.sign_in).setOnClickListener(this)
        root.findViewById<Button>(R.id.sign_up).setOnClickListener(this)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lifecycleOwner = view.findViewTreeLifecycleOwner()!!
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