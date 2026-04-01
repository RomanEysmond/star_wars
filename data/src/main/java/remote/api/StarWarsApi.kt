package com.example.starwars.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StarWarsApi {
    @GET("people/")
    suspend fun getAllPeople(
        @Query("page") page: Int = 1
    ): PeopleResponse

    @GET("people/{personId}/")
    suspend fun getPersonDetails(
        @Path("personId") personId: Int
    ): PersonDetailResponse

    @GET("planets/{planetId}/")
    suspend fun getPlanet(
        @Path("planetId") planetId: Int
    ): PlanetResponse

    @GET("films/{filmId}/")
    suspend fun getFilm(
        @Path("filmId") filmId: Int
    ): FilmResponse
}

data class PeopleResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PersonResponse>
)

data class PersonResponse(
    val name: String,
    val height: String,
    val mass: String,
    val hair_color: String,
    val skin_color: String,
    val eye_color: String,
    val birth_year: String,
    val gender: String,
    val homeworld: String,
    val films: List<String>,
    val species: List<String>,
    val url: String
)

data class PersonDetailResponse(
    val name: String,
    val height: String,
    val mass: String,
    val hair_color: String,
    val skin_color: String,
    val eye_color: String,
    val birth_year: String,
    val gender: String,
    val homeworld: String,
    val films: List<String>,
    val species: List<String>,
    val url: String
)

data class PlanetResponse(
    val name: String,
    val rotation_period: String,
    val orbital_period: String,
    val diameter: String,
    val climate: String,
    val gravity: String,
    val terrain: String,
    val surface_water: String,
    val population: String,
    val url: String
)

data class FilmResponse(
    val title: String,
    val episode_id: Int,
    val opening_crawl: String,
    val director: String,
    val producer: String,
    val release_date: String,
    val characters: List<String>,
    val planets: List<String>,
    val starships: List<String>,
    val vehicles: List<String>,
    val species: List<String>,
    val created: String,
    val edited: String,
    val url: String
)