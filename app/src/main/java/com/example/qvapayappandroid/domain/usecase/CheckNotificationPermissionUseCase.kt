package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.data.permissions.NotificationPermissionManager

class CheckNotificationPermissionUseCase(
    private val notificationPermissionManager: NotificationPermissionManager
) {
    data class PermissionCheckResult(
        val hasPermission: Boolean,
        val isRequired: Boolean,
        val permissionName: String?,
        val canShowNotifications: Boolean,
        val suggestions: List<String>
    )

    operator fun invoke(): PermissionCheckResult {
        val status = notificationPermissionManager.getNotificationPermissionStatus()
        val isRequired = notificationPermissionManager.isNotificationPermissionRequired()
        val permissionName = notificationPermissionManager.getRequiredPermission()

        val suggestions = mutableListOf<String>()
        
        if (!status.isGranted && isRequired) {
            suggestions.add("Conceder permiso de notificaciones en la app")
        }
        
        if (!status.canShowNotifications) {
            suggestions.add("Activar notificaciones en Configuración > Apps > QvaPay")
        }
        
        if (!status.isChannelEnabled) {
            suggestions.add("Habilitar el canal 'Alertas de Ofertas' en configuración de notificaciones")
        }

        return PermissionCheckResult(
            hasPermission = status.isFullyEnabled,
            isRequired = isRequired,
            permissionName = permissionName,
            canShowNotifications = status.canShowNotifications,
            suggestions = suggestions
        )
    }
}