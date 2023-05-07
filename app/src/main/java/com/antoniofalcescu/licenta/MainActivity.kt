package com.antoniofalcescu.licenta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.antoniofalcescu.licenta.databinding.ActivityMainBinding
import com.antoniofalcescu.licenta.login.*
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val ACCESS_TOKEN_REFRESH_MARGIN: Long = 5 * 1000

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController

    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModelFactory = MainViewModelFactory(this.application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

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

        viewModel.accessToken.observe(this) { accessToken ->
            val currentTime = System.currentTimeMillis()

            //TODO: De adaugat check daca este in joc sa nu il deconecteze
            if (accessToken.value == null || accessToken.expiresAt <= currentTime - ACCESS_TOKEN_REFRESH_MARGIN) {
                navController.navigate(R.id.loginFragment)
            } else {
                navController.navigate(R.id.profileFragment)
            }
        }
    }
}