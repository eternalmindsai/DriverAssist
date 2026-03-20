package com.driverassist

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CallLogEntry(
    val phoneNumber: String,
    val timestamp: Long,
    val formattedTime: String
)

object AppPrefs {
    private const val PREFS_NAME = "driver_assist_prefs"
    private const val KEY_DRIVING_MODE = "driving_mode"
    private const val KEY_CALL_LOG = "call_log"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private val gson = Gson()

    fun isDrivingMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DRIVING_MODE, false)
    }

    fun setDrivingMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_DRIVING_MODE, enabled)
        }
    }

    fun getCallLog(context: Context): MutableList<CallLogEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CALL_LOG, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<CallLogEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addCallLogEntry(context: Context, entry: CallLogEntry) {
        val log = getCallLog(context)
        log.add(0, entry) // newest first
        if (log.size > 100) log.removeAt(log.lastIndex) // keep max 100
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_CALL_LOG, gson.toJson(log))
        }
    }

    fun isOnboardingDone(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingDone(context: Context, done: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_ONBOARDING_DONE, done)
        }
    }

    fun clearCallLog(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_CALL_LOG)
        }
    }
}
