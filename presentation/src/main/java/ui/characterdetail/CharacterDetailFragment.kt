package com.example.starwars.presentation.ui.characterdetail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.starwars.domain.models.Character
import com.example.starwars.presentation.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class CharacterDetailFragment : Fragment() {

    // Views
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingPhraseText: TextView
    private lateinit var loadingInfoText: TextView
    private lateinit var errorView: View
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button

    private lateinit var characterName: TextView
    private lateinit var heightValue: TextView
    private lateinit var massValue: TextView
    private lateinit var hairValue: TextView
    private lateinit var eyesValue: TextView
    private lateinit var birthYearValue: TextView
    private lateinit var genderValue: TextView
    private lateinit var homeworldCard: View
    private lateinit var homeworldName: TextView
    private lateinit var homeworldClimate: TextView
    private lateinit var homeworldTerrain: TextView
    private lateinit var homeworldPopulation: TextView
    private lateinit var noHomeworldText: TextView
    private lateinit var filmsContainer: View
    private lateinit var filmsRecyclerView: RecyclerView
    private lateinit var filmsProgressBar: ProgressBar
    private lateinit var emptyFilmsText: TextView

    private lateinit var filmAdapter: FilmAdapter
    private val viewModel: CharacterDetailViewModel by viewModels()

    private val starWarsPhrases = listOf(
        "СКАНИРОВАНИЕ ОКРУЖАЮЩЕГО ПРОСТРАНСТВА...",
        "ЗАТОЧКА СВЕТОВОГО МЕЧА...",
        "НАСТРОЙКА СИНХРОНИЗАТОРА...",
        "ПОИСК ИСТОЧНИКОВ СИЛЫ...",
        "СБОР РАЗВЕДДАННЫХ...",
        "ДЕШИФРОВКА СООБЩЕНИЯ ПОВСТАНЦЕВ...",
        "СКАНИРОВАНИЕ ОКРУЖАЮЩЕГО ПРОСТРАНСТВА...",
        "ЗАГРУЗКА БОЕВЫХ ПРОТОКОЛОВ..."
    )

    private var phraseIndex = 0
    private var handler = Handler(Looper.getMainLooper())
    private var phraseRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_character_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        observeViewModel()

        val characterId = arguments?.getInt("characterId") ?: 0
        viewModel.loadCharacterDetails(characterId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPhraseAnimation()
    }

    private fun initViews(view: View) {
        contentLayout = view.findViewById(R.id.contentLayout)
        loadingContainer = view.findViewById(R.id.loadingContainer)
        progressBar = view.findViewById(R.id.progressBar)
        loadingPhraseText = view.findViewById(R.id.loadingPhraseText)
        loadingInfoText = view.findViewById(R.id.loadingInfoText)
        errorView = view.findViewById(R.id.errorView)
        errorText = view.findViewById(R.id.errorText)
        retryButton = view.findViewById(R.id.retryButton)

        characterName = view.findViewById(R.id.characterName)
        heightValue = view.findViewById(R.id.heightValue)
        massValue = view.findViewById(R.id.massValue)
        hairValue = view.findViewById(R.id.hairValue)
        eyesValue = view.findViewById(R.id.eyesValue)
        birthYearValue = view.findViewById(R.id.birthYearValue)
        genderValue = view.findViewById(R.id.genderValue)
        homeworldCard = view.findViewById(R.id.homeworldCard)
        homeworldName = view.findViewById(R.id.homeworldName)
        homeworldClimate = view.findViewById(R.id.homeworldClimate)
        homeworldTerrain = view.findViewById(R.id.homeworldTerrain)
        homeworldPopulation = view.findViewById(R.id.homeworldPopulation)
        noHomeworldText = view.findViewById(R.id.noHomeworldText)
        filmsContainer = view.findViewById(R.id.filmsContainer)
        filmsRecyclerView = view.findViewById(R.id.filmsRecyclerView)
        filmsProgressBar = view.findViewById(R.id.filmsProgressBar)
        emptyFilmsText = view.findViewById(R.id.emptyFilmsText)

        retryButton.setOnClickListener {
            val characterId = arguments?.getInt("characterId") ?: 0
            viewModel.loadCharacterDetails(characterId)
        }
    }

    private fun setupRecyclerView() {
        filmAdapter = FilmAdapter()
        filmsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = filmAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            when (isLoading) {
                true -> showFullScreenLoading()
                false -> hideFullScreenLoading()
            }
        }

        viewModel.character.observe(viewLifecycleOwner) { character ->
            character?.let {
                bindCharacterData(it)
            }
        }

        viewModel.isFilmsLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == true) {
                showFilmsLoading()
            } else {
                hideFilmsLoading()
            }
        }

        viewModel.films.observe(viewLifecycleOwner) { films ->
            if (films.isNotEmpty()) {
                showFilmsList(films)
            } else if (viewModel.isFilmsLoading.value == false && viewModel.character.value != null) {
                showEmptyFilms()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showError(error)
            } else {
                hideError()
            }
        }
    }


    private fun startPhraseAnimation() {
        phraseIndex = 0
        loadingPhraseText.text = starWarsPhrases[phraseIndex]

        phraseRunnable = object : Runnable {
            override fun run() {
                val fadeOut = AlphaAnimation(1f, 0f).apply {
                    duration = 500
                }

                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = 500
                }

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        phraseIndex = (phraseIndex + 1) % starWarsPhrases.size
                        loadingPhraseText.text = starWarsPhrases[phraseIndex]
                        loadingPhraseText.startAnimation(fadeIn)
                    }
                })

                loadingPhraseText.startAnimation(fadeOut)
                handler.postDelayed(this, 3500)
            }
        }

        handler.post(phraseRunnable!!)
    }


    private fun stopPhraseAnimation() {
        phraseRunnable?.let {
            handler.removeCallbacks(it)
        }
        phraseRunnable = null
    }


    private fun showFullScreenLoading() {
        loadingContainer.isVisible = true
        contentLayout.isVisible = false
        errorView.isVisible = false
        startPhraseAnimation()
    }

    private fun hideFullScreenLoading() {
        loadingContainer.isVisible = false
        contentLayout.isVisible = true
        stopPhraseAnimation()
    }

    private fun showError(error: String) {
        loadingContainer.isVisible = false
        contentLayout.isVisible = false
        errorView.isVisible = true
        errorText.text = error
        stopPhraseAnimation()
    }

    private fun hideError() {
        errorView.isVisible = false
    }

    private fun showFilmsLoading() {
        filmsProgressBar.isVisible = true
        filmsRecyclerView.isVisible = false
        emptyFilmsText.isVisible = false
    }

    private fun hideFilmsLoading() {
        filmsProgressBar.isVisible = false
    }

    private fun showFilmsList(films: List<com.example.starwars.domain.models.Film>) {
        filmsProgressBar.isVisible = false
        filmsRecyclerView.isVisible = true
        emptyFilmsText.isVisible = false
        filmAdapter.submitList(films)
    }

    private fun showEmptyFilms() {
        filmsProgressBar.isVisible = false
        filmsRecyclerView.isVisible = false
        emptyFilmsText.isVisible = true
    }

    private fun bindCharacterData(character: Character) {
        characterName.text = character.name.uppercase()
        heightValue.text = "${character.height} cm"
        massValue.text = "${character.mass} kg"
        hairValue.text = character.hairColor.ifEmpty { "N/A" }
        eyesValue.text = character.eyeColor.ifEmpty { "N/A" }
        birthYearValue.text = character.birthYear
        genderValue.text = character.gender.uppercase()

        character.homeworld?.let { planet ->
            homeworldName.text = planet.name.uppercase()
            homeworldClimate.text = planet.climate.uppercase()
            homeworldTerrain.text = planet.terrain.uppercase()
            homeworldPopulation.text = planet.population
            homeworldCard.isVisible = true
            noHomeworldText.isVisible = false
        } ?: run {
            homeworldCard.isVisible = false
            noHomeworldText.isVisible = true
        }
    }
}