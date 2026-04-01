package com.example.starwars.domain.models

data class Film(
    val id: Int,
    val title: String,
    val episodeId: Int,
    val releaseDate: String,
    val director: String
)