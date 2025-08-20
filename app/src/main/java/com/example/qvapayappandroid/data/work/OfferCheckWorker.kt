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
            // Verificar si hay alertas activas
            val activeAlerts = offerAlertRepository.getActiveAlerts().first()
            
            if (activeAlerts.isEmpty()) {
                // No hay alertas activas, no hacer nada
                return Result.success()
            }

            // Verificar si tenemos sesión activa
            val accessToken = sessionRepository.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                // Sin sesión, no podemos hacer peticiones
                return Result.success()
            }

            // Verificar cada alerta
            activeAlerts.forEach { alert ->
                checkAlertCriteria(alert, accessToken)
            }

            Result.success()
        } catch (e: Exception) {
            // Log error y reintentar
            android.util.Log.e("OfferCheckWorker", "Error checking offers", e)
            Result.retry()
        }
    }

    private suspend fun checkAlertCriteria(alert: OfferAlert, accessToken: String) {
        try {
            // Crear filtros según los criterios de la alerta
            val filterRequest = P2PFilterRequest(
                type = if (alert.offerType == "both") null else alert.offerType,
                coin = alert.coinType,
                min = alert.minAmount,
                max = alert.maxAmount,
                vip = if (alert.onlyVip) true else null,
                page = 1,
                perPage = 50 // Límite para evitar rate limiting
            )
            
            // Obtener ofertas según los criterios de la alerta
            val offerResponse = p2pRepository.getP2POffers(filterRequest, accessToken).getOrNull()

            offerResponse?.data?.forEach { offer ->
                if (doesOfferMatchCriteria(offer, alert)) {
                    // La oferta cumple los criterios, enviar notificación
                    sendOfferNotification(alert, offer)
                    
                    // Actualizar timestamp de última activación
                    offerAlertRepository.updateLastTriggeredAt(alert.id, System.currentTimeMillis())
                }
            }

            // Actualizar timestamp de última verificación
            offerAlertRepository.updateLastCheckedAt(alert.id, System.currentTimeMillis())

        } catch (e: Exception) {
            android.util.Log.e("OfferCheckWorker", "Error checking alert ${alert.id}", e)
        }
    }

    private fun doesOfferMatchCriteria(offer: P2POffer, alert: OfferAlert): Boolean {
        // Verificar tipo de oferta
        if (alert.offerType != "both" && offer.type != alert.offerType) {
            return false
        }

        // Verificar moneda
        if (offer.coin != alert.coinType) {
            return false
        }

        // Verificar monto
        val offerAmount = offer.amount?.toDoubleOrNull() ?: return false
        alert.minAmount?.let { min ->
            if (offerAmount < min) return false
        }
        alert.maxAmount?.let { max ->
            if (offerAmount > max) return false
        }

        // Verificar tasa/ratio
        val offerRate = offer.receive?.toDoubleOrNull() ?: return false
        when (alert.rateComparison) {
            "greater" -> if (offerRate <= alert.targetRate) return false
            "less" -> if (offerRate >= alert.targetRate) return false
            "equal" -> if (kotlin.math.abs(offerRate - alert.targetRate) > 0.01) return false
        }

        // Verificar KYC si es requerido
        if (alert.onlyKyc && offer.onlyKyc != 1) {
            return false
        }

        // Verificar VIP si es requerido  
        if (alert.onlyVip && offer.onlyVip != 1) {
            return false
        }

        return true
    }

    private fun sendOfferNotification(alert: OfferAlert, offer: P2POffer) {
        // Verificar permisos de notificaciones antes de enviar
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
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener este ícono
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