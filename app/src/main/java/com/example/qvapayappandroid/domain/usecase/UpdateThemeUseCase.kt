package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.SettingsRepository

class UpdateThemeUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(theme: String) {
        settingsRepository.updateTheme(theme)
    }
}