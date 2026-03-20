package com.driverassist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
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

            // Debounce: skip if same number was processed within 3 seconds
            val now = System.currentTimeMillis()
            if (incomingNumber == lastProcessedNumber && now - lastProcessedTime < 3000) return

            if (!AppPrefs.isDrivingMode(context)) return

            Log.d(TAG, "Driving mode ON — rejecting call from $incomingNumber")

            lastProcessedNumber = incomingNumber
            lastProcessedTime = now

            // 1. Reject the call
            rejectCall(context)

            // 2. Send SMS
            sendDrivingSms(context, incomingNumber)

            // 3. Log the call
            val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(now))
            AppPrefs.addCallLogEntry(context, CallLogEntry(incomingNumber, now, timeStr))

            // 4. Notify UI to refresh (using a broadcast)
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
            } else {
                Log.w(TAG, "ANSWER_PHONE_CALLS permission not granted — cannot reject call")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting call: ${e.message}")
        }
    }

    private fun sendDrivingSms(context: Context, phoneNumber: String) {
        try {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(phoneNumber, null, SMS_MESSAGE, null, null)
                Log.d(TAG, "SMS sent to $phoneNumber")
            } else {
                Log.w(TAG, "SEND_SMS permission not granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS: ${e.message}")
        }
    }
}
