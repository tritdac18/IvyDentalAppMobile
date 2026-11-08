package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_email_verification")
data class EmailVerificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "verification_id")
    val verificationId: Int = 0,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "verification_code")
    val verificationCode: String,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long,

    @ColumnInfo(name = "is_used")
    val isUsed: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "verified_at")
    val verifiedAt: Long? = null
)
