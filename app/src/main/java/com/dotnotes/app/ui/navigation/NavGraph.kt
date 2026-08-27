package com.dotnotes.app.ui.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dotnotes.app.ui.screens.editor.NoteEditorScreen
import com.dotnotes.app.ui.screens.notelist.NoteListScreen
import com.dotnotes.app.ui.screens.settings.SettingsScreen

object Routes {
    const val NOTE_LIST = "note_list"
    const val NOTE_EDITOR = "note_editor/{noteId}"
    const val NOTE_EDITOR_NEW = "note_editor/new"
    const val SETTINGS = "settings"

    fun noteEditor(noteId: String) = "note_editor/$noteId"
}

// Material 3 Emphasized Decelerate Curve for professional fluid motion
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.NOTE_LIST,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { (it * 0.22f).toInt() },
                animationSpec = tween(320, easing = EmphasizedDecelerate)
            ) + fadeIn(
                animationSpec = tween(280, easing = LinearOutSlowInEasing)
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(320, easing = EmphasizedDecelerate)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -(it * 0.12f).toInt() },
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(220)
            ) + scaleOut(
                targetScale = 0.97f,
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -(it * 0.12f).toInt() },
                animationSpec = tween(320, easing = EmphasizedDecelerate)
            ) + fadeIn(
                animationSpec = tween(280, easing = LinearOutSlowInEasing)
            ) + scaleIn(
                initialScale = 0.97f,
                animationSpec = tween(320, easing = EmphasizedDecelerate)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * 0.22f).toInt() },
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(220)
            ) + scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(Routes.NOTE_LIST) {
            NoteListScreen(
                onNoteClick = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                onNewNote = { navController.navigate(Routes.NOTE_EDITOR_NEW) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
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
    }
}
