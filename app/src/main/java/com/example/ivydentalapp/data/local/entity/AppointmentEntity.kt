package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "appointment_id")
    val appointmentId: Int = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Int,

    @ColumnInfo(name = "dentist_id")
    val dentistId: Int,

    @ColumnInfo(name = "scheduled_datetime")
    val scheduledDatetime: String, // Stored as ISO string or similar

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int = 60,

    @ColumnInfo(name = "service_type")
    val serviceType: String?,

    @ColumnInfo(name = "price")
    val price: Double?,

    @ColumnInfo(name = "service_id")
    val serviceId: Int?,

    @ColumnInfo(name = "triage_urgency")
    val triageUrgency: String = "Low",

    @ColumnInfo(name = "status")
    val status: String = "Pending", // Pending, Scheduled, Confirmed, Completed, Cancelled, No-Show

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
