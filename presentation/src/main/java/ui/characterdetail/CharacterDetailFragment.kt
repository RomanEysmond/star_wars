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
import androidx.navigation.fragment.findNavController
import com.example.starwars.domain.models.Character
import com.example.starwars.presentation.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterDetailFragment : Fragment() {

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
    private lateinit var filmsList: TextView

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
        observeViewModel()

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
        filmsList = view.findViewById(R.id.filmsList)

        retryButton.setOnClickListener {
            val characterId = arguments?.getInt("characterId") ?: 0
            viewModel.loadCharacterDetails(characterId)
        }
    }

    private fun observeViewModel() {
        viewModel.character.observe(viewLifecycleOwner) { character ->
            character?.let {
                bindCharacterData(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == true) {
                progressBar.isVisible = true
                contentLayout.isVisible = false
                errorView.isVisible = false
            } else {
                progressBar.isVisible = false
                contentLayout.isVisible = true
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                errorView.isVisible = true
                errorText.text = error
                contentLayout.isVisible = false
            } else {
                errorView.isVisible = false
            }
        }
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

        if (character.films.isNotEmpty()) {
            filmsContainer.isVisible = true
            filmsList.text = "Appears in ${character.films.size} movies"
        } else {
            filmsContainer.isVisible = false
        }
    }
}