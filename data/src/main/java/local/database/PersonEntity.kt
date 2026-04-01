package com.example.starwars.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: String,
    val mass: String,
    val hairColor: String,
    val eyeColor: String,
    val birthYear: String,
    val gender: String,
    val homeworldId: Int,
    val films: String,
    val species: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

