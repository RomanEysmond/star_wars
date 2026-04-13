package com.example.starwars.presentation.ui.characterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwars.domain.models.Character
import com.example.starwars.domain.usecases.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(CharacterListUiState())
    val uiState: StateFlow<CharacterListUiState> = _uiState.asStateFlow()

    // Для совместимости с существующим кодом (опционально)
    val filteredCharacters: StateFlow<List<Character>> = _uiState
        .map { it.filteredCharacters }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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

    private var allCharacters = listOf<Character>()

    init {
        observeCharacters()
        // Автоматически загружаем данные при создании ViewModel
        loadCharacters()
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            getCharactersUseCase()
                .catch { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Unknown error"
                        )
                    }
                }
                .collect { characters ->
                    allCharacters = characters
                    _uiState.update { state ->
                        state.copy(
                            characters = characters,
                            filteredCharacters = characters,
                            isLoading = false,
                            isDataLoaded = true
                        )
                    }
                }
        }
    }

    fun loadCharacters() {
        // Если данные уже загружены, не загружаем снова
        if (_uiState.value.isDataLoaded && allCharacters.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getCharactersUseCase.refresh()
            result.onSuccess {
                // Данные будут автоматически получены через observeCharacters()
                // Не нужно обновлять isLoading здесь, так как observeCharacters() обновит его
            }.onFailure { exception ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = if (allCharacters.isEmpty()) {
                            "Не удалось загрузить данные. Проверьте подключение к интернету."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun refreshCharacters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getCharactersUseCase.refresh()
            result.onSuccess {
                // Данные будут автоматически обновлены через observeCharacters()
            }.onFailure { exception ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "Не удалось обновить данные. Проверьте подключение к интернету."
                    )
                }
            }
        }
    }

    fun filterCharacters(query: String) {
        val filtered = if (query.isEmpty()) {
            allCharacters
        } else {
            allCharacters.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(filteredCharacters = filtered) }
    }
}

// UI State класс
data class CharacterListUiState(
    val characters: List<Character> = emptyList(),
    val filteredCharacters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDataLoaded: Boolean = false
)