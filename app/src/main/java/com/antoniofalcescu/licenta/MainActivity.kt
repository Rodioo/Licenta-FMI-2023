package com.antoniofalcescu.licenta

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.antoniofalcescu.licenta.databinding.ActivityMainBinding
import com.antoniofalcescu.licenta.login.*
import com.antoniofalcescu.licenta.repository.FIREBASE_DELETE_EMPTY_ROOMS_INTERVAL
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.ACCESS_TOKEN_REFRESH_INTERVAL
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.ACCESS_TOKEN_REFRESH_MARGIN
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse



//TODO: add try/catch for error when user fails to connect to spotify api (no/bad internet)
//TODO: refactor the code in here and inside login fragment (DRY)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController

    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private val accessTokenHandler = Handler(Looper.getMainLooper())
    private val firebaseHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLoginResult(result)
        }

        viewModelFactory = MainViewModelFactory(this.application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        bottomNavigation = binding.bottomNavigation

        navController = Navigation.findNavController(this, R.id.navHostFragment)
        bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.profileFragment
                || destination.id == R.id.homeFragment
                || destination.id == R.id.discoverFragment) {
                bottomNavigation.visibility = View.VISIBLE
            } else {
                bottomNavigation.visibility = View.GONE
            }
        }

        accessTokenHandler.postDelayed(object: Runnable {
            override fun run() {
                reinitializeAccessToken()
                accessTokenHandler.postDelayed(this, ACCESS_TOKEN_REFRESH_INTERVAL)
            }
        }, ACCESS_TOKEN_REFRESH_INTERVAL)

        firebaseHandler.postDelayed(object: Runnable {
            override fun run() {
                viewModel.deleteEmptyRooms()
                firebaseHandler.postDelayed(this, FIREBASE_DELETE_EMPTY_ROOMS_INTERVAL)
            }
        }, FIREBASE_DELETE_EMPTY_ROOMS_INTERVAL)

        viewModel.accessToken.observe(this) { accessToken ->
            val currentTime = System.currentTimeMillis()
            if (accessToken != null && accessToken.expiresAt > currentTime + ACCESS_TOKEN_REFRESH_MARGIN && !accessToken.needsRefresh) {
                navController.navigate(R.id.profileFragment)
            } else {
                try {
                    reinitializeAccessToken()
                } catch (e: Exception) {
                    Log.e("eroare", e.toString())
                    navController.navigate(R.id.loginFragment)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        viewModel.accessToken.value?.value?.let {
            viewModel.saveAccessToken(it)
        }
    }

    private fun reinitializeAccessToken() {
        val currentTime = System.currentTimeMillis()

        if (viewModel.accessToken.value?.value == null
            || viewModel.accessToken.value!!.expiresAt <= currentTime + ACCESS_TOKEN_REFRESH_MARGIN
            || viewModel.accessToken.value!!.needsRefresh) {
            spotifyLogin()
            Log.e("REINITIALIZED_ACCESS_TOKEN", viewModel.accessToken.value.toString())
        }
    }

    private fun spotifyLogin() {
        val request = getAuthenticationRequest()
        val loginIntent = AuthorizationClient.createLoginActivityIntent(this, request)
        launcher.launch(loginIntent)
    }

    private fun handleLoginResult(result: ActivityResult) {
        if (result.data != null) {
            val response = AuthorizationClient.getResponse(result.resultCode, result.data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    viewModel.saveAndGetAccessToken(response.accessToken)
                    Toast.makeText(this, "Reinitialized Spotify Connection", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.profileFragment)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("token_auth", response.error)
                    navController.navigate(R.id.loginFragment)
                    Toast.makeText(this, "Failed to reinitialize Spotify Connection.\n Please restart the application.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    navController.navigate(R.id.loginFragment)
                    Log.e("token_auth", response.type.toString())
                }
            }
        } else {
            navController.navigate(R.id.loginFragment)
            Log.e("token_auth", "null")
        }
    }

    private fun getAuthenticationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(clientId, AuthorizationResponse.Type.TOKEN, redirectUri)
            .setShowDialog(false)
            .setScopes(Permission.ALL_ACCESS.scopes)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenHandler.removeCallbacksAndMessages(null)
        firebaseHandler.removeCallbacksAndMessages(null)
    }
}