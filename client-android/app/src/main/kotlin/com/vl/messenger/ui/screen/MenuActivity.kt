package com.vl.messenger.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.navigation.NavigationView
import com.vl.messenger.R
import com.vl.messenger.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuActivity: AppCompatActivity(), View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val TAG = "Menu"
    }

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var drawer: DrawerLayout
    private lateinit var navigation: NavigationView
    private lateinit var username: TextView
    private lateinit var image: ImageView
    private val routes: Map<Int, Class<out Fragment>> = mapOf(
        R.id.search to SearchFragment::class.java,
        R.id.friends to FriendsFragment::class.java,
        R.id.dialogs to DialogsFragment::class.java
    )
    private lateinit var pickMediaRequestLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        pickMediaRequestLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            if (it == null)
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
            else
                viewModel.uploadPhoto(it)
        }

        drawer = findViewById(R.id.drawer)
        navigation = findViewById(R.id.navigation)
        navigation.setNavigationItemSelectedListener(this)
        navigation.getHeaderView(0).apply {
            username = findViewById(R.id.title)
            image = findViewById(R.id.image)
        }
        image.setOnClickListener(this)
        navigation.setCheckedItem(R.id.dialogs)
        navigateTo(R.id.dialogs)

        lifecycleScope.launch(Dispatchers.Main) {
            launch { viewModel.profile.collect { username.text = it?.login ?: "" } }
            launch { viewModel.profileImage.collect { image.setImageBitmap(it) } }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        Log.d(TAG, "\"${menuItem.title}\" is chosen")
        drawer.closeDrawer(GravityCompat.START)
        if (menuItem.groupId == R.id.navigation)
            navigateTo(menuItem.itemId)
        else when (menuItem.itemId) {
            R.id.conversation ->
                Toast.makeText(this, "Не реализовано", Toast.LENGTH_SHORT).show() // TODO
            R.id.logout -> {
                viewModel.logOut()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }
        return true
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.image -> PopupMenu(this, image).apply {
                menu.add(getString(R.string.upload_photo)).setOnMenuItemClickListener {
                    pickMediaRequestLauncher.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    ))
                    true
                }
                show()
            }
        }
    }

    fun openDrawer() {
        drawer.openDrawer(GravityCompat.START)
    }

    private fun navigateTo(@IdRes route: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.placeholder, routes[route]!!, null)
            .commit()
    }
}