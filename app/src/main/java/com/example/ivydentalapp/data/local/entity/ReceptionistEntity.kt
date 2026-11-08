package com.example.ivydentalapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "tbl_staff")
data class ReceptionistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "staff_id")
    val staffId: Int = 0,
    
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
    
    @ColumnInfo(name = "role")
    val role: String = "Receptionist", // Manager, Dentist, Receptionist
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String?,

    @ColumnInfo(name = "status")
    val status: String = "Active", // Active, Inactive

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)

