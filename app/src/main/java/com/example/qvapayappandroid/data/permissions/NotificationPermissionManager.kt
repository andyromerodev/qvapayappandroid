package com.example.qvapayappandroid.data.permissions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationPermissionManager(private val context: Context) {

    private val _permissionState = MutableStateFlow(getNotificationPermissionStatus())
    val permissionState: Flow<NotificationPermissionStatus> = _permissionState.asStateFlow()

    data class NotificationPermissionStatus(
        val isGranted: Boolean,
        val isChannelEnabled: Boolean,
        val shouldShowRationale: Boolean = false,
        val canShowNotifications: Boolean = false
    ) {
        val isFullyEnabled: Boolean get() = isGranted && isChannelEnabled && canShowNotifications
    }

    fun getNotificationPermissionStatus(): NotificationPermissionStatus {
        val isAndroid13Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        
        // Para Android 13+, verificar permiso POST_NOTIFICATIONS
        val isGranted = if (isAndroid13Plus) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Para Android < 13, las notificaciones están habilitadas por defecto
            true
        }

        // Verificar si las notificaciones están habilitadas a nivel de app
        val canShowNotifications = NotificationManagerCompat.from(context).areNotificationsEnabled()

        // Verificar si el canal específico está habilitado
        val isChannelEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel("offer_alerts_channel")
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            true
        }

        return NotificationPermissionStatus(
            isGranted = isGranted,
            isChannelEnabled = isChannelEnabled,
            canShowNotifications = canShowNotifications
        )
    }

    fun refreshPermissionStatus() {
        _permissionState.value = getNotificationPermissionStatus()
    }

    fun isNotificationPermissionRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun getRequiredPermission(): String? {
        return if (isNotificationPermissionRequired()) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
    }

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        
        fun shouldRequestPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        }
    }
}