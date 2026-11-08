package com.example.ivydentalapp.data.repository

import com.example.ivydentalapp.data.local.dao.AppointmentDao
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val appointmentDao: AppointmentDao) {
    suspend fun bookAppointment(appointment: AppointmentEntity): Result<Long> {
        return try {
            // Parse datetime to check for conflicts based on duration
            // Support AM/PM format from UI ("yyyy-MM-dd hh:mm a")
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", java.util.Locale.US)
            
            val scheduledDateTime = java.time.LocalDateTime.parse(
                appointment.scheduledDatetime,
                formatter
            )
            val endDateTime = scheduledDateTime.plusMinutes(appointment.durationMinutes.toLong())
            
            // Get all appointments for this dentist on the same date
            val dateStr = scheduledDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val existingAppointments = appointmentDao.getDentistAppointmentsByDate(appointment.dentistId, dateStr)
            
            // Check for time overlaps
            for (existing in existingAppointments) {
                // Try parsing existing appointments. If they are legacy (24h) this might fail, 
                // but we assume new format for now or catch exception to be safe?
                // For now, assume consistent format.
                val existingStart = try {
                    java.time.LocalDateTime.parse(existing.scheduledDatetime, formatter)
                } catch (e: Exception) {
                     // Fallback for legacy 24h format if needed, or log and skip
                     try {
                         java.time.LocalDateTime.parse(existing.scheduledDatetime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                     } catch (e2: Exception) {
                         continue // Skip invalid dates
                     }
                }
                
                val existingEnd = existingStart.plusMinutes(existing.durationMinutes.toLong())
                
                // Check if there's any overlap
                if (scheduledDateTime.isBefore(existingEnd) && endDateTime.isAfter(existingStart)) {
                    return Result.failure(Exception("Time slot conflicts with existing appointment"))
                }
            }
            
            val id = appointmentDao.insertAppointment(appointment)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAppointments(patientId: Int): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsForPatient(patientId)
    }

    suspend fun getAppointmentById(appointmentId: Int): AppointmentEntity? {
        return appointmentDao.getAppointmentById(appointmentId)
    }

    suspend fun updateAppointmentStatus(appointmentId: Int, status: String) {
        appointmentDao.updateAppointmentStatus(appointmentId, status)
    }

    suspend fun getAppointmentsWithoutInvoices(): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsWithoutInvoices()
    }

    suspend fun getAppointmentsWithoutInvoicesForPatient(patientId: Int): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsWithoutInvoicesForPatient(patientId)
    }

    suspend fun getCompletedAppointments(patientId: Int): List<AppointmentEntity> {
        return appointmentDao.getCompletedAppointmentsForPatient(patientId)
    }

    suspend fun getAllAppointments(): List<AppointmentEntity> {
        return appointmentDao.getAllAppointments()
    }

    suspend fun getAppointmentsByDate(date: String): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsByDate(date)
    }

    suspend fun getDentistAppointmentsByDate(dentistId: Int, date: String): List<AppointmentEntity> {
        return appointmentDao.getDentistAppointmentsByDate(dentistId, date)
    }

    suspend fun countDentistAppointmentsByDate(dentistId: Int, date: String): Int {
        return appointmentDao.countDentistAppointmentsByDate(dentistId, date)
    }
}
