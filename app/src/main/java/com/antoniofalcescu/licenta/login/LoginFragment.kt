package com.antoniofalcescu.licenta.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentLoginBinding
import com.spotify.sdk.android.auth.*

private const val clientId = "dd13ee5f82ce43d0a607b3ebc1f2de91"
private const val redirectUri = "com.antoniofalcescu.licenta://callback"

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var viewModelFactory: LoginViewModelFactory

    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        viewModelFactory = LoginViewModelFactory(this.requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLoginResult(result)
        }

        binding.loginButton.setOnClickListener{
            spotifyLogin()
        }

        return binding.root
    }

    fun spotifyLogin() {
        val request = getAuthenticationRequest()
        val loginIntent = AuthorizationClient.createLoginActivityIntent(activity, request)
        launcher.launch(loginIntent)
    }

    private fun handleLoginResult(result: ActivityResult) {
        if (result.data != null) {
            val response = AuthorizationClient.getResponse(result.resultCode, result.data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    viewModel.saveAccessToken(response.accessToken)
                    view?.findNavController()?.navigate(LoginFragmentDirections.actionLoginFragmentToProfileFragment())
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("token_auth", response.error)
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

}