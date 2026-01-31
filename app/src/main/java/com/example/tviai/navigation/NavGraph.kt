package com.example.tviai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tviai.AppContainer
import com.example.tviai.ui.screens.AnalysisScreen
import com.example.tviai.ui.screens.HistoryScreen
import com.example.tviai.ui.screens.InputScreen
import com.example.tviai.ui.screens.LasoScreen
import com.example.tviai.ui.screens.SettingsScreen
import com.example.tviai.viewmodel.TuViViewModel

sealed class Screen(val route: String) {
    object Input : Screen("input")
    object Laso : Screen("laso")
    object Analysis : Screen("analysis")
    object Settings : Screen("settings")
    object History : Screen("history")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: TuViViewModel,
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Input.route
    ) {
        composable(Screen.Input.route) {
            InputScreen(
                viewModel = viewModel,
                onCalculate = {
                    navController.navigate(Screen.Laso.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        
        composable(Screen.Laso.route) {
            LasoScreen(
                viewModel = viewModel,
                onNavigateToAnalysis = {
                    navController.navigate(Screen.Analysis.route)
                }
            )
        }
        
        composable(Screen.Analysis.route) {
            AnalysisScreen(viewModel = viewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                dataStore = appContainer.settingsDataStore,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.History.route) {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            HistoryScreen(
                repository = appContainer.historyRepository,
                onSelect = { laso ->
                    viewModel.setLaso(laso)
                    navController.navigate(Screen.Laso.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
