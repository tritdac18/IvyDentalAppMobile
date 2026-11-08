package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ivydentalapp.data.local.entity.EmailVerificationEntity

@Dao
interface EmailVerificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EmailVerificationEntity): Long

    @Query("DELETE FROM tbl_email_verification WHERE email = :email AND (is_used = 1 OR expires_at < :now)")
    suspend fun deleteOld(email: String, now: Long)
    
    @Query("SELECT COUNT(*) FROM tbl_email_verification WHERE email = :email")
    suspend fun countCodesForEmail(email: String): Int

    @Query("""
        SELECT * FROM tbl_email_verification
        WHERE email = :email
          AND verification_code = :code
          AND is_used = 0
          AND expires_at > :now
        ORDER BY created_at DESC
        LIMIT 1
    """)
    suspend fun findValid(email: String, code: String, now: Long): EmailVerificationEntity?

    @Update
    suspend fun update(entity: EmailVerificationEntity)

    @Query("SELECT * FROM tbl_email_verification WHERE email = :email AND is_used = 0 AND expires_at > :now ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestValidCode(email: String, now: Long): EmailVerificationEntity?
    
    @Query("SELECT * FROM tbl_email_verification WHERE email = :email ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestCodeAny(email: String): EmailVerificationEntity?
}
