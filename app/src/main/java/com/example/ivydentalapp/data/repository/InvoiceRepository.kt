package com.example.ivydentalapp.data.repository

import com.example.ivydentalapp.data.local.dao.InvoiceDao
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    fun getInvoices(patientId: Int): Flow<List<InvoiceEntity>> {
        return invoiceDao.getInvoicesForPatient(patientId)
    }

    fun getAllInvoices(): Flow<List<InvoiceEntity>> {
        return invoiceDao.getAllInvoices()
    }

    suspend fun createInvoice(invoice: InvoiceEntity): Long {
        return invoiceDao.insertInvoice(invoice)
    }

    suspend fun getInvoiceByAppointmentId(appointmentId: Int): InvoiceEntity? {
        return invoiceDao.getInvoiceByAppointmentId(appointmentId)
    }

    suspend fun updateInvoiceStatus(invoiceId: Int, status: String) {
        invoiceDao.updateInvoiceStatus(invoiceId, status)
    }

    suspend fun getOutstandingBalance(): Double {
        return invoiceDao.getOutstandingBalance()
    }

    suspend fun getDailyRevenue(): Double {
        return invoiceDao.getDailyRevenue()
    }

    suspend fun getInvoiceCount(): Int {
        return invoiceDao.getInvoiceCount()
    }

    suspend fun deleteInvoice(invoiceId: Int) {
        invoiceDao.deleteInvoice(invoiceId)
    }

    suspend fun getPartialInvoices(): List<InvoiceEntity> {
        return invoiceDao.getPartialInvoices()
    }

    /**
     * Cleanup invoices that were auto-generated from downpayments only.
     * These should be removed and the payments should have invoiceId reset to 0.
     */
    suspend fun cleanupDownpaymentOnlyInvoices(paymentRepository: com.example.ivydentalapp.data.repository.PaymentRepository) {
        val partialInvoices = getPartialInvoices()
        for (invoice in partialInvoices) {
            // Skip if appointmentId is null
            val appointmentId = invoice.appointmentId ?: continue
            
            // Check if this invoice only has downpayments
            val payments = paymentRepository.getPaymentsByAppointmentId(appointmentId)
            val allDownpayments = payments.all { it.paymentType == "Downpayment" }
            
            if (allDownpayments && payments.isNotEmpty()) {
                // Reset payment invoice_id to 0
                payments.forEach { payment ->
                    val updated = payment.copy(invoiceId = 0)
                    paymentRepository.updatePayment(updated)
                }
                // Delete the orphaned invoice
                deleteInvoice(invoice.invoiceId)
                android.util.Log.d("InvoiceRepository", "Cleaned up orphaned invoice ${invoice.invoiceId}")
            }
        }
    }
}
