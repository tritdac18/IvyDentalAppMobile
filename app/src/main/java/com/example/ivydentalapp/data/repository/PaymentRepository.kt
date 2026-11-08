package com.example.ivydentalapp.data.repository

import com.example.ivydentalapp.data.local.dao.PaymentDao
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {
    fun getPaymentsForInvoice(invoiceId: Int): Flow<List<PaymentEntity>> {
        return paymentDao.getPaymentsForInvoice(invoiceId)
    }

    fun getAllPayments(): Flow<List<PaymentEntity>> {
        return paymentDao.getAllPayments()
    }

    suspend fun insertPayment(payment: PaymentEntity): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun getPaymentById(paymentId: Int): PaymentEntity? {
        return paymentDao.getPaymentById(paymentId)
    }

    suspend fun updatePayment(payment: PaymentEntity) {
        paymentDao.updatePayment(payment)
    }

    suspend fun getPendingPayments(): List<PaymentEntity> {
        return paymentDao.getPendingPayments()
    }

    suspend fun getPaymentsForInvoices(invoiceIds: List<Int>): List<PaymentEntity> {
        return paymentDao.getPaymentsForInvoices(invoiceIds)
    }

    suspend fun getTotalDownpaymentForInvoice(invoiceId: Int): Double {
        return paymentDao.getTotalDownpaymentForInvoice(invoiceId)
    }

    suspend fun getPaymentsByAppointmentId(appointmentId: Int): List<PaymentEntity> {
        return paymentDao.getPaymentsByAppointmentId(appointmentId)
    }
}

