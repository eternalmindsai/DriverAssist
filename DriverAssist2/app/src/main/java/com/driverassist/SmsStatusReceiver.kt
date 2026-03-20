package com.driverassist

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SmsStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timestamp = intent.getLongExtra(SmsSenderService.EXTRA_TIMESTAMP, 0L)
        if (timestamp == 0L) return

        when (intent.action) {
            SmsSenderService.ACTION_SMS_SENT -> {
                val status = when (resultCode) {
                    Activity.RESULT_OK -> "✅ SMS Sent"
                    else -> "❌ SMS Failed"
                }
                Log.d("SmsStatusReceiver", "Sent status: $status")
                AppPrefs.updateSmsStatus(context, timestamp, status)
                context.sendBroadcast(Intent("com.driverassist.CALL_LOG_UPDATED"))
            }
            SmsSenderService.ACTION_SMS_DELIVERED -> {
                Log.d("SmsStatusReceiver", "SMS Delivered")
                AppPrefs.updateSmsStatus(context, timestamp, "✅ SMS Delivered")
                context.sendBroadcast(Intent("com.driverassist.CALL_LOG_UPDATED"))
            }
        }
    }
}
