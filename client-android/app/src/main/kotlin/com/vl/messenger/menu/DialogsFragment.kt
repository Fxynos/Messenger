package com.vl.messenger.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vl.messenger.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dialogs, container, false)
    }
}