package com.antoniofalcescu.licenta.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.antoniofalcescu.licenta.databinding.FragmentHomeBinding
import com.antoniofalcescu.licenta.profile.tracks.TracksAdapter
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory

    private lateinit var genresAdapter: GenreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        viewModelFactory = HomeViewModelFactory(this.requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        genresAdapter = GenreAdapter {genreName ->
            view?.findNavController()?.navigate(
                HomeFragmentDirections.actionHomeFragmentToGameFragment(genreName)
            )
        }

        binding.genresRecycler.adapter = genresAdapter
        binding.genresRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.SMALL), Orientation.VERTICAL)
        )

        binding.createGameButton.setOnClickListener {
            createGameHandler()
        }
        binding.joinGameButton.setOnClickListener {
            joinGameHandler()
        }

        binding.genresGameButton.setOnClickListener {
            binding.createGameLayout.visibility = View.GONE
            viewModel.getGameGenres()
            binding.genresView.visibility = View.VISIBLE
        }

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
        } else if (binding.genresView.visibility == View.VISIBLE) {
            binding.genresView.visibility = View.GONE
            binding.createGameLayout.visibility = View.VISIBLE
        }
    }
}