package com.example.starwars.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.starwars.data.local.converters.Converters

@Database(
    entities = [
        PersonEntity::class,
        PlanetEntity::class,
        FilmEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun planetDao(): PlanetDao
    abstract fun filmDao(): FilmDao
}