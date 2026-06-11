package com.mdzeviatau.todoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

@Composable
fun AppNavHost(navController: NavHostController) {
     NavHost(
         navController = navController,
         startDestination = TaskListDestination
     ) {
         // Ekran 1: Lista Zadań
         composable<TaskListDestination> {

         }

         // Ekran 2: Szczegóły / Edycja Zadania
         composable<TaskDetailDestination> { backStackEntry ->
             val route: TaskDetailDestination = backStackEntry.toRoute()
             val taskId = route.taskId
         }

         // Ekran 3: Ustawienia
         composable<SettingsDestination> {

         }
     }
 }