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
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentLoginBinding
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.spotify.sdk.android.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val clientId = "dd13ee5f82ce43d0a607b3ebc1f2de91"
private const val redirectUri = "com.antoniofalcescu.licenta://callback"

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private lateinit var accessToken: String
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleLoginResult(result)
        }

        binding.loginButton.setOnClickListener{
            spotifyLogin()
        }

        return binding.root
    }

    private fun spotifyLogin() {
        val request = getAuthenticationRequest()
        val loginIntent = AuthorizationClient.createLoginActivityIntent(activity, request)
        launcher.launch(loginIntent)
    }

    private fun handleLoginResult(result: ActivityResult) {
        if (result.data != null) {
            val response = AuthorizationClient.getResponse(result.resultCode, result.data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    accessToken = response.accessToken
                    Log.e("success", "1")
                    Log.e("token_auth", response.accessToken)
                    GuessifyApi.retrofitService.getCurrentUserProfile("Bearer $accessToken").enqueue(object:
                        Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            Log.e("ceva", response.code().toString())
                            Log.e("ceva", response.body().toString())
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.e("ceva", t.toString())
                        }

                    })

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