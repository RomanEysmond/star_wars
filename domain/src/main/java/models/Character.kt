package com.example.starwars.domain.models

data class Character(
    val id: Int,
    val name: String,
    val height: String,
    val mass: String,
    val hairColor: String,
    val eyeColor: String,
    val birthYear: String,
    val gender: String,
    val homeworld: Planet?,
    val films: List<Int>
)

data class Planet(
    val id: Int,
    val name: String,
    val climate: String,
    val terrain: String,
    val population: String
)