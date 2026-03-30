package com.example.starwars.presentation.ui.characterlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwars.domain.models.Character
import com.example.starwars.domain.usecases.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {

    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> = _characters

    private val _filteredCharacters = MutableLiveData<List<Character>>()
    val filteredCharacters: LiveData<List<Character>> = _filteredCharacters

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var allCharacters = listOf<Character>()

    init {
        observeCharacters()
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            getCharactersUseCase().collect { characters ->
                allCharacters = characters
                _characters.value = characters
                _filteredCharacters.value = characters
            }
        }
    }

    fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = getCharactersUseCase.refresh()
            result.onSuccess {
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
                _errorMessage.value = if (allCharacters.isEmpty()) {
                    "Не удалось загрузить данные. Проверьте подключение к интернету."
                } else {
                    null
                }
            }
        }
    }

    fun refreshCharacters() {
        loadCharacters()
    }

    fun filterCharacters(query: String) {
        if (query.isEmpty()) {
            _filteredCharacters.value = allCharacters
        } else {
            val filtered = allCharacters.filter {
                it.name.contains(query, ignoreCase = true)
            }
            _filteredCharacters.value = filtered
        }
    }
}