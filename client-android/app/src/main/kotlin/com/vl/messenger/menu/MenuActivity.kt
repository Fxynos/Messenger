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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        drawer.closeDrawer(GravityCompat.START)
        if (menuItem.groupId == R.id.navigation)
            navigateTo(menuItem.itemId)
        else when (menuItem.itemId) {
            R.id.conversation ->
                Toast.makeText(this, "Не реализовано", Toast.LENGTH_SHORT).show()
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