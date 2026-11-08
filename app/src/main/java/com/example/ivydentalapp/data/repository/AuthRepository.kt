package com.example.ivydentalapp.data.repository

import com.example.ivydentalapp.data.local.dao.PatientDao
import com.example.ivydentalapp.data.local.dao.ReceptionistDao
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction

import android.content.Context
import android.content.SharedPreferences

class AuthRepository(
    private val patientDao: PatientDao,
    private val receptionistDao: ReceptionistDao,
    private val context: Context
) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ivy_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_TYPE = "user_type" // "patient" or "receptionist"
    }

    suspend fun registerPatient(patient: PatientEntity, allergies: List<String>): Result<Long> {
        val database = com.example.ivydentalapp.data.local.AppDatabase.getDatabase(context)
        return try {
            database.withTransaction {
                // Check if user already exists
                val existingUser = patientDao.getPatientByEmail(patient.email)
                if (existingUser != null) {
                    throw Exception("Email already registered")
                }
                
                val existingUsername = patientDao.getPatientByUsername(patient.username)
                if (existingUsername != null) {
                    throw Exception("Username already taken")
                }

                val id = patientDao.insertPatient(patient)
                
                // Save allergies
                if (allergies.isNotEmpty()) {
                    val allergyEntities = allergies.map { 
                        com.example.ivydentalapp.data.local.entity.PatientAllergyEntity(
                            patientId = id.toInt(), 
                            allergyName = it
                        ) 
                    }
                    patientDao.insertAllergies(allergyEntities)
                }
                
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePatient(patient: PatientEntity, allergies: List<String>): Result<Unit> {
        val database = com.example.ivydentalapp.data.local.AppDatabase.getDatabase(context)
        return try {
             database.withTransaction {
                patientDao.updatePatient(patient)
                
                // Update allergies: Delete all existing and re-insert
                patientDao.deleteAllergiesForPatient(patient.patientId)
                
                if (allergies.isNotEmpty()) {
                    val allergyEntities = allergies.map { 
                        com.example.ivydentalapp.data.local.entity.PatientAllergyEntity(
                            patientId = patient.patientId, 
                            allergyName = it
                        ) 
                    }
                    patientDao.insertAllergies(allergyEntities)
                }
             }
             Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIfPatientExists(identifier: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                patientDao.getPatientByIdentifier(identifier) != null
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun checkIfReceptionistExists(identifier: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                receptionistDao.getReceptionistByIdentifier(identifier) != null
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun loginPatient(identifier: String, password: String): Result<PatientEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val patient = patientDao.getPatientByIdentifier(identifier)
                if (patient != null) {
                    if (patient.password == password) {
                        saveSession(patient)
                        Result.success(patient)
                    } else {
                        Result.failure(Exception("Invalid password"))
                    }
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun saveSession(patient: PatientEntity) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putInt(KEY_USER_ID, patient.patientId)
            .putString(KEY_USERNAME, patient.username)
            .putString(KEY_USER_TYPE, "patient")
            .apply()
    }

    private fun saveReceptionistSession(receptionist: ReceptionistEntity) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putInt(KEY_USER_ID, receptionist.staffId)
            .putString(KEY_USERNAME, receptionist.username)
            .putString(KEY_USER_TYPE, receptionist.role.lowercase()) // "receptionist" or "manager"
            .apply()
    }

    suspend fun loginReceptionist(identifier: String, password: String): Result<ReceptionistEntity> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepository", "Attempting login for identifier: $identifier")
                val receptionist = receptionistDao.getReceptionistByIdentifier(identifier)
                android.util.Log.d("AuthRepository", "Receptionist found: ${receptionist != null}")
                
                if (receptionist != null) {
                    android.util.Log.d("AuthRepository", "Found receptionist - username: ${receptionist.username}, role: ${receptionist.role}")
                    android.util.Log.d("AuthRepository", "Password match: ${receptionist.password == password}")
                    android.util.Log.d("AuthRepository", "Role check: ${receptionist.role == "Receptionist" || receptionist.role == "Manager"}")
                    
                    // Allow both Receptionist and Manager
                    if (receptionist.password == password && (receptionist.role == "Receptionist" || receptionist.role == "Manager")) {
                        saveReceptionistSession(receptionist)
                        android.util.Log.d("AuthRepository", "Login successful for ${receptionist.username}")
                        Result.success(receptionist)
                    } else {
                        android.util.Log.e("AuthRepository", "Login failed - password or role mismatch")
                        Result.failure(Exception("Invalid password or unauthorized role"))
                    }
                } else {
                    android.util.Log.e("AuthRepository", "Staff not found for identifier: $identifier")
                    // List all staff for debugging
                    try {
                        val allStaff = receptionistDao.getAllStaff()
                        android.util.Log.d("AuthRepository", "All staff in database: ${allStaff.size}")
                        allStaff.forEach { staff ->
                            android.util.Log.d("AuthRepository", "  - Username: ${staff.username}, Role: ${staff.role}, Email: ${staff.email}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AuthRepository", "Error listing staff", e)
                    }
                    Result.failure(Exception("Staff not found"))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Exception during login", e)
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    fun getCurrentUserType(): String? {
        return sharedPreferences.getString(KEY_USER_TYPE, null)
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

    suspend fun getCurrentUser(): PatientEntity? {
        val userType = getCurrentUserType()
        if (userType != "patient") return null
        val userId = getCurrentUserId()
        if (userId == -1) return null
        return withContext(Dispatchers.IO) {
            patientDao.getPatientById(userId)
        }
    }

    suspend fun getCurrentReceptionist(): ReceptionistEntity? {
        val userType = getCurrentUserType()
        if (userType != "receptionist" && userType != "manager") return null
        val userId = getCurrentUserId()
        if (userId == -1) return null
        return withContext(Dispatchers.IO) {
            receptionistDao.getReceptionistById(userId)
        }
    }

    suspend fun updateReceptionist(receptionist: ReceptionistEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                receptionistDao.updateReceptionist(receptionist)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getAllergies(patientId: Int): List<String> {
        return withContext(Dispatchers.IO) {
            patientDao.getAllergiesForPatient(patientId).map { it.allergyName }
        }
    }
}
