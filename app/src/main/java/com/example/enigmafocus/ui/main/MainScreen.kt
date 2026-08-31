package com.example.enigmafocus.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.enigmafocus.data.AppStrings

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isEng = uiState.isEnglish

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF181818),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.HourglassBottom else Icons.Outlined.HourglassEmpty,
                            contentDescription = AppStrings.get("tab_focus", isEng)
                        )
                    },
                    label = { Text(AppStrings.get("tab_focus", isEng)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
                        indicatorColor = Color(0xFF1B3B2B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Apps else Icons.Outlined.Apps,
                            contentDescription = AppStrings.get("tab_apps", isEng)
                        )
                    },
                    label = { Text(AppStrings.get("tab_apps", isEng)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
                        indicatorColor = Color(0xFF1B3B2B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = AppStrings.get("tab_settings", isEng)
                        )
                    },
                    label = { Text(AppStrings.get("tab_settings", isEng)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
                        indicatorColor = Color(0xFF1B3B2B)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> FocusScreen(
                    state = uiState,
                    viewModel = viewModel,
                    onNavigateToSettings = { selectedTab = 2 }
                )
                1 -> AppsScreen(
                    state = uiState,
                    viewModel = viewModel
                )
                2 -> SettingsScreen(
                    state = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}
