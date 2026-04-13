package com.example.starwars.presentation.ui.characterdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.starwars.domain.models.Character
import com.example.starwars.presentation.R
import com.example.starwars.presentation.utils.AnimatedProgressBarManager
import com.example.starwars.presentation.utils.StarWarsPhrases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    private lateinit var animatedProgressManager: AnimatedProgressBarManager


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
        setupAnimatedProgressBar()
        setupRecyclerView()
        observeViewModel()

        val characterId = arguments?.getInt("characterId") ?: 0
        viewModel.loadCharacterDetails(characterId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animatedProgressManager.cleanup()
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

    private fun setupAnimatedProgressBar() {
        animatedProgressManager = AnimatedProgressBarManager(
            loadingContainer = loadingContainer,
            loadingPhraseText = loadingPhraseText,
            phrases = StarWarsPhrases.loadingPhrases
        )
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateUI(uiState)
                }
            }
        }
    }

    private fun updateUI(uiState: CharacterDetailUiState) {
        if (uiState.isLoading) {
            showFullScreenLoading()
        } else {
            hideFullScreenLoading()
        }

        uiState.character?.let { bindCharacterData(it) }

        if (uiState.isFilmsLoading) {
            showFilmsLoading()
        } else {
            hideFilmsLoading()
        }

        if (uiState.films.isNotEmpty()) {
            showFilmsList(uiState.films)
        } else if (!uiState.isFilmsLoading && uiState.character != null) {
            showEmptyFilms()
        }

        if (uiState.errorMessage != null) {
            showError(uiState.errorMessage)
        } else {
            hideError()
        }
    }

    private fun showFullScreenLoading() {
        contentLayout.isVisible = false
        errorView.isVisible = false
        animatedProgressManager.start()
    }

    private fun hideFullScreenLoading() {
        animatedProgressManager.stop()
        contentLayout.isVisible = true
    }

    private fun showError(error: String) {
        animatedProgressManager.stop()
        contentLayout.isVisible = false
        errorView.isVisible = true
        errorText.text = error
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