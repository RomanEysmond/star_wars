package com.example.starwars.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.starwars.presentation.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Находим NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Устанавливаем граф навигации
        navController.setGraph(R.navigation.nav_graph)

        // Находим toolbar
        toolbar = findViewById(R.id.toolbar)

        // Устанавливаем toolbar как ActionBar
        setSupportActionBar(toolbar)

        // Настраиваем ActionBar с навигацией
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.characterListFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Связываем toolbar с NavController
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}