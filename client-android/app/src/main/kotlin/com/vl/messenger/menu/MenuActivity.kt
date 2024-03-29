package com.vl.messenger.menu

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.vl.messenger.R
import com.vl.messenger.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var drawer: DrawerLayout
    private lateinit var navigation: NavigationView
    private lateinit var username: TextView
    private lateinit var image: ImageView
    private val routes = mapOf(
        R.id.search to SearchFragment::class.java,
        R.id.friends to FriendsFragment::class.java,
        R.id.dialogs to DialogsFragment::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        drawer = findViewById(R.id.drawer)
        navigation = findViewById(R.id.navigation)
        navigation.setNavigationItemSelectedListener(this)
        navigation.getHeaderView(0).apply {
            username = findViewById(R.id.title)
            image = findViewById(R.id.image)
        }
        navigation.setCheckedItem(R.id.dialogs)
        navigateTo(R.id.dialogs)

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.profile.collect { username.text = it.login }
            viewModel.profileImage.collect { image.setImageBitmap(it) }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        Toast.makeText(this, menuItem.title!!, Toast.LENGTH_SHORT).show()
        drawer.closeDrawer(GravityCompat.START)
        if (menuItem.groupId == R.id.navigation)
            navigateTo(menuItem.itemId)
        else when (menuItem.itemId) {
            R.id.conversation ->
                Toast.makeText(this, "Не реализовано", Toast.LENGTH_SHORT).show()
            R.id.logout -> {
                viewModel.logOut()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }
        return true
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