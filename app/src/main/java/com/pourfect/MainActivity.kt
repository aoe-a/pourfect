package com.pourfect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.net.Uri
import com.pourfect.domain.CustomRecipe
import com.pourfect.ui.BrewSessionViewModel
import com.pourfect.ui.brew.GuidedBrewScreen
import com.pourfect.ui.builder.RecipeBuilderScreen
import com.pourfect.ui.grinder.GrinderScreen
import com.pourfect.ui.home.HomeScreen
import com.pourfect.ui.journal.JournalScreen
import com.pourfect.ui.setup.BrewSetupScreen
import com.pourfect.ui.settings.SettingsScreen
import com.pourfect.ui.theme.PourfectTheme

object Routes {
    const val HOME = "home"
    const val SETUP = "setup/{preset}"
    const val SETUP_CUSTOM = "setupCustom/{recipe}"
    const val BUILDER = "builder"
    const val BREW = "brew"
    const val SETTINGS = "settings"
    const val JOURNAL = "journal"
    const val GRINDERS = "grinders"

    fun setup(preset: String) = "setup/$preset"
    fun setupCustom(recipe: CustomRecipe) = "setupCustom/${Uri.encode(recipe.encode())}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PourfectTheme {
                // Warm depth gradient behind every screen instead of flat black
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF191209),
                                    Color(0xFF0E0D0B),
                                    Color(0xFF0A0908)
                                )
                            )
                        )
                ) {
                    val navController = rememberNavController()
                    val session: BrewSessionViewModel = viewModel()

                    NavHost(navController = navController, startDestination = Routes.HOME) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                onPresetSelected = { presetId ->
                                    navController.navigate(Routes.setup(presetId))
                                },
                                onSettings = { navController.navigate(Routes.SETTINGS) },
                                onJournal = { navController.navigate(Routes.JOURNAL) },
                                onCreateRecipe = { navController.navigate(Routes.BUILDER) },
                                onCustomSelected = { recipe ->
                                    navController.navigate(Routes.setupCustom(recipe))
                                }
                            )
                        }
                        composable(Routes.BUILDER) {
                            RecipeBuilderScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Routes.SETUP_CUSTOM) { backStackEntry ->
                            val recipe = backStackEntry.arguments?.getString("recipe")
                                ?.let { CustomRecipe.decode(it) }
                            BrewSetupScreen(
                                presetId = "classic",
                                customRecipe = recipe,
                                session = session,
                                onStartBrew = { navController.navigate(Routes.BREW) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.SETUP) { backStackEntry ->
                            val presetId = backStackEntry.arguments?.getString("preset") ?: "classic"
                            BrewSetupScreen(
                                presetId = presetId,
                                session = session,
                                onStartBrew = { navController.navigate(Routes.BREW) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.BREW) {
                            GuidedBrewScreen(
                                session = session,
                                onExit = { navController.popBackStack(Routes.HOME, inclusive = false) }
                            )
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onGrinders = { navController.navigate(Routes.GRINDERS) }
                            )
                        }
                        composable(Routes.JOURNAL) {
                            JournalScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Routes.GRINDERS) {
                            GrinderScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
