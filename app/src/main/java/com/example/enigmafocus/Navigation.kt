package com.example.enigmafocus

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.enigmafocus.ui.main.MainScreen
import com.example.enigmafocus.ui.main.MainScreenViewModel

@Composable
fun MainNavigation(viewModel: MainScreenViewModel = viewModel()) {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(viewModel = viewModel)
            }
        }
    )
}
