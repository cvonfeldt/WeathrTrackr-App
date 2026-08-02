package com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation.specs

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.csci448.cvonfeldt.cvonfeldt_a4.R
import com.csci448.cvonfeldt.cvonfeldt_a4.screens.AboutScreen
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel

object AboutScreenSpec : IScreenSpec {
    override val route = "about"
    override val arguments: List<NamedNavArgument> = emptyList()

    @Composable
    override fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        weatherViewModel: IWeatherViewModel,
        context: Context,
        coroutineScope: kotlinx.coroutines.CoroutineScope
    ) {
        AboutScreen(
            modifier = modifier,
            weatherViewModel = weatherViewModel
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun TopAppBarContent(
        weatherViewModel: IWeatherViewModel,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry?,
        context: Context
    ) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.nav_about)) },
            navigationIcon = {
                IconButton(onClick = { /* Open drawer */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        )
    }
}