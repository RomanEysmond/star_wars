package com.example.starwars.domain.models

data class Planet(
    val id: Int,
    val name: String,
    val climate: String,
    val terrain: String,
    val population: String
)