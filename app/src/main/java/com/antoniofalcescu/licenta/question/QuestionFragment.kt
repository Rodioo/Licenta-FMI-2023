package com.antoniofalcescu.licenta.question

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.databinding.FragmentQuestionBinding
import com.antoniofalcescu.licenta.game.*
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_TIME_TO_GUESS: Long = 10 * 1000
private const val LEADERBOARD_SHOW_TIME: Long = 4 * 1000

//TODO: de facut request-urile pentru melodii si de la most listened songs/artists
//TODO: de reparat bug zice +0 puncte la primul jucator care raspunde
//TODO: posibil sa trebuiasca adaugat flag in firebase la fel cu doneLoading sau cv de genul
//TODO: buggy si la screen-ul de asteptare ca toti sa raspunda
class QuestionFragment : Fragment() {

    private lateinit var binding: FragmentQuestionBinding
    private lateinit var viewModel: QuestionViewModel
    private lateinit var viewModelFactory: QuestionViewModelFactory

    private lateinit var questionAnswerAdapter: QuestionAnswerAdapter
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    private lateinit var gameRoom: GameRoom

    private var mediaPlayer = MediaPlayer()
    private val songHandler = Handler(Looper.getMainLooper())

    private var isFocused = true
    private var points = 0
    private var currentQuestionIndex = 0
    private lateinit var questions: List<Question>

    private var questionTimer: CountDownTimer? = null
    private var leaderboardTimer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestionBinding.inflate(inflater)

        gameRoom =  QuestionFragmentArgs.fromBundle(requireArguments()).gameRoom

