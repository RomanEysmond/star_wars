package com.example.starwars.data.repository

import android.util.Log
import com.example.starwars.data.local.database.AppDatabase
import com.example.starwars.data.local.database.FilmEntity
import com.example.starwars.data.local.database.PersonEntity
import com.example.starwars.data.local.database.PlanetEntity
import com.example.starwars.data.remote.api.StarWarsApi
import com.example.starwars.domain.models.Character
import com.example.starwars.domain.models.Film
import com.example.starwars.domain.models.Planet
import com.example.starwars.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val api: StarWarsApi,
    private val database: AppDatabase
) : CharacterRepository {

    companion object {
        private const val TAG = "CharacterRepository"
    }

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
                Log.d(TAG, "Loading character from cache: $characterId")

                val character = loadCharacterFromCache(personEntity)
                Result.success(character)
            } else {
                Log.d(TAG, "Loading character from API: $characterId")

                val character = loadCharacterFromApi(characterId)

                cacheCharacter(character)

                Result.success(character)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading character: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }


    private suspend fun loadCharacterFromCache(personEntity: PersonEntity): Character {
        val planet = if (personEntity.homeworldId > 0) {
            database.planetDao().getPlanetById(personEntity.homeworldId)?.toDomain()
                ?: fetchAndCachePlanet(personEntity.homeworldId)?.toDomain()
        } else null

        val filmIdentifiers = if (personEntity.films.isNotEmpty()) {
            personEntity.films.split(",").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        Log.d(TAG, "Film identifiers from cache: $filmIdentifiers")

        val films = loadFilmsWithCache(filmIdentifiers)

        return Character(
            id = personEntity.id,
            name = personEntity.name,
            height = personEntity.height,
            mass = personEntity.mass,
            hairColor = personEntity.hairColor,
            eyeColor = personEntity.eyeColor,
            birthYear = personEntity.birthYear,
            gender = personEntity.gender,
            homeworld = planet,
            films = films
        )
    }


    private suspend fun loadCharacterFromApi(characterId: Int): Character = coroutineScope {
        Log.d(TAG, "Loading character $characterId from API with parallel requests")

        val characterResponse = api.getPersonDetails(characterId)

        val homeworldDeferred = async {
            fetchAndCachePlanet(extractIdFromUrl(characterResponse.homeworld))
        }

        val filmsDeferred = async {
            val filmIdentifiers = characterResponse.films
            Log.d(TAG, "Film URLs from API: $filmIdentifiers")

            if (filmIdentifiers.isNotEmpty()) {
                filmIdentifiers.map { filmUrl ->
                    async {
                        val filmId = extractIdFromUrl(filmUrl)
                        fetchAndCacheFilm(filmId)
                    }
                }.awaitAll().map { it.toDomain() }
            } else {
                emptyList()
            }
        }

        val planet = homeworldDeferred.await()
        val films = filmsDeferred.await()

        Log.d(TAG, "Loaded character $characterId with planet: ${planet?.name}, films: ${films.size}")

        Character(
            id = characterId,
            name = characterResponse.name,
            height = characterResponse.height,
            mass = characterResponse.mass,
            hairColor = characterResponse.hair_color,
            eyeColor = characterResponse.eye_color,
            birthYear = characterResponse.birth_year,
            gender = characterResponse.gender,
            homeworld = planet?.toDomain(),
            films = films
        )
    }


    private suspend fun fetchAndCacheFilm(filmId: Int): FilmEntity {
        Log.d(TAG, "Fetching film $filmId from API")
        val response = api.getFilm(filmId)
        val entity = FilmEntity(
            id = filmId,
            title = response.title,
            episodeId = response.episode_id,
            releaseDate = response.release_date,
            director = response.director
        )
        database.filmDao().insertFilm(entity)
        return entity
    }


    private suspend fun loadFilmsWithCache(filmIdentifiers: List<String>): List<Film> {
        if (filmIdentifiers.isEmpty()) return emptyList()

        val filmIds = filmIdentifiers.map { identifier ->
            identifier.toIntOrNull() ?: extractIdFromUrl(identifier)
        }

        Log.d(TAG, "Resolved film IDs: $filmIds")

        val cachedFilms = filmIds.mapNotNull { id ->
            database.filmDao().getFilmById(id)?.toDomain()
        }

        Log.d(TAG, "Cached films: ${cachedFilms.size} of ${filmIds.size}")

        if (cachedFilms.size == filmIds.size) {
            return cachedFilms.sortedBy { it.episodeId }
        }

        val missingIds = filmIds.filter { id ->
            database.filmDao().getFilmById(id) == null
        }

        Log.d(TAG, "Missing film IDs: $missingIds")

        val newFilms = missingIds.map { id ->
            try {
                fetchAndCacheFilm(id).toDomain()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading film $id: ${e.message}", e)
                Film(id, "Unknown Film", 0, "Unknown", "Unknown")
            }
        }

        return (cachedFilms + newFilms).sortedBy { it.episodeId }
    }

    private suspend fun fetchAndCachePlanet(planetId: Int): PlanetEntity? {
        return try {
            Log.d(TAG, "Fetching planet $planetId from API")
            val response = api.getPlanet(planetId)
            Log.d(TAG, "Planet response: ${response.name}")

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
            Log.e(TAG, "Error fetching planet $planetId: ${e.message}", e)
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
            films = character.films.joinToString(",") { it.id.toString() },
            species = ""
        )
        database.personDao().insertPerson(entity)

        character.films.forEach { film ->
            if (database.filmDao().getFilmById(film.id) == null) {
                database.filmDao().insertFilm(
                    FilmEntity(
                        id = film.id,
                        title = film.title,
                        episodeId = film.episodeId,
                        releaseDate = film.releaseDate,
                        director = film.director
                    )
                )
            }
        }
    }


    private fun extractIdFromUrl(url: String): Int {
        return try {
            url.trimEnd('/').split("/").last().toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting ID from URL: $url", e)
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
            films = emptyList()
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

    private fun FilmEntity.toDomain(): Film {
        return Film(
            id = id,
            title = title,
            episodeId = episodeId,
            releaseDate = releaseDate,
            director = director
        )
    }
}