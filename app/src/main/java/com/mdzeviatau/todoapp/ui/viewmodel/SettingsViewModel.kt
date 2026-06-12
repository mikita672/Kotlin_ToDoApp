package com.mdzeviatau.todoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mdzeviatau.todoapp.data.repository.AppTheme
import com.mdzeviatau.todoapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    val themeState: StateFlow<AppTheme> = userPreferencesRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    val notificationsEnabledState: StateFlow<Boolean> = userPreferencesRepository.notificationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(theme)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateNotificationsEnabled(enabled)
        }
    }
}

class SettingsViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
