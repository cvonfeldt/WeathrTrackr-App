package com.csci448.cvonfeldt.cvonfeldt_a4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel


@Composable
fun AboutScreen( // info screen displaying app details and usage instructions
    modifier: Modifier = Modifier,
    weatherViewModel: IWeatherViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title and developer information
        Text(
            text = "WeathrTrackr",
            style = MaterialTheme.typography.headlineLarge
        )

        // version information section
        Text(
            text = "Version",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        //usage instructions section
        Text(
            text = "How to Use",
            style = MaterialTheme.typography.titleLarge,
            modifier =Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "This app allows you to track weather conditions at different locations. " +
                    "Press the Floating Action Button (FAB) to check in at your current location. " +
                    "Your location will be plotted on the map with a marker. " +
                    "\n\nTapping on a marker will show when you checked in and what the weather " +
                    "was like at that time. You can delete checkpoints from either the map or " +
                    "history view.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}