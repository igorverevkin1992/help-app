package com.helpapp.therapy.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.helpapp.therapy.data.prefs.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules up to three fixed-time daily reminders (responsibility module in
 * the morning, dereflection midday, vitality in the evening). We use
 * AlarmManager with setAndAllowWhileIdle — exact alarms are intentionally
 * avoided so we do not need the SCHEDULE_EXACT_ALARM runtime prompt unless
 * the user asks for tighter reminders.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AppPreferences,
) {
    suspend fun rescheduleFromPreferences() {
        val snap = preferences.snapshot.first()
        cancelAll()
        if (!snap.remindersEnabled) return
        schedule(SLOT_RESPONSIBILITY, snap.reminderMorningMinutes, "Responsibility module")
        schedule(SLOT_DEREFLECTION, snap.reminderMiddayMinutes, "Dereflection module")
        schedule(SLOT_VITALITY, snap.reminderEveningMinutes, "Vitality compass")
    }

    fun cancelAll() {
        listOf(SLOT_RESPONSIBILITY, SLOT_DEREFLECTION, SLOT_VITALITY).forEach { slot ->
            alarmManager().cancel(pendingIntentFor(slot, "", updateCurrent = false))
        }
    }

    private fun schedule(slot: Int, minuteOfDay: Int, title: String) {
        val target = nextTriggerMillis(minuteOfDay)
        val intent = pendingIntentFor(slot, title, updateCurrent = true)
        alarmManager().setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target, intent)
    }

    private fun nextTriggerMillis(minuteOfDay: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun pendingIntentFor(slot: Int, title: String, updateCurrent: Boolean): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_SLOT, slot)
            putExtra(EXTRA_TITLE, title)
        }
        var flags = PendingIntent.FLAG_IMMUTABLE
        if (updateCurrent) flags = flags or PendingIntent.FLAG_UPDATE_CURRENT
        else flags = flags or PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, slot, intent, flags)
            ?: PendingIntent.getBroadcast(
                context, slot, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }

    private fun alarmManager(): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_FIRE = "com.helpapp.therapy.action.FIRE_REMINDER"
        const val EXTRA_SLOT = "slot"
        const val EXTRA_TITLE = "title"
        const val SLOT_RESPONSIBILITY = 1001
        const val SLOT_DEREFLECTION = 1002
        const val SLOT_VITALITY = 1003
        const val CHANNEL_ID = "therapy_reminders"

        @Suppress("unused")
        fun isIdleSafe(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}
