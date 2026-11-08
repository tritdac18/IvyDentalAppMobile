package com.example.ivydentalapp.data.local.dao

import androidx.room.*
import com.example.ivydentalapp.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM tbl_notifications WHERE user_id = :userId AND user_type = :userType ORDER BY created_at DESC")
    fun getNotificationsForUser(userId: Int, userType: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM tbl_notifications WHERE user_type = 'Staff' ORDER BY created_at DESC")
    fun getStaffNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE tbl_notifications SET is_read = 1 WHERE notification_id = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    @Query("UPDATE tbl_notifications SET is_read = 1 WHERE user_id = :userId AND user_type = :userType")
    suspend fun markAllAsRead(userId: Int, userType: String)
}
