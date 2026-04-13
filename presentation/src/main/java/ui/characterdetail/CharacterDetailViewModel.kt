package com.example.starwars.presentation.ui.characterdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwars.domain.models.Character
import com.example.starwars.domain.models.Film
import com.example.starwars.domain.usecases.GetCharacterDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase
) : ViewModel() {

    private val _character = MutableLiveData<Character?>()
    val character: LiveData<Character?> = _character

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _films = MutableLiveData<List<Film>>()
    val films: LiveData<List<Film>> = _films

    private val _isFilmsLoading = MutableLiveData<Boolean>()
    val isFilmsLoading: LiveData<Boolean> = _isFilmsLoading

    fun loadCharacterDetails(characterId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _isFilmsLoading.value = true
            _errorMessage.value = null

            val result = getCharacterDetailUseCase(characterId)

            result.onSuccess { character ->
                _character.value = character
                loadFilms(character.films)

            }.onFailure { exception ->
                _isLoading.value = false
                _isFilmsLoading.value = false
                _errorMessage.value = "Не удалось загрузить данные о персонаже. Проверьте подключение к интернету."
            }
        }
    }

    private fun loadFilms(filmsList: List<Film>) {
        viewModelScope.launch {
            if (filmsList.isEmpty()) {
                _films.value = emptyList()
                _isFilmsLoading.value = false
                _isLoading.value = false  //
                return@launch
            }

            kotlinx.coroutines.delay(300)

            _films.value = filmsList
            _isFilmsLoading.value = false
            _isLoading.value = false
        }
    }
}