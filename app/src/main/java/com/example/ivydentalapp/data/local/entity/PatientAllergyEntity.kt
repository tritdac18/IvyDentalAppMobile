package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_patient_allergies",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["patient_id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PatientAllergyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Int,

    @ColumnInfo(name = "allergy_name")
    val allergyName: String
)
