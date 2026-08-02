package com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation.specs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation.SettingsScreenSpec
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel

interface IScreenSpec {
    val route: String
    val arguments: List<NamedNavArgument>

    @Composable
    fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        weatherViewModel: IWeatherViewModel,
        context: Context,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    )

    @Composable
    fun TopAppBarContent(
        weatherViewModel: IWeatherViewModel,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry?,
        context: Context
    )

    companion object {
        const val ROOT = "root"
        private val mapScreen = MapScreenSpec
        private val historyScreen = HistoryScreenSpec
        private val settingsScreen = SettingsScreenSpec
        private val aboutScreen = AboutScreenSpec

        val startDestination = mapScreen.route

        val allScreens = mapOf(
            mapScreen.route to mapScreen,
            historyScreen.route to historyScreen,
            settingsScreen.route to settingsScreen,
            aboutScreen.route to aboutScreen
        )
    }
}