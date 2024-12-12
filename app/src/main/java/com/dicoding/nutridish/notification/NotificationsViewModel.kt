package com.dicoding.nutridish.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.nutridish.data.database.entity.NotificationEntity
import com.dicoding.nutridish.data.database.room.NotificationDao

class NotificationsViewModel(private val notificationDao: NotificationDao) : ViewModel() {

    // Mengamati data notifikasi dari database
    val notifications: LiveData<List<NotificationEntity>> = notificationDao.getAllNotifications()
}

class NotificationsViewModelFactory(
    private val notificationDao: NotificationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(notificationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
