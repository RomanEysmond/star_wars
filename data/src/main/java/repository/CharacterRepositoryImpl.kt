package com.example.starwars.data.repository

import com.example.starwars.data.local.database.AppDatabase
import com.example.starwars.data.local.database.PersonEntity
import com.example.starwars.data.local.database.PlanetEntity
import com.example.starwars.data.remote.api.StarWarsApi
import com.example.starwars.domain.models.Character
import com.example.starwars.domain.models.Planet
import com.example.starwars.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val api: StarWarsApi,
    private val database: AppDatabase
) : CharacterRepository {

    override fun getAllCharacters(): Flow<List<Character>> {
        return database.personDao().getAllPersons().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchAndCacheCharacters(): Result<Unit> {
        return try {
            val response = api.getAllPeople(page = 1)
            val persons = response.results.map { person ->
                val id = extractIdFromUrl(person.url)
                PersonEntity(
                    id = id,
                    name = person.name,
                    height = person.height,
                    mass = person.mass,
                    hairColor = person.hair_color,
                    eyeColor = person.eye_color,
                    birthYear = person.birth_year,
                    gender = person.gender,
                    homeworldId = extractIdFromUrl(person.homeworld),
                    films = person.films.joinToString(","),
                    species = person.species.joinToString(",")
                )
            }
            database.personDao().insertAllPersons(persons)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCharacterDetail(characterId: Int): Result<Character> {
        return try {
            val personEntity = database.personDao().getPersonById(characterId)
            if (personEntity != null) {
                val planet = if (personEntity.homeworldId > 0) {
                    database.planetDao().getPlanetById(personEntity.homeworldId)?.toDomain()
                } else null
                val character = personEntity.toDomain().copy(homeworld = planet)
                Result.success(character)
            } else {
                val response = api.getPersonDetails(characterId)
                val planetEntity = fetchAndCachePlanet(extractIdFromUrl(response.homeworld))
                val character = Character(
                    id = characterId,
                    name = response.name,
                    height = response.height,
                    mass = response.mass,
                    hairColor = response.hair_color,
                    eyeColor = response.eye_color,
                    birthYear = response.birth_year,
                    gender = response.gender,
                    homeworld = planetEntity?.toDomain(),
                    films = response.films.map { extractIdFromUrl(it) }
                )
                cacheCharacter(character)
                Result.success(character)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun fetchAndCachePlanet(planetId: Int): PlanetEntity? {
        return try {
            val response = api.getPlanet(planetId)
            val planetEntity = PlanetEntity(
                id = planetId,
                name = response.name,
                climate = response.climate,
                terrain = response.terrain,
                population = response.population
            )
            database.planetDao().insertPlanet(planetEntity)
            planetEntity
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun cacheCharacter(character: Character) {
        val entity = PersonEntity(
            id = character.id,
            name = character.name,
            height = character.height,
            mass = character.mass,
            hairColor = character.hairColor,
            eyeColor = character.eyeColor,
            birthYear = character.birthYear,
            gender = character.gender,
            homeworldId = character.homeworld?.id ?: 0,
            films = character.films.joinToString(","),
            species = ""
        )
        database.personDao().insertPerson(entity)
    }

    private fun extractIdFromUrl(url: String): Int {
        return try {
            url.trimEnd('/').split("/").last().toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun PersonEntity.toDomain(): Character {
        return Character(
            id = id,
            name = name,
            height = height,
            mass = mass,
            hairColor = hairColor,
            eyeColor = eyeColor,
            birthYear = birthYear,
            gender = gender,
            homeworld = null,
            films = films.split(",").filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        )
    }

    private fun PlanetEntity.toDomain(): Planet {
        return Planet(
            id = id,
            name = name,
            climate = climate,
            terrain = terrain,
            population = population
        )
    }
}