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
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.databinding.FragmentQuestionBinding
import com.antoniofalcescu.licenta.game.*
import com.antoniofalcescu.licenta.utils.Orientation
import com.antoniofalcescu.licenta.utils.RecyclerViewSpacing
import com.antoniofalcescu.licenta.utils.Spacing
import com.antoniofalcescu.licenta.utils.getSpacing

private const val MAX_TIME_TO_GUESS: Long = 10 * 1000

class QuestionFragment : Fragment() {

    private lateinit var binding: FragmentQuestionBinding
    private lateinit var viewModel: QuestionViewModel
    private lateinit var viewModelFactory: QuestionViewModelFactory

    private lateinit var questionAnswerAdapter: QuestionAnswerAdapter

    private lateinit var gameRoom: GameRoom

    private var mediaPlayer = MediaPlayer()
    private val songHandler = Handler(Looper.getMainLooper())

    private var isFocused = true

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

        questionAnswerAdapter = QuestionAnswerAdapter {}
        binding.questionAnswersRecycler.adapter = questionAnswerAdapter
        binding.questionAnswersRecycler.addItemDecoration(
            RecyclerViewSpacing(requireContext().getSpacing(Spacing.LARGE), Orientation.VERTICAL)
        )


        viewModel.gameRoom.observe(viewLifecycleOwner) {gameRoom ->
            if (gameRoom.doneLoading) {
                viewModel.getQuestionsFromRoom()
            }
        }

        viewModel.gameQuestions.observe(viewLifecycleOwner) { gameQuestions ->
            if (gameQuestions.questions.isNotEmpty()) {
                showQuestionView(true, gameQuestions.questions)
            } else {
                showQuestionView(false, emptyList())
            }
        }

        return binding.root
    }

    private fun playSongSample(previewUrl: String) {
        mediaPlayer.apply {
            reset()
            setOnPreparedListener { mp ->
                mp.start()
                if (!isFocused) {
                    mp.setVolume(0f, 0f)
                } else {
                    mp.setVolume(1f, 1f)
                }
            }
            setOnErrorListener { mp, _, _ ->
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

    private fun playNextQuestion(currentQuestionIndex: Int, questions: List<Question>) {
        val timer = object : CountDownTimer(MAX_TIME_TO_GUESS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                if (secondsRemaining == 10L) {
                    binding.guessTimeLeftText.text = "0:$secondsRemaining"
                } else {
                    binding.guessTimeLeftText.text = "0:0$secondsRemaining"
                }
            }

            override fun onFinish() {

            }
        }

        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]

            questionAnswerAdapter.submitList(question.answers)
            playSongSample(question.previewUrl)



            timer.start()

            songHandler.postDelayed({
                mediaPlayer.pause()
                val incrementedIndex = currentQuestionIndex + 1
                playNextQuestion(incrementedIndex, questions)
            }, MAX_TIME_TO_GUESS)
        }
    }

    private fun showQuestionView(isVisible: Boolean, questions: List<Question>) {
        binding.songsLoading.visibility = if (isVisible) View.GONE else View.VISIBLE
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
                    playNextQuestion(0, questions)
                }
            }
            timer.start()
        } else {
            songHandler.removeCallbacksAndMessages(null)
            mediaPlayer.pause()
            binding.questionView.visibility = View.GONE
        }
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

        songHandler.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }
}