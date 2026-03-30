package com.example.starwars.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.starwars.data.local.converters.Converters
import com.example.starwars.data.local.database.PersonEntity
import com.example.starwars.data.local.database.PlanetEntity

@Database(
    entities = [PersonEntity::class, PlanetEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun planetDao(): PlanetDao
}