        viewModelFactory = QuestionViewModelFactory(this.requireActivity().application, gameRoom)
        viewModel = ViewModelProvider(this, viewModelFactory)[QuestionViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.questionAnswersRecycler.addItemDecoration(
                RecyclerViewSpacing(requireContext().getSpacing(Spacing.LARGE), Orientation.VERTICAL)
        )
        binding.leaderboardRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.SMALL), Orientation.VERTICAL)
        )

        viewModel.gameRoom.observe(viewLifecycleOwner) {gameRoom ->
            if (gameRoom.doneLoading && (viewModel.gameQuestions.value == null || viewModel.gameQuestions.value!!.questions.isEmpty())) {
                viewModel.getQuestionsFromRoom()
            }
        }

        viewModel.gameQuestions.observe(viewLifecycleOwner) { gameQuestions ->
            if (gameQuestions.questions.isNotEmpty()) {
                questions = gameQuestions.questions
                showQuestionView(true)
            } else {
                showQuestionView(false)
            }
        }

        viewModel.userHasAnswered.observe(viewLifecycleOwner) { userHasAnswered ->
            if (userHasAnswered) {
                showLeaderboardView(true)
            }
        }

        questionTimer = object : CountDownTimer(MAX_TIME_TO_GUESS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000

                points = (millisUntilFinished / 10).toInt()

                if (secondsRemaining == 10L) {
                    binding.guessTimeLeftText.text = "0:$secondsRemaining"
                } else {
                    binding.guessTimeLeftText.text = "0:0$secondsRemaining"
                }
            }

            override fun onFinish() {
                points = 0
                mediaPlayer.pause()
                userAnswered("")
            }
        }

        binding.returnGameButton.setOnClickListener {
            viewModel.restartRoom(
                onSuccess = {
                    view?.findNavController()?.navigate(
                        QuestionFragmentDirections.actionQuestionFragmentToGameFragment(viewModel.gameRoom.value!!)
                    )
                },
                onFailure = {
                    view?.findNavController()?.navigate(
                        QuestionFragmentDirections.actionQuestionFragmentToHomeFragment()
                    )
                }
            )
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        viewModel.criticalError.observe(viewLifecycleOwner) { criticalError ->
            Toast.makeText(requireContext(), criticalError, Toast.LENGTH_LONG).show()
            viewModel.restartRoom(
                onSuccess = {
                    view?.findNavController()?.navigate(
                        QuestionFragmentDirections.actionQuestionFragmentToGameFragment(viewModel.gameRoom.value!!)
                    )
                },
                onFailure = {
                    view?.findNavController()?.navigate(
                        QuestionFragmentDirections.actionQuestionFragmentToHomeFragment()
                    )
                }
            )
        }

        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.currentUser.observe(viewLifecycleOwner) {
                    viewModel.leaveRoom()
                }
                view?.findNavController()?.navigate(
                    QuestionFragmentDirections.actionQuestionFragmentToHomeFragment()
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)

        return binding.root
    }

    private fun playSongSample(previewUrl: String) {
        mediaPlayer.apply {
            reset()
            questionTimer?.cancel()
            setOnPreparedListener { mp ->
                mp.start()
                questionTimer?.start()
                if (!isFocused) {
                    mp.setVolume(0f, 0f)
                } else {
                    mp.setVolume(1f, 1f)
                }
            }
            setOnErrorListener { mp, _, _ ->
                questionTimer?.cancel()
                Log.e("MediaPlayer", "Error occurred during preparation")
                false
            }
            try {
                setDataSource(previewUrl)
                prepare()
            } catch (e: Exception) {
                Log.e("MediaPlayer", "Error setting data source: ${e.message}")
            }
        }
    }

    private fun playNextQuestion() {
        showLeaderboardView(false)

        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            viewModel.syncQuestion(question)

            questionAnswerAdapter = QuestionAnswerAdapter {
                questionTimer?.cancel()
                mediaPlayer.pause()
                userAnswered(it)
            }
            binding.questionAnswersRecycler.adapter = questionAnswerAdapter

            //TODO: bug aici ca dupa ce nu raspunzi la o melodie urmatoarea melodie o sa apara altceva pt o secunda si dupa revine la normal
            viewModel.getQuestionsProfileZipped(question) {
                questionAnswerAdapter.submitList(viewModel.questionsProfileZipped.value!!)
            }

            playSongSample(question.previewUrl)

            questionTimer?.start()
        }
    }

    private fun showQuestionView(isVisible: Boolean) {
        binding.songsLoading.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.waitText.text = resources.getString(R.string.please_wait_while_we_load_the_songs)
        binding.startingSoonView.visibility = if (isVisible) View.VISIBLE else View.GONE
        if (isVisible) {
            val timer = object : CountDownTimer(3 * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    binding.startTimerText.text = secondsRemaining.toString()
                }

                override fun onFinish() {
                    binding.startingSoonView.visibility = View.GONE
                    binding.questionView.visibility = View.VISIBLE
                    playNextQuestion()
                }
            }
            timer.start()
        } else {
            songHandler.removeCallbacksAndMessages(null)
            mediaPlayer.pause()
            binding.questionView.visibility = View.GONE
        }
    }

    private fun showLeaderboardView(isVisible: Boolean) {
        if (isVisible) {
            val leaderboardTimer = object : CountDownTimer(LEADERBOARD_SHOW_TIME, 1000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    currentQuestionIndex++
                    if (currentQuestionIndex < questions.size) {
                        playNextQuestion()
                    } else {
                        viewModel.viewModelScope.launch {
                            binding.songView.visibility = View.GONE
                            binding.leaderboardRecycler.visibility = View.GONE
                            delay(500L)
                            binding.newGameView.visibility = View.VISIBLE
                            delay(100L)
                            binding.winnerView.visibility = View.VISIBLE
                            delay(500L)
                            binding.fullLeaderboardText.visibility = View.VISIBLE
                            binding.leaderboardRecycler.visibility = View.VISIBLE
                            binding.returnGameButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
            val question = questions[currentQuestionIndex]

            binding.questionView.visibility = View.GONE
            binding.songsLoading.visibility = View.VISIBLE
            binding.waitText.text = resources.getString(R.string.please_wait_for_everybody_to_finish_guessing)

            var hasRanOnce = false

            viewModel.everybodyAnswered.observe(viewLifecycleOwner) {everybodyAnswered ->
                if (everybodyAnswered && !hasRanOnce) {
                    hasRanOnce = true
                    binding.songsLoading.visibility = View.GONE
                    binding.leaderboardView.visibility = View.VISIBLE

                    leaderboardAdapter = LeaderboardAdapter()
                    binding.leaderboardRecycler.adapter = leaderboardAdapter

                    viewModel.getLeaderboardZipped(question) {
                        leaderboardAdapter.submitList(viewModel.leaderboardZipped.value!!)
                    }

                    leaderboardTimer.start()
                }
            }
        } else {
            binding.leaderboardView.visibility = View.GONE
            binding.questionView.visibility = View.VISIBLE
        }
    }

    private fun userAnswered(answer: String) {
        val pointsToInsert = if (questions[currentQuestionIndex].correctAnswer == answer) {
            points
        } else {
            0
        }

        viewModel.onUserAnswer(questions[currentQuestionIndex], pointsToInsert)
    }

    override fun onStart() {
        super.onStart()
        isFocused = true
        mediaPlayer.setVolume(1f, 1f)
    }

    override fun onStop() {
        super.onStop()
        isFocused = false
        mediaPlayer.setVolume(0f, 0f)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.leaveRoom()
        mediaPlayer.release()
    }
}