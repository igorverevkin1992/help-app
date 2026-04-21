package com.helpapp.therapy.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.helpapp.therapy.MainActivity
import com.helpapp.therapy.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_FIRE) return
        val slot = intent.getIntExtra(ReminderScheduler.EXTRA_SLOT, -1)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Therapy module"
        showNotification(context, slot, title)
        // Re-arm for the next day — AlarmManager one-shots don't self-repeat.
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try { scheduler.rescheduleFromPreferences() } finally { pending.finish() }
        }
    }

    private fun showNotification(context: Context, slot: Int, title: String) {
        val mgr = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        ensureChannel(context)
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val content = PendingIntent.getActivity(
            context, slot, launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Open the module to complete today's session.")
            .setAutoCancel(true)
            .setContentIntent(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        mgr.notify(slot, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(ReminderScheduler.CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            ReminderScheduler.CHANNEL_ID,
            "Therapy reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily prompts for the three therapy modules."
        }
        mgr.createNotificationChannel(channel)
    }
}
