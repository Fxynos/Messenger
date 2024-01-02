package com.vl.messenger.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vl.messenger.R

class SignInFragment: Fragment(), View.OnClickListener {

    private lateinit var username: EditText
    private lateinit var password: EditText

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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sign_in ->
                Toast.makeText(requireContext(), "Sign in", Toast.LENGTH_SHORT).show()
            R.id.sign_up ->
                Toast.makeText(requireContext(), "Sign up", Toast.LENGTH_SHORT).show()
        }
    }
}