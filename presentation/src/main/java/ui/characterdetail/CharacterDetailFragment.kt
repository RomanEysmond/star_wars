package com.example.starwars.presentation.ui.characterdetail

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.starwars.domain.models.Character
import com.example.starwars.presentation.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterDetailFragment : Fragment() {

    // Views
    private lateinit var contentLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
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

        // Загружаем данные
        val characterId = arguments?.getInt("characterId") ?: 0
        viewModel.loadCharacterDetails(characterId)
    }

    private fun initViews(view: View) {
        contentLayout = view.findViewById(R.id.contentLayout)
        progressBar = view.findViewById(R.id.progressBar)
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
        // Наблюдаем за основным состоянием загрузки (показываем на весь экран)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == true) {
                showFullScreenLoading()
            } else {
                hideFullScreenLoading()
            }
        }

        // Наблюдаем за данными персонажа
        viewModel.character.observe(viewLifecycleOwner) { character ->
            character?.let {
                bindCharacterData(it)
            }
        }

        // Наблюдаем за загрузкой фильмов (только индикатор внутри карточки)
        viewModel.isFilmsLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == true) {
                showFilmsLoading()
            } else {
                hideFilmsLoading()
            }
        }

        // Наблюдаем за списком фильмов
        viewModel.films.observe(viewLifecycleOwner) { films ->
            if (films.isNotEmpty()) {
                showFilmsList(films)
            } else if (viewModel.isFilmsLoading.value == false && viewModel.character.value != null) {
                showEmptyFilms()
            }
        }

        // Наблюдаем за ошибками
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showError(error)
            } else {
                hideError()
            }
        }
    }

    // Методы управления состояниями UI

    /**
     * Показывает полноэкранный прогресс-бар во время загрузки ВСЕХ данных
     */
    private fun showFullScreenLoading() {
        progressBar.isVisible = true      // Большой прогресс-бар на весь экран
        contentLayout.isVisible = false   // Скрываем весь контент
        errorView.isVisible = false       // Скрываем ошибку
    }

    /**
     * Скрывает полноэкранный прогресс-бар после полной загрузки
     */
    private fun hideFullScreenLoading() {
        progressBar.isVisible = false
        contentLayout.isVisible = true
    }

    private fun showError(error: String) {
        progressBar.isVisible = false
        contentLayout.isVisible = false
        errorView.isVisible = true
        errorText.text = error
    }

    private fun hideError() {
        errorView.isVisible = false
    }

    /**
     * Показывает маленький прогресс-бар внутри карточки фильмов
     */
    private fun showFilmsLoading() {
        filmsProgressBar.isVisible = true
        filmsRecyclerView.isVisible = false
        emptyFilmsText.isVisible = false
    }

    /**
     * Скрывает маленький прогресс-бар внутри карточки фильмов
     */
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

        // Отображаем домашнюю планету
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