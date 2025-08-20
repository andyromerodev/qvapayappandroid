package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.data.permissions.NotificationPermissionManager
import kotlinx.coroutines.flow.Flow

class GetNotificationPermissionStatusUseCase(
    private val notificationPermissionManager: NotificationPermissionManager
) {
    operator fun invoke(): Flow<NotificationPermissionManager.NotificationPermissionStatus> {
        return notificationPermissionManager.permissionState
    }

    fun getCurrentStatus(): NotificationPermissionManager.NotificationPermissionStatus {
        return notificationPermissionManager.getNotificationPermissionStatus()
    }

    fun refreshStatus() {
        notificationPermissionManager.refreshPermissionStatus()
    }
}