package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.SettingsRepository

class UpdateBiometricUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateBiometric(enabled)
    }
}