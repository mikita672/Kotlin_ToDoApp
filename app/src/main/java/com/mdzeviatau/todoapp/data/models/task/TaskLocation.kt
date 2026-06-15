package com.mdzeviatau.todoapp.data.models.task

data class TaskLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)