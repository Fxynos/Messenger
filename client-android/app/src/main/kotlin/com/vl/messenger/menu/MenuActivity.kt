package com.vl.messenger.menu

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.vl.messenger.R

class MenuActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var navigation: NavigationView
    private val routes = mapOf(
        R.id.search to SearchFragment::class.java,
        R.id.friends to FriendsFragment::class.java,
        R.id.dialogs to DialogsFragment::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity)
        drawer = findViewById(R.id.drawer)
        navigation = findViewById(R.id.navigation)
        navigation.setNavigationItemSelectedListener(this)

        navigation.setCheckedItem(R.id.dialogs)
        navigateTo(R.id.dialogs)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        Toast.makeText(this, menuItem.title!!, Toast.LENGTH_SHORT).show()
        return when (menuItem.groupId) {
            R.id.navigation -> {
                navigateTo(menuItem.itemId)
                drawer.closeDrawer(GravityCompat.START)
                true
            }
            else -> false
        }
    }

    private fun navigateTo(@IdRes route: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.placeholder, routes[route]!!, null)
            .commit()
    }
}