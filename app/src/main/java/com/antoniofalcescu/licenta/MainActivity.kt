package com.antoniofalcescu.licenta

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.antoniofalcescu.licenta.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.android.appremote.api.SpotifyAppRemote

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigation = binding.bottomNavigation

        navController = Navigation.findNavController(this, R.id.navHostFragment)
        bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.profileFragment
                || destination.id == R.id.homeFragment
                || destination.id == R.id.recommendationsFragment) {
                bottomNavigation.visibility = View.VISIBLE
            } else {
                bottomNavigation.visibility = View.GONE
            }
        }
    }
}