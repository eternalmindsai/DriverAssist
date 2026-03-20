package com.driverassist

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallReceiver"
        private const val SMS_MESSAGE = "Hi! I am currently driving. I will call you back shortly. – Driver Assist"
        private var lastProcessedNumber: String? = null
        private var lastProcessedTime: Long = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING && !incomingNumber.isNullOrEmpty()) {

            val now = System.currentTimeMillis()
            if (incomingNumber == lastProcessedNumber && now - lastProcessedTime < 3000) return
            if (!AppPrefs.isDrivingMode(context)) return

            Log.d(TAG, "Driving mode ON — rejecting call from $incomingNumber")

            lastProcessedNumber = incomingNumber
            lastProcessedTime = now

            rejectCall(context)
           sendDrivingSms(context, incomingNumber, now)

            val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(now))
            AppPrefs.addCallLogEntry(context, CallLogEntry(incomingNumber, now, timeStr))
            context.sendBroadcast(Intent("com.driverassist.CALL_LOG_UPDATED"))
        }
    }

    private fun rejectCall(context: Context) {
        try {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
                Log.d(TAG, "Call rejected via TelecomManager")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting call: ${e.message}")
        }
    }

 private fun sendDrivingSms(context: Context, phoneNumber: String, timestamp: Long) {
    try {
        val serviceIntent = Intent(context, SmsSenderService::class.java).apply {
            putExtra(SmsSenderService.EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(SmsSenderService.EXTRA_TIMESTAMP, timestamp)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        Log.d(TAG, "SMS service started for $phoneNumber")
    } catch (e: Exception) {
        Log.e(TAG, "Error starting SMS service: ${e.message}")
    }
}

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    context.getSystemService(SmsManager::class.java)
                        .createForSubscriptionId(subscriptionId)
                } else {
                    context.getSystemService(SmsManager::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            smsManager.sendTextMessage(phoneNumber, null, SMS_MESSAGE, null, null)
            Log.d(TAG, "SMS sent to $phoneNumber")

        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS: ${e.message}")
        }
    }
}
