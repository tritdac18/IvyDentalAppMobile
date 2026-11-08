package com.example.ivydentalapp.data.service

import com.example.ivydentalapp.data.local.AppDatabase
import com.example.ivydentalapp.data.local.dao.EmailVerificationDao
import com.example.ivydentalapp.data.local.entity.EmailVerificationEntity
import androidx.room.withTransaction

class EmailVerificationService(
    private val dao: EmailVerificationDao,
    private val database: AppDatabase,
    private val expiryMinutes: Long = 10
) {
    companion object {
        private const val VERIFICATION_CODE_LENGTH = 6
    }

    /**
     * Generate a 6-digit verification code
     */
    fun generateCode(): String {
        val num = (0..999999).random()
        return num.toString().padStart(VERIFICATION_CODE_LENGTH, '0')
    }

    /**
     * Save verification code for an email address
     * Deletes old expired/used codes first
     */
    suspend fun saveCode(email: String, code: String) {
        try {
            val now = System.currentTimeMillis()
            val expiresAt = now + (expiryMinutes * 60 * 1000) // Ensure proper calculation

            android.util.Log.d("EmailVerificationService", "=== SAVING CODE ===")
            android.util.Log.d("EmailVerificationService", "Email: $email")
            android.util.Log.d("EmailVerificationService", "Code: $code")
            android.util.Log.d("EmailVerificationService", "Current time: $now")
            android.util.Log.d("EmailVerificationService", "Expires at: $expiresAt (${expiryMinutes} minutes from now)")

            // Use transaction to ensure atomicity
            database.withTransaction {
                // Check what codes exist before cleanup
                val beforeCleanup = dao.getLatestCodeAny(email)
                android.util.Log.d("EmailVerificationService", "Codes before cleanup: ${beforeCleanup?.verificationCode}")

                // Clean up old codes (only expired or used ones) - but use a slightly earlier time to avoid deleting codes that just expired
                val cleanupTime = now - 1000 // 1 second buffer
                dao.deleteOld(email, cleanupTime)
                android.util.Log.d("EmailVerificationService", "Cleaned up old codes")

                // Insert new code
                val entity = EmailVerificationEntity(
                    email = email,
                    verificationCode = code,
                    expiresAt = expiresAt,
                    isUsed = false,
                    createdAt = now
                )
                val id = dao.insert(entity)
                android.util.Log.d("EmailVerificationService", "Code inserted with ID: $id")
            }
            
            // Verify it was saved correctly - check immediately and after a delay
            val countBefore = dao.countCodesForEmail(email)
            android.util.Log.d("EmailVerificationService", "Codes in DB before insert: $countBefore")
            
            // Wait a bit to ensure database write is committed
            kotlinx.coroutines.delay(300)
            
            val countAfter = dao.countCodesForEmail(email)
            android.util.Log.d("EmailVerificationService", "Codes in DB after insert: $countAfter")
            
            val saved = dao.getLatestValidCode(email, now)
            if (saved != null) {
                android.util.Log.d("EmailVerificationService", "✓ Verification SUCCESS - saved code exists: true")
                android.util.Log.d("EmailVerificationService", "✓ Code matches: ${saved.verificationCode == code}")
                android.util.Log.d("EmailVerificationService", "✓ Saved code: ${saved.verificationCode}")
                android.util.Log.d("EmailVerificationService", "✓ Expires at: ${saved.expiresAt}, time left: ${(saved.expiresAt - now) / 1000} seconds")
            } else {
                android.util.Log.e("EmailVerificationService", "✗ ERROR: Code was not found after saving!")
                val anyCode = dao.getLatestCodeAny(email)
                if (anyCode != null) {
                    android.util.Log.e("EmailVerificationService", "Found code in DB but invalid: ${anyCode.verificationCode}")
                    android.util.Log.e("EmailVerificationService", "  - isUsed: ${anyCode.isUsed}")
                    android.util.Log.e("EmailVerificationService", "  - expiresAt: ${anyCode.expiresAt}")
                    android.util.Log.e("EmailVerificationService", "  - now: $now")
                    android.util.Log.e("EmailVerificationService", "  - expired: ${anyCode.expiresAt <= now}")
                } else {
                    android.util.Log.e("EmailVerificationService", "No codes found in DB at all! Count: $countAfter")
                }
            }
            android.util.Log.d("EmailVerificationService", "=== END SAVING CODE ===")
        } catch (e: Exception) {
            android.util.Log.e("EmailVerificationService", "ERROR saving code", e)
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Verify a code for an email address
     * Returns true if code is valid and not expired/used
     */
    suspend fun verifyCode(email: String, code: String): Boolean {
        try {
            val now = System.currentTimeMillis()
            android.util.Log.d("EmailVerificationService", "Verifying code for email: $email, code: $code, current time: $now")
            
            // First, check if any code exists for this email (for debugging)
            val latestAny = dao.getLatestCodeAny(email)
            if (latestAny != null) {
                android.util.Log.d("EmailVerificationService", "Latest code in DB: ${latestAny.verificationCode}, expires: ${latestAny.expiresAt}, isUsed: ${latestAny.isUsed}, time left: ${(latestAny.expiresAt - now) / 1000} seconds")
            } else {
                android.util.Log.d("EmailVerificationService", "No codes found in DB for email: $email")
            }
            
            val entity = dao.findValid(email, code, now)
            android.util.Log.d("EmailVerificationService", "Found valid entity: ${entity != null}")
            
            if (entity == null) {
                // Check if code exists but is expired or used
                if (latestAny != null) {
                    if (latestAny.verificationCode == code) {
                        if (latestAny.isUsed) {
                            android.util.Log.d("EmailVerificationService", "Code was already used")
                        } else if (latestAny.expiresAt <= now) {
                            android.util.Log.d("EmailVerificationService", "Code expired. Expires at: ${latestAny.expiresAt}, now: $now, diff: ${now - latestAny.expiresAt}ms")
                        }
                    } else {
                        android.util.Log.d("EmailVerificationService", "Code doesn't match. Expected: ${latestAny.verificationCode}, got: $code")
                    }
                }
                return false
            }

            // Mark as used
            val updated = entity.copy(isUsed = true, verifiedAt = now)
            dao.update(updated)
            android.util.Log.d("EmailVerificationService", "Code verified and marked as used")
            return true
        } catch (e: Exception) {
            android.util.Log.e("EmailVerificationService", "Error verifying code", e)
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Get the latest valid verification code for an email (for testing/debugging)
     */
    suspend fun getLatestCode(email: String): String? {
        val now = System.currentTimeMillis()
        return dao.getLatestValidCode(email, now)?.verificationCode
    }
    
    /**
     * Resend verification code - generates a new code and saves it
     */
    suspend fun resendCode(email: String): String {
        val code = generateCode()
        saveCode(email, code)
        return code
    }
}

