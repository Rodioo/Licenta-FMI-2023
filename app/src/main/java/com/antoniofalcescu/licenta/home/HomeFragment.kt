package com.antoniofalcescu.licenta.home

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentDiscoverBinding
import com.antoniofalcescu.licenta.databinding.FragmentHomeBinding
import com.antoniofalcescu.licenta.discover.DiscoverViewModel
import com.antoniofalcescu.licenta.discover.DiscoverViewModelFactory

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        viewModelFactory = HomeViewModelFactory(this.requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.createGameButton.setOnClickListener { createGameHandler() }
        binding.joinGameButton.setOnClickListener { joinGameHandler() }

        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                resetUI()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)

        return binding.root
    }

    private fun createGameHandler() {
        binding.startGameLayout.visibility = View.GONE
        binding.createGameLayout.visibility = View.VISIBLE
    }

    private fun joinGameHandler() {
        binding.startGameLayout.visibility = View.GONE
        binding.joinGameLayout.visibility = View.VISIBLE
    }

    private fun resetUI() {
        if (binding.createGameLayout.visibility == View.VISIBLE) {
            binding.createGameLayout.visibility = View.GONE
            binding.startGameLayout.visibility = View.VISIBLE
        }
        else if (binding.joinGameLayout.visibility == View.VISIBLE) {
            binding.joinGameLayout.visibility = View.GONE
            binding.startGameLayout.visibility = View.VISIBLE
        }
    }
}