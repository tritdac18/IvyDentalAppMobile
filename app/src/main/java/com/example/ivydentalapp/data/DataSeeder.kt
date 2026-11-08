package com.example.ivydentalapp.data

import android.util.Log
import com.example.ivydentalapp.data.local.AppDatabase
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.PatientEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DataSeeder {

    fun seedData(database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("DataSeeder", "Starting data seeding...")
                
                // 1. Seed Patients
                if (database.patientDao().getAllPatients().isEmpty()) {
                        val patients = listOf(
                        PatientEntity(
                            username = "jdoe",
                            firstName = "John",
                            lastName = "Doe",
                            email = "john.doe@example.com",
                            password = "password123",
                            dob = "1990-01-01",
                            status = "Active",
                            createdAt = LocalDateTime.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            primaryPhone = "09171234567"
                        ),
                         PatientEntity(
                            username = "asmith",
                            firstName = "Alice",
                            lastName = "Smith",
                            email = "alice.smith@example.com",
                            password = "password123",
                             dob = "1995-05-15",
                            status = "Active",
                            createdAt = LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            primaryPhone = "09181234567"
                        ),
                        PatientEntity(
                            username = "bobb",
                            firstName = "Bob",
                            lastName = "Brown",
                            email = "bob.brown@example.com",
                            password = "password123",
                             dob = "1985-08-20",
                            status = "Active",
                             createdAt = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                             primaryPhone = "09191234567"
                        )
                    )
                    
                    patients.forEach { 
                        database.patientDao().insertPatient(it) 
                    }
                    Log.d("DataSeeder", "Seeded ${patients.size} patients.")
                }

                // 2. Seed Appointments
                if (database.appointmentDao().getAllAppointments().isEmpty()) {
                    val patients = database.patientDao().getAllPatients()
                    if (patients.isNotEmpty()) {
                        val today = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        
                        val appointments = listOf(
                            // Upcoming
                            AppointmentEntity(
                                patientId = patients[0].patientId,
                                dentistId = 1, // Assuming Dentist ID 1 exists
                                scheduledDatetime = today.plusDays(1).withHour(10).withMinute(0).format(formatter),
                                status = "Scheduled",
                                serviceType = "Dental Cleaning",
                                serviceId = 0,
                                price = 1500.0
                            ),
                            AppointmentEntity(
                                patientId = patients[1].patientId,
                                dentistId = 1,
                                scheduledDatetime = today.plusDays(2).withHour(14).withMinute(0).format(formatter),
                                status = "Confirmed",
                                serviceType = "Tooth Extraction",
                                serviceId = 0,
                                price = 2500.0
                            ),
                            // Completed (for reports)
                            AppointmentEntity(
                                patientId = patients[0].patientId,
                                dentistId = 1,
                                scheduledDatetime = today.minusDays(5).withHour(9).withMinute(0).format(formatter),
                                status = "Completed",
                                serviceType = "Consultation",
                                serviceId = 0,
                                price = 500.0
                            ),
                             AppointmentEntity(
                                patientId = patients[2].patientId,
                                dentistId = 1,
                                scheduledDatetime = today.minusDays(1).withHour(11).withMinute(0).format(formatter),
                                status = "Completed",
                                serviceType = "Whitening",
                                serviceId = 0,
                                price = 5000.0
                            )
                        )
                        
                        appointments.forEach {
                            database.appointmentDao().insertAppointment(it)
                        }
                        Log.d("DataSeeder", "Seeded ${appointments.size} appointments.")
                    }
                }
                
                Log.d("DataSeeder", "Data seeding complete.")
                
            } catch (e: Exception) {
                Log.e("DataSeeder", "Error seeding data", e)
            }
        }
    }
}
