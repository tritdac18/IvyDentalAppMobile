package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "service_id")
    val serviceId: Int = 0,

    @ColumnInfo(name = "service_name")
    val serviceName: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "base_price")
    val basePrice: Double,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

