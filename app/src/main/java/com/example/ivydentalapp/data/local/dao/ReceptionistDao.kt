package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity

@Dao
interface ReceptionistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceptionist(receptionist: ReceptionistEntity): Long

    @androidx.room.Update
    suspend fun updateReceptionist(receptionist: ReceptionistEntity)

    @Query("SELECT * FROM tbl_staff WHERE email = :email LIMIT 1")
    suspend fun getReceptionistByEmail(email: String): ReceptionistEntity?

    @Query("SELECT * FROM tbl_staff WHERE username = :username LIMIT 1")
    suspend fun getReceptionistByUsername(username: String): ReceptionistEntity?

    @Query("SELECT * FROM tbl_staff WHERE email = :identifier OR username = :identifier LIMIT 1")
    suspend fun getReceptionistByIdentifier(identifier: String): ReceptionistEntity?

    @Query("SELECT * FROM tbl_staff WHERE staff_id = :id LIMIT 1")
    suspend fun getReceptionistById(id: Int): ReceptionistEntity?

    @Query("SELECT * FROM tbl_staff WHERE role = 'Receptionist'")
    suspend fun getAllReceptionists(): List<ReceptionistEntity>
    
    @Query("SELECT * FROM tbl_staff")
    suspend fun getAllStaff(): List<ReceptionistEntity>
    
    @Query("DELETE FROM tbl_staff WHERE staff_id = :staffId")
    suspend fun deleteStaffById(staffId: Int)
    @Query("UPDATE tbl_staff SET status = :status WHERE staff_id = :staffId")
    suspend fun updateStatus(staffId: Int, status: String)
}

