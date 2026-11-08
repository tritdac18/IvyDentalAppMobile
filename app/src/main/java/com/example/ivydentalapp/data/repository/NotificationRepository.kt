package com.example.ivydentalapp.data.repository

import com.example.ivydentalapp.data.local.dao.NotificationDao
import com.example.ivydentalapp.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun getUserNotifications(userId: Int, userType: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForUser(userId, userType)
    }

    suspend fun createNotification(userId: Int, userType: String, title: String, message: String) {
        val notification = NotificationEntity(
            userId = userId,
            userType = userType,
            title = title,
            message = message,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        notificationDao.insertNotification(notification)
    }
    
    suspend fun markAsRead(notificationId: Int) {
        notificationDao.markAsRead(notificationId)
    }
    
    suspend fun markAllAsRead(userId: Int, userType: String) {
        notificationDao.markAllAsRead(userId, userType)
    }
}
