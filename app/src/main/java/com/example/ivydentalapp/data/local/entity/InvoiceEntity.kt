package com.example.ivydentalapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "invoice_id")
    val invoiceId: Int = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Int?,

    @ColumnInfo(name = "appointment_id")
    val appointmentId: Int?,

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double?,

    @ColumnInfo(name = "payment_status")
    val paymentStatus: String? = "Unpaid", // Unpaid, Paid, Partial, Collection

    @ColumnInfo(name = "generated_at")
    val generatedAt: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
