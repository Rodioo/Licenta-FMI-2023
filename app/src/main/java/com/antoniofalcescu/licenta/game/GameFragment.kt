package com.antoniofalcescu.licenta.game

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentGameBinding
import com.antoniofalcescu.licenta.databinding.FragmentProfileBinding
import com.antoniofalcescu.licenta.profile.ProfileViewModel
import com.antoniofalcescu.licenta.profile.ProfileViewModelFactory
import com.antoniofalcescu.licenta.profile.tracks.TracksAdapter
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing

//TODO: posibil sa adaug field isOwner in firestore cand creezi tu joc ca sa poti da kick la altii ingame
//TODO: de adaugat flow-ul pentru creare joc din artists/tracks
//TODO: de adaugat flow pt join la joc
//TODO: de adaugat business logic si adaugat camera in bd
//TODO: add delete room after 15 mins or some timer
class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding
    private lateinit var viewModel: GameViewModel
    private lateinit var viewModelFactory: GameViewModelFactory
    private lateinit var usersAdapter: UserAdapter

    private lateinit var gameMode: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameBinding.inflate(inflater)

        gameMode = GameFragmentArgs.fromBundle(requireArguments()).gameMode

        viewModelFactory = GameViewModelFactory(this.requireActivity().application, gameMode)
        viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        usersAdapter = UserAdapter {
                Log.e("user", it)
        }
        binding.usersRecycler.adapter = usersAdapter
        binding.usersRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.EXTRA_LARGE), Orientation.HORIZONTAL, true)
        )
        binding.usersRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.MEDIUM), Orientation.VERTICAL, true)
        )
        viewModel.users.observe(viewLifecycleOwner) {
                users -> usersAdapter.submitList(users)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {
            viewModel.addUser()
        }

        viewModel.room.observe(viewLifecycleOwner) {
            viewModel.addRoom()
        }

        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                view?.findNavController()?.navigate(
                    GameFragmentDirections.actionGameFragmentToHomeFragment()
                )
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)


        return binding.root
    }
}