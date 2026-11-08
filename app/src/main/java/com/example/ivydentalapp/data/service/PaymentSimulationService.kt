package com.example.ivydentalapp.data.service

import com.example.ivydentalapp.data.local.dao.PaymentDao
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import kotlinx.coroutines.delay

/**
 * Simulates payment processing for the app-only implementation
 * Uses GCash for online payments
 */
class PaymentSimulationService(
    private val paymentDao: PaymentDao
) {
    /**
     * Create a GCash payment with reference number and account name
     */
    suspend fun createGCashPayment(
        invoiceId: Int,
        amount: Double,
        referenceNumber: String,
        accountName: String,
        patientId: Int,
        appointmentId: Int? = null
    ): Long {
        val payment = PaymentEntity(
            invoiceId = invoiceId,
            appointmentId = appointmentId,
            patientId = patientId,
            amountPaid = amount,
            status = "Pending",
            paymentMethod = "GCash",
            paymentType = "Invoice Payment",
            transactionId = referenceNumber,
            gcashAccountName = accountName,
            createdAt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        
        return paymentDao.insertPayment(payment)
    }

    suspend fun createCashPayment(
        invoiceId: Int,
        amount: Double,
        patientId: Int,
        appointmentId: Int? = null,
        notes: String? = null
    ): Long {
        val payment = PaymentEntity(
            invoiceId = invoiceId,
            appointmentId = appointmentId,
            patientId = patientId,
            amountPaid = amount,
            status = "Completed",
            paymentMethod = "Cash",
            paymentType = "Invoice Payment",
            notes = notes,
            createdAt = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        
        return paymentDao.insertPayment(payment)
    }

    /**
     * Simulate processing a payment
     * In real scenario, this would be called by webhook or payment status check
     */
    suspend fun processPayment(paymentId: Int, success: Boolean = true): Boolean {
        val payment = paymentDao.getPaymentById(paymentId) ?: return false
        
        val updatedPayment = payment.copy(
            status = if (success) "Paid" else "Failed"
        )
        
        paymentDao.updatePayment(updatedPayment)
        return success
    }

    /**
     * Verify GCash payment by reference number
     */
    suspend fun verifyGCashPayment(referenceNumber: String): PaymentEntity? {
        val allPayments = paymentDao.getPendingPayments()
        return allPayments.find { it.transactionId == referenceNumber }
    }

    /**
     * Get payment by ID (for internal use)
     */
    suspend fun getPaymentById(paymentId: Int): PaymentEntity? {
        return paymentDao.getPaymentById(paymentId)
    }
    
    /**
     * Update payment entity
     */
    suspend fun updatePayment(payment: PaymentEntity) {
        paymentDao.updatePayment(payment)
    }

    /**
     * Simulate payment completion with delay (for demo purposes)
     */
    suspend fun simulatePaymentCompletion(paymentId: Int, delaySeconds: Long = 2): Boolean {
        delay(delaySeconds * 1000)
        return processPayment(paymentId, success = true)
    }
}


