package com.antoniofalcescu.licenta

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.antoniofalcescu.licenta.databinding.ActivityMainBinding
import com.antoniofalcescu.licenta.login.*
import com.antoniofalcescu.licenta.repository.accessToken.AccessToken
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ACCESS_TOKEN_REFRESH_MARGIN: Long = 15 * 60 * 1000
private const val ACCESS_TOKEN_REFRESH_INTERVAL: Long = 60 * 1000

//TODO: refactor the code in here and inside login fragment (DRY)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController

    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val handler = Handler(Looper.getMainLooper())

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

        handler.postDelayed(object: Runnable {
            override fun run() {
                reinitializeAccessToken()
                handler.postDelayed(this, ACCESS_TOKEN_REFRESH_INTERVAL)
            }
        }, ACCESS_TOKEN_REFRESH_INTERVAL)
    }

    override fun onStart() {
        super.onStart()
        viewModel.accessToken.observe(this) { accessToken ->
            val currentTime = System.currentTimeMillis()
            if (accessToken != null && accessToken.expiresAt > currentTime + ACCESS_TOKEN_REFRESH_MARGIN && !accessToken.needsRefresh) {
                navController.navigate(R.id.profileFragment)
            } else {
                try {
                    reinitializeAccessToken()
                } catch(e: Exception) {
                    Log.e("eroare", e.toString())
                    navController.navigate(R.id.loginFragment)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        viewModel.accessToken.value?.value?.let { viewModel.saveAccessToken(it) }
    }

    private fun reinitializeAccessToken() {
        val currentTime = System.currentTimeMillis()
        if (viewModel.accessToken.value?.value == null
            || viewModel.accessToken.value!!.expiresAt <= currentTime + ACCESS_TOKEN_REFRESH_MARGIN
            || viewModel.accessToken.value!!.needsRefresh) {
            Log.e("main", "4")
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
                    viewModel.saveAccessToken(response.accessToken)
                    Toast.makeText(this, "Reinitialized Spotify Connection", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.profileFragment)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("token_auth", response.error)
                    Toast.makeText(this, "Failed to reinitialize Spotify Connection.\n Please restart the application.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Log.e("token_auth", response.type.toString())
                }
            }
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
        handler.removeCallbacksAndMessages(null)
    }
}