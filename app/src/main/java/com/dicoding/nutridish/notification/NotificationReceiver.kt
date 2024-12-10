package com.dicoding.nutridish.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class NotificationReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("NOTIFICATION_TITLE") ?: "Reminder"
        val message = intent.getStringExtra("NOTIFICATION_MESSAGE") ?: "It's time!"

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(0, title, message)
    }

    companion object {
        fun createIntent(context: Context, title: String, message: String): Intent {
            return Intent(context, NotificationReceiver::class.java).apply {
                putExtra("NOTIFICATION_TITLE", title)
                putExtra("NOTIFICATION_MESSAGE", message)
            }
        }
    }
}