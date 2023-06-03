package com.antoniofalcescu.licenta.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.antoniofalcescu.licenta.R
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

    private var roomCode = "0000"

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
            viewModel.createRoom(genreName)
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

        binding.topTracksGameButton.setOnClickListener {
            binding.createGameLayout.visibility = View.GONE
            viewModel.user.observe(viewLifecycleOwner) {
                viewModel.createRoom(resources.getString(R.string.most_listened_songs))
            }
        }

        binding.topArtistsGameButton.setOnClickListener {
            binding.createGameLayout.visibility = View.GONE
            viewModel.user.observe(viewLifecycleOwner) {
                viewModel.createRoom(resources.getString(R.string.most_listened_artists))
            }
        }

        binding.joinGameCodeButton.setOnClickListener {
            Log.e("ceva", roomCode)
            viewModel.user.observe(viewLifecycleOwner) {
                viewModel.joinRoom(roomCode)
            }
        }

        viewModel.room.observe(viewLifecycleOwner) {room->
            view?.findNavController()?.navigate(
                HomeFragmentDirections.actionHomeFragmentToGameFragment(room!!)
            )
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
        getRoomCode()
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

    private fun getRoomCode() {
        val codeValues = mutableListOf("0", "0", "0", "0")
        fun onRoomCodeValueChanged(numberPicker: NumberPicker, oldValue: Int, newValue: Int) {
            when (numberPicker.id) {
                R.id.first_digit_room_code_picker -> codeValues[0] = newValue.toString()
                R.id.second_digit_room_code_picker -> codeValues[1] = newValue.toString()
                R.id.third_digit_room_code_picker -> codeValues[2] = newValue.toString()
                R.id.fourth_digit_room_code_picker -> codeValues[3] = newValue.toString()
            }
            roomCode = codeValues.joinToString("")
        }

        binding.firstDigitRoomCodePicker.setOnValueChangedListener(::onRoomCodeValueChanged)
        binding.secondDigitRoomCodePicker.setOnValueChangedListener(::onRoomCodeValueChanged)
        binding.thirdDigitRoomCodePicker.setOnValueChangedListener(::onRoomCodeValueChanged)
        binding.fourthDigitRoomCodePicker.setOnValueChangedListener(::onRoomCodeValueChanged)
    }

    override fun onStart() {
        super.onStart()
        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.addUser()
        }
    }
}