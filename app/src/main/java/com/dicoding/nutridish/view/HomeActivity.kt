package com.dicoding.nutridish.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dicoding.nutridish.R
import com.dicoding.nutridish.databinding.ActivityHomeBinding
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize NavHost and NavController
        initNavHost()

        // Setup CurvedBottomNavigation
        setUpBottomNavigation()
    }

    private fun initNavHost() {
            val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setUpBottomNavigation() {
        val bottomNavigationItems = listOf(
            CurvedBottomNavigation.Model(HOME_ITEM, getString(R.string.dashboard), R.drawable.ic_home),
            CurvedBottomNavigation.Model(OFFERS_ITEM, getString(R.string.explore), R.drawable.ic_explore),
            CurvedBottomNavigation.Model(SECTION_ITEM, getString(R.string.favorite), R.drawable.ic_favorite),
            CurvedBottomNavigation.Model(MORE_ITEM, getString(R.string.profile), R.drawable.ic_profile),
        )

        binding.bottomNavigation.apply {
            bottomNavigationItems.forEach { add(it) }

            // Handle menu item clicks
            setOnClickMenuListener {
                when (it.id) {
                    HOME_ITEM -> navController.navigate(R.id.nav_home)
                    OFFERS_ITEM -> navController.navigate(R.id.nav_explore)
                    SECTION_ITEM -> navController.navigate(R.id.nav_favorite)
                    MORE_ITEM -> navController.navigate(R.id.nav_profile)
                }
            }

            // Show default menu item
            show(HOME_ITEM)
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == HOME_ITEM) {
            super.onBackPressed()
        } else {
            navController.popBackStack(R.id.nav_home, false)
        }
    }

    companion object {
        val HOME_ITEM = R.id.nav_home
        val OFFERS_ITEM = R.id.nav_explore
        val SECTION_ITEM = R.id.nav_favorite
        val MORE_ITEM = R.id.nav_profile
    }
}
