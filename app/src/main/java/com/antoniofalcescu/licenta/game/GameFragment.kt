package com.antoniofalcescu.licenta.game

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.antoniofalcescu.licenta.databinding.FragmentGameBinding
import com.antoniofalcescu.licenta.repository.CHECK_IF_USER_GOT_KICKED_FROM_ROOM_INTERVAL
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.ACCESS_TOKEN_REFRESH_INTERVAL
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing
import kotlinx.coroutines.launch

//TODO: cand fac feature-ul de a vizita profilul unui alt jucator din camera sa trimit si atunci codul camerei
//TODO: posibil un bug apare cand iesi prea repede (pana sa-si dea insert roomCode in bd locala te rebaga in camera veche, dar e relativ ok ca daca nu mai exista isi ia kick oricum)
class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding
    private lateinit var viewModel: GameViewModel
    private lateinit var viewModelFactory: GameViewModelFactory
    private lateinit var usersAdapter: UserAdapter

    private lateinit var gameRoom: GameRoom

    private val userLeftHandler = Handler(Looper.getMainLooper())

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

        viewModel.currentUser.observe(viewLifecycleOwner) {currentUser ->
            if (currentUser != null) {
                usersAdapter = UserAdapter(currentUser, viewModel) {
                    Log.e("user", it)
                }
                binding.usersRecycler.adapter = usersAdapter
            }
        }

        binding.usersRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.EXTRA_LARGE), Orientation.HORIZONTAL, true)
        )
        binding.usersRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.MEDIUM), Orientation.VERTICAL, true)
        )
        viewModel.users.observe(viewLifecycleOwner) {users ->
            if (::usersAdapter.isInitialized) {
                usersAdapter.submitList(users)
            }
        }

        viewModel.userIds.observe(viewLifecycleOwner) {userIds ->
            if (userIds.indexOf(viewModel.currentUser.value?.id_spotify) == 0) {
                binding.startGameButton.visibility = View.VISIBLE
            } else {
                binding.startGameButton.visibility = View.GONE
            }
            viewModel.getUsersProfiles()
        }

        binding.startGameButton.setOnClickListener {
            viewModel.startRoom()
        }

        viewModel.gameRoom.observe(viewLifecycleOwner) { gameRoom ->
            if (gameRoom.hasStarted) {
                view?.findNavController()?.navigate(
                    GameFragmentDirections.actionGameFragmentToQuestionFragment(gameRoom)
                )
            }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rejoinRoom().await()

            userLeftHandler.postDelayed(object : Runnable {
                override fun run() {
                    if (viewModel.checkIfUserLeft() == true) {
                        if (viewModel.userLeftMessage.value.isNullOrEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "You have been kicked",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        view?.findNavController()?.navigate(
                            GameFragmentDirections.actionGameFragmentToHomeFragment()
                        )
                    }
                    userLeftHandler.postDelayed(this, CHECK_IF_USER_GOT_KICKED_FROM_ROOM_INTERVAL)
                }
            }, CHECK_IF_USER_GOT_KICKED_FROM_ROOM_INTERVAL)
        }
    }

    override fun onStop() {
        super.onStop()

        userLeftHandler.removeCallbacksAndMessages(null)
        if (viewModel.gameRoom.value?.hasStarted == false ) {
            viewModel.leaveRoom()
        }
    }
}