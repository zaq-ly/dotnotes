package com.dotnotes.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dotnotes.app.ui.screens.editor.NoteEditorScreen
import com.dotnotes.app.ui.screens.history.HistoryScreen
import com.dotnotes.app.ui.screens.notelist.NoteListScreen
import com.dotnotes.app.ui.screens.settings.SettingsScreen
import com.dotnotes.app.ui.screens.splash.SplashScreen
import androidx.compose.animation.fadeOut

object Routes {
    const val SPLASH = "splash"
    const val NOTE_LIST = "note_list"
    const val NOTE_EDITOR = "note_editor/{noteId}"
    const val NOTE_EDITOR_NEW = "note_editor/new"
    const val SETTINGS = "settings"
    const val HISTORY = "history"

    fun noteEditor(noteId: String) = "note_editor/$noteId"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(
            Routes.SPLASH,
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.NOTE_LIST) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.NOTE_LIST) {
            NoteListScreen(
                onNoteClick = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                onNewNote = { navController.navigate(Routes.NOTE_EDITOR_NEW) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) }
            )
        }
        composable(
            Routes.NOTE_EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditorScreen(
                noteId = if (noteId == "new") null else noteId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onNoteClick = { noteId -> navController.navigate(Routes.noteEditor(noteId)) }
            )
        }
    }
}
