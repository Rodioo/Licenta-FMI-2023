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
import com.antoniofalcescu.licenta.databinding.FragmentGameBinding
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing

//TODO: posibil sa adaug field isOwner in firestore cand creezi tu joc ca sa poti da kick la altii ingame
//TODO: verificat cand si dc da 401 cateodata random (posibil sa nu reinitalizeze bine token-ul)
//TODO: cand fac feature-ul de a vizita profilul unui alt jucator din camera sa trimit si atunci codul camerei
//TODO: de gandit cum sa arate UI-ul la quiz si de facut request-urile a.i sa primesc cel putin 10-15 intrebari cu sample != null
class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding
    private lateinit var viewModel: GameViewModel
    private lateinit var viewModelFactory: GameViewModelFactory
    private lateinit var usersAdapter: UserAdapter

    private lateinit var gameRoom: GameRoom
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater)

        gameRoom = GameFragmentArgs.fromBundle(requireArguments()).gameRoom

        viewModelFactory = GameViewModelFactory(this.requireActivity().application, gameRoom)
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
        viewModel.users.observe(viewLifecycleOwner) {users ->
            usersAdapter.submitList(users)
        }

        viewModel.userIds.observe(viewLifecycleOwner) {
            viewModel.getUsersProfiles()
        }

        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.currentUser.observe(viewLifecycleOwner) {
                    viewModel.leaveRoom()
                }
                view?.findNavController()?.navigate(
                    GameFragmentDirections.actionGameFragmentToHomeFragment()
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.gameRoomDao.observe(viewLifecycleOwner) {
            viewModel.rejoinRoom()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.leaveRoom()
    }
}