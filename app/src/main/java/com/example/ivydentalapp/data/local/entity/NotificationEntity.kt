package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    val notificationId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int, // Can depend on type, or just store ID. For simplicity, assume Patient ID or 0 for Generic/Admin

    @ColumnInfo(name = "user_type")
    val userType: String = "Patient", // Patient, Staff

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: String
)
