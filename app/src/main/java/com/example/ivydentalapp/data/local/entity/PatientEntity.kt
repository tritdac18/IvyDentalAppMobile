package com.example.ivydentalapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "tbl_patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "patient_id")
    val patientId: Int = 0,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "first_name")
    val firstName: String,
    
    @ColumnInfo(name = "last_name")
    val lastName: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "password")
    val password: String,
    
    @ColumnInfo(name = "dob")
    val dob: String?,
    
    @ColumnInfo(name = "primary_phone")
    val primaryPhone: String?,
    
    @ColumnInfo(name = "blood_type")
    val bloodType: String? = "Unknown",
    
    @ColumnInfo(name = "gender")
    val gender: String? = "Prefer not to say",
    
    // Note: Allergies are in a separate table in SQL (tbl_patient_allergies),
    // but for simplicity in this Entity we might often load them separately.
    // For now we will replicate the main table columns.
    
    @ColumnInfo(name = "status")
    val status: String = "pending_verification",
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
