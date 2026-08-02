package com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.csci448.cvonfeldt.cvonfeldt_a4.screens.*
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    weatherViewModel: IWeatherViewModel,
    context: Context,
    coroutineScope: CoroutineScope
) {

    // Initialize drawer state and coroutine scope for animations
    val drawerState =rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    //Setup navigation drawer with all available screens
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // nav drawer header and items
                Text("WeathrTrackr", style = MaterialTheme.typography.headlineMedium,modifier = Modifier.padding(16.dp))
                // Map screen navigation item
                NavigationDrawerItem(
                    label = { Text("Map") },
                    selected = navController.currentDestination?.route == "map",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("map")
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("History") },
                    selected = navController.currentDestination?.route == "history",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("history")
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = navController.currentDestination?.route == "settings",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("settings")
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = navController.currentDestination?.route == "about",
                    onClick ={
                        scope.launch {
                            drawerState.close()
                            navController.navigate("about")
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("WeathrTrackr") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                modifier = modifier.padding(paddingValues),
                navController =navController,
                startDestination = "map"
            ) {
                // Screen route definitions
                composable("map") {
                    MapScreen(
                        weatherViewModel = weatherViewModel,
                        coroutineScope = coroutineScope
                    )
                }
                composable("history") {
                    HistoryScreen(
                        weatherViewModel = weatherViewModel,
                        coroutineScope = coroutineScope
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        weatherViewModel = weatherViewModel,
                        coroutineScope = coroutineScope
                    )
                }

                composable("about") {
                    AboutScreen(
                        weatherViewModel = weatherViewModel
                    )
                }
            }
        }
    }
}

