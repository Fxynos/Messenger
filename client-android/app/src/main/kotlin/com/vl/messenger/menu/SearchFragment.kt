package com.vl.messenger.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.vl.messenger.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment() {

    private lateinit var menu: ImageButton
    private lateinit var search: ImageButton
    private lateinit var input: EditText
    private lateinit var result: RecyclerView
    private lateinit var hint: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false).apply {
        menu = findViewById(R.id.menu)
        search = findViewById(R.id.search)
        input = findViewById(R.id.input)
        result = findViewById(R.id.result)
        hint = findViewById(R.id.hint)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO
    }
}