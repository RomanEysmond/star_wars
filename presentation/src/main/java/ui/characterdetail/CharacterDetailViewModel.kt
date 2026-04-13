package com.example.starwars.presentation.ui.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwars.domain.models.Character
import com.example.starwars.domain.models.Film
import com.example.starwars.domain.usecases.GetCharacterDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterDetailUiState())
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()

    // Для совместимости с существующим кодом (опционально)
    val character: StateFlow<Character?> = _uiState
        .map { it.character }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isLoading: StateFlow<Boolean> = _uiState
        .map { it.isLoading }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val errorMessage: StateFlow<String?> = _uiState
        .map { it.errorMessage }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val films: StateFlow<List<Film>> = _uiState
        .map { it.films }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isFilmsLoading: StateFlow<Boolean> = _uiState
        .map { it.isFilmsLoading }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun loadCharacterDetails(characterId: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    isFilmsLoading = true,
                    errorMessage = null
                )
            }

            val result = getCharacterDetailUseCase(characterId)

            result.onSuccess { character ->
                _uiState.update { state ->
                    state.copy(
                        character = character,
                        isLoading = false
                    )
                }
                loadFilms(character.films)
            }.onFailure { exception ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isFilmsLoading = false,
                        errorMessage = "Не удалось загрузить данные о персонаже. Проверьте подключение к интернету."
                    )
                }
            }
        }
    }

    private fun loadFilms(filmsList: List<Film>) {
        viewModelScope.launch {
            if (filmsList.isEmpty()) {
                _uiState.update { state ->
                    state.copy(
                        films = emptyList(),
                        isFilmsLoading = false,
                        isLoading = false
                    )
                }
                return@launch
            }

            kotlinx.coroutines.delay(300)

            _uiState.update { state ->
                state.copy(
                    films = filmsList,
                    isFilmsLoading = false,
                    isLoading = false
                )
            }
        }
    }
}

// UI State класс
data class CharacterDetailUiState(
    val character: Character? = null,
    val films: List<Film> = emptyList(),
    val isLoading: Boolean = false,
    val isFilmsLoading: Boolean = false,
    val errorMessage: String? = null
)