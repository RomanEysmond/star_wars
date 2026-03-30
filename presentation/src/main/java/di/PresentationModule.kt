package com.example.starwars.presentation.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {


    @Provides
    @Singleton
    fun provideBaseUrl(): String {
        return "https://swapi.dev/api/"
    }

    // Пример предоставления зависимости для презентационного слоя
    @Provides
    @Singleton
    fun provideCoroutineDispatcher(): CoroutineDispatcherProvider {
        return CoroutineDispatcherProvider()
    }
}

// Вспомогательный класс для предоставления диспетчеров корутин
class CoroutineDispatcherProvider {
    val main = kotlinx.coroutines.Dispatchers.Main
    val io = kotlinx.coroutines.Dispatchers.IO
    val default = kotlinx.coroutines.Dispatchers.Default
    val unconfined = kotlinx.coroutines.Dispatchers.Unconfined
}