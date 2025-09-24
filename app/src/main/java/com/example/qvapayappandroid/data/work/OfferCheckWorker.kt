package com.example.qvapayappandroid.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qvapayappandroid.MainActivity
import com.example.qvapayappandroid.R
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.permissions.NotificationPermissionManager
import com.example.qvapayappandroid.domain.model.OfferAlert
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OfferCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val offerAlertRepository: OfferAlertRepository by inject()
    private val p2pRepository: P2PRepository by inject()
    private val sessionRepository: SessionRepository by inject()

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "offer_alerts_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Alertas de Ofertas"
        const val WORK_NAME = "offer_check_work"
    }

    override suspend fun doWork(): Result {
        return try {
            // Check if there are any active alerts
            val activeAlerts = offerAlertRepository.getActiveAlerts().first()
            
            if (activeAlerts.isEmpty()) {
                // No alerts to process, nothing else to do
                return Result.success()
            }

            // Ensure there is an active session
            val accessToken = sessionRepository.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                // Without a session we cannot make API calls
                return Result.success()
            }

            // Evaluate each alert
            activeAlerts.forEach { alert ->
                checkAlertCriteria(alert, accessToken)
            }

            Result.success()
        } catch (e: Exception) {
            // Log the error and retry later
            android.util.Log.e("OfferCheckWorker", "Error checking offers", e)
            Result.retry()
        }
    }

    private suspend fun checkAlertCriteria(alert: OfferAlert, accessToken: String) {
        try {
            // Build the filter request based on the alert criteria
            val filterRequest = P2PFilterRequest(
                type = if (alert.offerType == "both") null else alert.offerType,
                coin = alert.coinType,
                min = alert.minAmount,
                max = alert.maxAmount,
                vip = if (alert.onlyVip) true else null,
                page = 1,
                perPage = 50 // Limit to avoid hitting rate limits
            )
            
            // Fetch the offers that match the requested filters
            val offerResponse = p2pRepository.getP2POffers(filterRequest, accessToken).getOrNull()

            offerResponse?.data?.forEach { offer ->
                if (doesOfferMatchCriteria(offer, alert)) {
                    // The offer meets the criteria—send a notification
                    sendOfferNotification(alert, offer)
                    
                    // Update the last-triggered timestamp
                    offerAlertRepository.updateLastTriggeredAt(alert.id, System.currentTimeMillis())
                }
            }

            // Update the last-checked timestamp
            offerAlertRepository.updateLastCheckedAt(alert.id, System.currentTimeMillis())

        } catch (e: Exception) {
            android.util.Log.e("OfferCheckWorker", "Error checking alert ${alert.id}", e)
        }
    }

    private fun doesOfferMatchCriteria(offer: P2POffer, alert: OfferAlert): Boolean {
        // Enforce offer type
        if (alert.offerType != "both" && offer.type != alert.offerType) {
            return false
        }

        // Enforce coin filter
        if (offer.coin != alert.coinType) {
            return false
        }

        // Enforce amount boundaries
        val offerAmount = offer.amount?.toDoubleOrNull() ?: return false
        alert.minAmount?.let { min ->
            if (offerAmount < min) return false
        }
        alert.maxAmount?.let { max ->
            if (offerAmount > max) return false
        }

        // Enforce rate comparison
        val offerRate = offer.receive?.toDoubleOrNull() ?: return false
        when (alert.rateComparison) {
            "greater" -> if (offerRate <= alert.targetRate) return false
            "less" -> if (offerRate >= alert.targetRate) return false
            "equal" -> if (kotlin.math.abs(offerRate - alert.targetRate) > 0.01) return false
        }

        // Require KYC when needed
        if (alert.onlyKyc && offer.onlyKyc != 1) {
            return false
        }

        // Require VIP status when needed  
        if (alert.onlyVip && offer.onlyVip != 1) {
            return false
        }

        return true
    }

    private fun sendOfferNotification(alert: OfferAlert, offer: P2POffer) {
        // Check notification permissions before sending anything
        val permissionManager = NotificationPermissionManager(applicationContext)
        val permissionStatus = permissionManager.getNotificationPermissionStatus()
        
        if (!permissionStatus.isFullyEnabled) {
            android.util.Log.w(
                "OfferCheckWorker", 
                "Cannot send notification - permissions not granted. " +
                "Granted: ${permissionStatus.isGranted}, " +
                "Channel enabled: ${permissionStatus.isChannelEnabled}, " +
                "Can show: ${permissionStatus.canShowNotifications}"
            )
            return
        }

        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("offer_id", offer.uuid)
            putExtra("navigate_to", "p2p_offer_detail")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            offer.uuid?.hashCode() ?: alert.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = "🎯 ¡Nueva oferta encontrada!"
        val notificationText = "${alert.name}: ${offer.type?.uppercase() ?: "OFERTA"} ${offer.amount ?: "N/A"} ${offer.coin ?: ""} a ${offer.receive ?: "N/A"}"

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.doublecheck) // Ensure this icon exists in resources
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$notificationText\n\nMensaje: ${offer.message ?: "Sin mensaje"}\nTasa: ${offer.receive ?: "N/A"}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
            notificationManager.notify(alert.id.toInt(), notification)
            android.util.Log.d("OfferCheckWorker", "Notification sent successfully for alert ${alert.id}")
        } catch (e: Exception) {
            android.util.Log.e("OfferCheckWorker", "Failed to send notification", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones cuando se encuentran ofertas que cumplen tus criterios"
            enableVibration(true)
            enableLights(true)
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
