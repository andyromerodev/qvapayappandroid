package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.SettingsRepository

class InitializeSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.initializeDefaultSettings()
    }
}