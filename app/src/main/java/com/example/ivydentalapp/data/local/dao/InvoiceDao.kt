package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM tbl_invoices WHERE patient_id = :patientId ORDER BY generated_at DESC")
    fun getInvoicesForPatient(patientId: Int): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM tbl_invoices ORDER BY generated_at DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM tbl_invoices WHERE appointment_id = :appointmentId LIMIT 1")
    suspend fun getInvoiceByAppointmentId(appointmentId: Int): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Query("UPDATE tbl_invoices SET payment_status = :status WHERE invoice_id = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: Int, status: String)

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM tbl_invoices WHERE payment_status IN ('Unpaid', 'Partial')")
    suspend fun getOutstandingBalance(): Double

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM tbl_invoices WHERE payment_status = 'Paid' AND DATE(generated_at) = DATE('now')")
    suspend fun getDailyRevenue(): Double

    @Query("SELECT COUNT(*) FROM tbl_invoices")
    suspend fun getInvoiceCount(): Int

    @Query("DELETE FROM tbl_invoices WHERE invoice_id = :invoiceId")
    suspend fun deleteInvoice(invoiceId: Int)

    @Query("SELECT * FROM tbl_invoices WHERE payment_status = 'Partial'")
    suspend fun getPartialInvoices(): List<InvoiceEntity>
}
