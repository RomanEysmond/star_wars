package com.example.starwars.domain.usecases

import com.example.starwars.domain.models.Character
import com.example.starwars.domain.repository.CharacterRepository
import javax.inject.Inject

class GetCharacterDetailUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(characterId: Int): Result<Character> {
        return repository.getCharacterDetail(characterId)
    }
}