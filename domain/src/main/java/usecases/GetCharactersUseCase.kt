package com.example.starwars.domain.usecases

import com.example.starwars.domain.models.Character
import com.example.starwars.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    operator fun invoke(): Flow<List<Character>> {
        return repository.getAllCharacters()
    }

    suspend fun refresh(): Result<Unit> {
        return repository.fetchAndCacheCharacters()
    }
}