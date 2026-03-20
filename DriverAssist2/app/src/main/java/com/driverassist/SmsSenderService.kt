package com.driverassist

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log

class SmsSenderService : Service() {

    companion object {
        private const val TAG = "SmsSenderService"
        private const val CHANNEL_ID = "sms_sender_channel"
        private const val SMS_MESSAGE = "Hi! I am currently driving. I will call you back shortly. – Driver Assist"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val ACTION_SMS_SENT = "com.driverassist.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.driverassist.SMS_DELIVERED"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra(EXTRA_PHONE_NUMBER)
        val timestamp = intent?.getLongExtra(EXTRA_TIMESTAMP, 0L) ?: 0L

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Driver Assist")
            .setContentText("Sending auto-reply SMS...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)

        if (!phoneNumber.isNullOrEmpty()) {
            sendSms(phoneNumber, timestamp)
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

private fun sendSms(phoneNumber: String, timestamp: Long) {
    try {
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
            } else {
                getSystemService(SmsManager::class.java)
            }
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        smsManager.sendTextMessage(phoneNumber, null, SMS_MESSAGE, null, null)

        // Mark as sent immediately after successful send
        AppPrefs.updateSmsStatus(this, timestamp, "✅ SMS Sent")
        sendBroadcast(Intent("com.driverassist.CALL_LOG_UPDATED"))
        Log.d(TAG, "SMS sent to $phoneNumber")

    } catch (e: Exception) {
        AppPrefs.updateSmsStatus(this, timestamp, "❌ SMS Failed")
        sendBroadcast(Intent("com.driverassist.CALL_LOG_UPDATED"))
        Log.e(TAG, "Error sending SMS: ${e.message}")
    }
}

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "SMS Sender", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
