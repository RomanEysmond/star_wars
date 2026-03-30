package com.example.starwars.domain.repository

import com.example.starwars.domain.models.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getAllCharacters(): Flow<List<Character>>
    suspend fun fetchAndCacheCharacters(): Result<Unit>
    suspend fun getCharacterDetail(characterId: Int): Result<Character>
}