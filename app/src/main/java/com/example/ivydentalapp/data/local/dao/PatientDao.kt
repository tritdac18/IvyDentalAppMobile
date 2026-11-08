package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ivydentalapp.data.local.entity.PatientEntity

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPatient(patient: PatientEntity): Long

    @androidx.room.Update
    suspend fun updatePatient(patient: PatientEntity)

    @Query("SELECT * FROM tbl_patients WHERE email = :email LIMIT 1")
    suspend fun getPatientByEmail(email: String): PatientEntity?

    @Query("SELECT * FROM tbl_patients WHERE username = :username LIMIT 1")
    suspend fun getPatientByUsername(username: String): PatientEntity?

    @Query("SELECT * FROM tbl_patients WHERE email = :identifier OR username = :identifier LIMIT 1")
    suspend fun getPatientByIdentifier(identifier: String): PatientEntity?

    @Query("SELECT * FROM tbl_patients WHERE patient_id = :id LIMIT 1")
    suspend fun getPatientById(id: Int): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergies(allergies: List<com.example.ivydentalapp.data.local.entity.PatientAllergyEntity>)

    @Query("SELECT * FROM tbl_patient_allergies WHERE patient_id = :patientId")
    suspend fun getAllergiesForPatient(patientId: Int): List<com.example.ivydentalapp.data.local.entity.PatientAllergyEntity>

    @Query("DELETE FROM tbl_patient_allergies WHERE patient_id = :patientId")
    suspend fun deleteAllergiesForPatient(patientId: Int)

    @Query("SELECT * FROM tbl_patients ORDER BY last_name, first_name")
    suspend fun getAllPatients(): List<PatientEntity>
}
