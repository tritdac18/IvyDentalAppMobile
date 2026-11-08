package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "payment_id")
    val paymentId: Int = 0,

    @ColumnInfo(name = "invoice_id")
    val invoiceId: Int = 0,

    @ColumnInfo(name = "appointment_id")
    val appointmentId: Int? = null,

    @ColumnInfo(name = "patient_id")
    val patientId: Int,

    @ColumnInfo(name = "amount_paid")
    val amountPaid: Double,

    @ColumnInfo(name = "status")
    val status: String = "Pending", // Pending, Paid, Failed, Completed

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Cash", // Cash, GCash

    @ColumnInfo(name = "payment_type")
    val paymentType: String = "Invoice Payment", // Invoice Payment, Downpayment

    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null, // GCash reference number

    @ColumnInfo(name = "gcash_account_name")
    val gcashAccountName: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
