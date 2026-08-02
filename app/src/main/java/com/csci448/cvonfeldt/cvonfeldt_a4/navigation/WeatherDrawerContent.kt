package com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.csci448.cvonfeldt.cvonfeldt_a4.R
import com.csci448.cvonfeldt.cvonfeldt_a4.presentation.navigation.specs.IScreenSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun WeatherDrawerContent(   // Composable that defines the navigation drawer's content and behavior
    navController: NavHostController,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope
) {
    ModalDrawerSheet {
        // add top padding to drawer content
        Spacer(modifier = Modifier.height(24.dp))

        // Create navigation items for all available screens
        IScreenSpec.allScreens.forEach { (route, screen) ->
            NavigationDrawerItem(
                label = {
                    // map route names to localized string resources
                    when (route) {
                        "map" -> Text(stringResource(R.string.nav_map))
                        "history" -> Text(stringResource(R.string.nav_history))
                        "settings"-> Text(stringResource(R.string.nav_settings))
                        "about" -> Text(stringResource(R.string.nav_about))
                        else -> Text(route)
                    }
                },
                  //highlight current screen in drawer
                selected = navController.currentBackStackEntry?.destination?.route == route,
                onClick = {
                    coroutineScope.launch {
                        // Close drawer and navigate to selected screen
                        drawerState.close()

                        navController.navigate(route) {
                            popUpTo(IScreenSpec.startDestination)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}