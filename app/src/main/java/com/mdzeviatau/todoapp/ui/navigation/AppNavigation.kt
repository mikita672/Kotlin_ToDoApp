package com.mdzeviatau.todoapp.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mdzeviatau.todoapp.TodoApplication
import com.mdzeviatau.todoapp.ui.screens.SettingsScreen
import com.mdzeviatau.todoapp.ui.screens.TaskDetailScreen
import com.mdzeviatau.todoapp.ui.screens.TaskListScreen
import com.mdzeviatau.todoapp.ui.viewmodel.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    initialTaskId: String? = null,
    initialShortcutAction: String? = null,
    onIntentHandled: () -> Unit = {}
) {
    LaunchedEffect(initialTaskId, initialShortcutAction) {
        if (initialTaskId != null) {
            navController.navigate(TaskDetailDestination(initialTaskId))
            onIntentHandled()
        } else if (initialShortcutAction == "add_task") {
            navController.navigate(TaskDetailDestination())
            onIntentHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = TaskListDestination
    ) {
        composable<TaskListDestination> {
            val context = LocalContext.current
            val viewModel: TaskViewModel = viewModel(
                factory = TaskViewModelFactory((context.applicationContext as TodoApplication).repository)
            )

            TaskListScreen(
                viewModel = viewModel,
                onAddTaskClick = { navController.navigate(TaskDetailDestination()) },
                onTaskClick = { taskId -> navController.navigate(TaskDetailDestination(taskId)) },
                onSettingsClick = { navController.navigate(SettingsDestination) }
            )
        }

        composable<TaskDetailDestination> { backStackEntry ->
            val route: TaskDetailDestination = backStackEntry.toRoute()
            val context = LocalContext.current
            val viewModel: TaskViewModel = viewModel(
                factory = TaskViewModelFactory((context.applicationContext as TodoApplication).repository)
            )

            TaskDetailScreen(
                taskId = route.taskId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SettingsDestination> {
            val context = LocalContext.current
            val app = context.applicationContext as TodoApplication
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(app.userPreferencesRepository)
            )

            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
