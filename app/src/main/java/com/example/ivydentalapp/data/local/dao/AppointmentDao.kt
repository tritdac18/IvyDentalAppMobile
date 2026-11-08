package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM tbl_appointments WHERE patient_id = :patientId ORDER BY scheduled_datetime DESC")
    fun getAppointmentsForPatient(patientId: Int): Flow<List<AppointmentEntity>>

    @Query("""
        SELECT COUNT(*) FROM tbl_appointments
        WHERE dentist_id = :dentistId
          AND scheduled_datetime = :scheduledTime
          AND status IN ('Pending','Scheduled','Confirmed')
    """)
    suspend fun countConflicts(dentistId: Int, scheduledTime: String): Int

    @Query("SELECT * FROM tbl_appointments WHERE appointment_id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Int): AppointmentEntity?

    @Query("UPDATE tbl_appointments SET status = :status WHERE appointment_id = :appointmentId")
    suspend fun updateAppointmentStatus(appointmentId: Int, status: String)

    @Query("""
        SELECT a.* FROM tbl_appointments a
        LEFT JOIN tbl_invoices i ON a.appointment_id = i.appointment_id
        WHERE a.status IN ('Scheduled', 'Confirmed', 'Completed')
        AND i.invoice_id IS NULL
        ORDER BY a.scheduled_datetime DESC
    """)
    suspend fun getAppointmentsWithoutInvoices(): List<AppointmentEntity>

    @Query("""
        SELECT a.* FROM tbl_appointments a
        LEFT JOIN tbl_invoices i ON a.appointment_id = i.appointment_id
        WHERE a.patient_id = :patientId
        AND a.status IN ('Scheduled', 'Confirmed', 'Completed')
        AND i.invoice_id IS NULL
        ORDER BY a.scheduled_datetime DESC
    """)
    suspend fun getAppointmentsWithoutInvoicesForPatient(patientId: Int): List<AppointmentEntity>

    @Query("""
        SELECT * FROM tbl_appointments
        WHERE patient_id = :patientId
          AND status = 'Completed'
    """)
    suspend fun getCompletedAppointmentsForPatient(patientId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM tbl_appointments ORDER BY scheduled_datetime DESC")
    suspend fun getAllAppointments(): List<AppointmentEntity>

    @Query("""
        SELECT * FROM tbl_appointments
        WHERE DATE(scheduled_datetime) = DATE(:date)
        ORDER BY scheduled_datetime ASC
    """)
    suspend fun getAppointmentsByDate(date: String): List<AppointmentEntity>

    @Query("""
        SELECT * FROM tbl_appointments
        WHERE dentist_id = :dentistId
          AND DATE(scheduled_datetime) = DATE(:date)
          AND status IN ('Pending', 'Scheduled', 'Confirmed')
        ORDER BY scheduled_datetime ASC
    """)
    suspend fun getDentistAppointmentsByDate(dentistId: Int, date: String): List<AppointmentEntity>

    @Query("""
        SELECT COUNT(*) FROM tbl_appointments
        WHERE dentist_id = :dentistId
          AND DATE(scheduled_datetime) = DATE(:date)
          AND status IN ('Pending', 'Scheduled', 'Confirmed')
    """)
    suspend fun countDentistAppointmentsByDate(dentistId: Int, date: String): Int
}
