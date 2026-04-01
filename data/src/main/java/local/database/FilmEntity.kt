package com.example.starwars.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwars.domain.models.Film

@Entity(tableName = "films")
data class FilmEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val episodeId: Int,
    val releaseDate: String,
    val director: String
) {
    fun toDomain() = Film(id, title, episodeId, releaseDate, director)
}