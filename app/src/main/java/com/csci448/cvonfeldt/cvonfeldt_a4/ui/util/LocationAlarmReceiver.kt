package com.csci448.cvonfeldt.cvonfeldt_a4.ui.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.util.concurrent.TimeUnit

//broadcastReceiver for handling location based alarms and notifs
class LocationAlarmReceiver : BroadcastReceiver() {
    companion object {
         // constants for notification channel and deep linking
        private const val CHANNEL_ID = "location_channel"
        private const val NOTIFICATION_ID = 1
        private const val ALARM_REQUEST_CODE = 0
        private const val SCHEME = "https"
        private const val HOST = "weathrtrackr.labs.csci448.mines.edu"
        private const val BASE_URI = "$SCHEME://$HOST"

        private fun formatUriString(location: Location? = null): String {
            return if (location == null) {
                BASE_URI
            } else {
                "$BASE_URI?lat=${location.latitude}&lon=${location.longitude}"
            }
        }

        // create deep link intent for notifications
        fun createPendingIntent(context: Context,location: Location): PendingIntent {
            val deepLinkIntent = Intent(
                Intent.ACTION_VIEW,
                formatUriString(location).toUri(),
                context,
                Class.forName("com.csci448.cvonfeldt.cvonfeldt_a4.MainActivity")
            )
            return PendingIntent.getActivity(
                context,
                0,
                deepLinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    var lastLocation: Location? = null

    override fun onReceive(context: Context, intent: Intent) {
        lastLocation?.let { location ->
            showNotification(context, location)
        }
    }

    private fun showNotification(context: Context, location: Location) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val pendingIntent = createPendingIntent(context, location)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Location Update")
            .setContentText("Tap to view weather at your location")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for location-based weather updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkPermissionAndScheduleAlarm(
        context: Context,
        permissionLauncher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> scheduleAlarm(context)
                else -> permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleAlarm(context)
        }
    }

    private fun scheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LocationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule alarm to repeat every hour
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            pendingIntent
        )
    }
}