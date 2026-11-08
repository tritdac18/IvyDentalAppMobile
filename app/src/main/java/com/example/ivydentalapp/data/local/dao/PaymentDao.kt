package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE invoice_id = :invoiceId ORDER BY created_at DESC")
    fun getPaymentsForInvoice(invoiceId: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE payment_id = :paymentId")
    suspend fun getPaymentById(paymentId: Int): PaymentEntity?

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE status = 'Pending'")
    suspend fun getPendingPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY created_at DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE invoice_id IN (:invoiceIds)")
    suspend fun getPaymentsForInvoices(invoiceIds: List<Int>): List<PaymentEntity>

    @Query("""
        SELECT COALESCE(SUM(amount_paid), 0) FROM payments
        WHERE invoice_id = :invoiceId
          AND payment_type = 'Downpayment'
          AND status = 'Completed'
    """)
    suspend fun getTotalDownpaymentForInvoice(invoiceId: Int): Double

    @Query("SELECT * FROM payments WHERE appointment_id = :appointmentId")
    suspend fun getPaymentsByAppointmentId(appointmentId: Int): List<PaymentEntity>
}
