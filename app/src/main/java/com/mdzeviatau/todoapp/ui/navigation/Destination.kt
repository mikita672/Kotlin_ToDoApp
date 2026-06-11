package com.mdzeviatau.todoapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object TaskListDestination
@Serializable
data class TaskDetailDestination(val taskId: String? =null)
@Serializable
data object SettingsDestination
