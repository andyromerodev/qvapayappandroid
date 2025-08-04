package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.SettingsRepository
import com.example.qvapayappandroid.presentation.ui.settings.UserSettings
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): UserSettings {
        return settingsRepository.getSettings()
    }
    
    fun flow(): Flow<UserSettings> {
        return settingsRepository.getSettingsFlow()
    }
}