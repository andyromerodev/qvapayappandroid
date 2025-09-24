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
        
        // On Android 13+, check the POST_NOTIFICATIONS permission
        val isGranted = if (isAndroid13Plus) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On Android < 13, notifications are enabled by default
            true
        }

        // Check whether app-wide notifications are enabled
        val canShowNotifications = NotificationManagerCompat.from(context).areNotificationsEnabled()

        // Check whether the specific channel is enabled
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
