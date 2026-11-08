package com.example.ivydentalapp

import android.app.Application
import com.example.ivydentalapp.data.local.AppDatabase
import com.example.ivydentalapp.data.repository.AuthRepository
import com.example.ivydentalapp.data.repository.ServiceRepository
import com.example.ivydentalapp.data.service.PaymentSimulationService
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IvyDentalApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("IvyDentalApp", "=== Application onCreate START ===")
        // Force database initialization immediately
        val db = database
        android.util.Log.d("IvyDentalApp", "Database instance obtained")
        
        // Disabled: Create accounts immediately in a blocking way
        // User has existing data and doesn't want test accounts
        /*
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("IvyDentalApp", "Starting immediate account creation...")
                kotlinx.coroutines.delay(500) // Small delay to ensure DB is ready
                ensureTestAccountsExist(db)
            } catch (e: Exception) {
                android.util.Log.e("IvyDentalApp", "Error ensuring test accounts", e)
                e.printStackTrace()
            }
        }
        */
        android.util.Log.d("IvyDentalApp", "=== Application onCreate END ===")
    }
    
    private suspend fun ensureTestAccountsExist(database: AppDatabase) {
        android.util.Log.d("IvyDentalApp", "=== Ensuring Test Accounts Exist ===")
        initializeTestReceptionist(database)
        initializeTestManager(database)
        initializeTestPatients(database)
        // Skip cleanup to avoid blocking - only run if needed
        // cleanupDuplicateStaff(database)
        
        // Verify they exist
        val receptionist = database.receptionistDao().getReceptionistByUsername("receptionist")
        val manager = database.receptionistDao().getReceptionistByUsername("manager")
        
        android.util.Log.d("IvyDentalApp", "Final check - Receptionist exists: ${receptionist != null}")
        android.util.Log.d("IvyDentalApp", "Final check - Manager exists: ${manager != null}")
        
        if (receptionist == null) {
            android.util.Log.e("IvyDentalApp", "CRITICAL: Receptionist account still not found after initialization!")
            // Try creating again
            try {
                val testReceptionist = ReceptionistEntity(
                    username = "receptionist",
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "receptionist@ivydental.com",
                    password = "receptionist123",
                    role = "Receptionist",
                    phoneNumber = "+63 912 345 6789"
                )
                database.receptionistDao().insertReceptionist(testReceptionist)
                android.util.Log.d("IvyDentalApp", "Retry: Receptionist created")
            } catch (e: Exception) {
                android.util.Log.e("IvyDentalApp", "Retry failed", e)
            }
        }
        if (manager == null) {
            android.util.Log.e("IvyDentalApp", "CRITICAL: Manager account still not found after initialization!")
            // Try creating again
            try {
                val testManager = ReceptionistEntity(
                    username = "manager",
                    firstName = "Admin",
                    lastName = "User",
                    email = "manager@ivydental.com",
                    password = "manager123",
                    role = "Manager",
                    phoneNumber = "+63 999 888 7777"
                )
                database.receptionistDao().insertReceptionist(testManager)
                android.util.Log.d("IvyDentalApp", "Retry: Manager created")
            } catch (e: Exception) {
                android.util.Log.e("IvyDentalApp", "Retry failed", e)
            }
        }
    }
    
    // Lazy initialization of the database and repository
    val database by lazy { 
        android.util.Log.d("IvyDentalApp", "Database lazy initialization triggered")
        val db = AppDatabase.getDatabase(this)
        android.util.Log.d("IvyDentalApp", "Database instance created")
        // Initialize test users and services asynchronously (don't block)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if we even need to initialize (saves CPU/Battery)
                val staffCount = db.receptionistDao().getAllStaff().size
                if (staffCount < 2) {
                    android.util.Log.d("IvyDentalApp", "Database needs setup. Initializing...")
                    initializeTestReceptionist(db)
                    initializeTestManager(db)
                    initializeTestPatients(db)
                    initializeTestAppointments(db)
                    ServiceRepository(db.serviceDao()).initializeDefaultServices()
                } else {
                    // Just a subtle log so we know it's ready
                    android.util.Log.d("IvyDentalApp", "Database ready (Skip init)")
                }
            } catch (e: Exception) {
                android.util.Log.e("IvyDentalApp", "Initialization check failed", e)
            }
        }
        db
    }
    val authRepository by lazy { 
        AuthRepository(database.patientDao(), database.receptionistDao(), this)
    }
    val appointmentRepository by lazy { 
        com.example.ivydentalapp.data.repository.AppointmentRepository(database.appointmentDao())
    }
    val invoiceRepository by lazy { 
        com.example.ivydentalapp.data.repository.InvoiceRepository(database.invoiceDao())
    }
    val paymentRepository by lazy { 
        com.example.ivydentalapp.data.repository.PaymentRepository(database.paymentDao())
    }
    val serviceRepository by lazy { 
        ServiceRepository(database.serviceDao())
    }
    val paymentSimulationService by lazy { 
        PaymentSimulationService(database.paymentDao())
    }
    val notificationRepository by lazy { 
        com.example.ivydentalapp.data.repository.NotificationRepository(database.notificationDao())
    }

    private suspend fun initializeTestReceptionist(database: AppDatabase) {
        try {
            android.util.Log.d("IvyDentalApp", "=== Initializing Test Receptionist ===")
            val existing = database.receptionistDao().getReceptionistByUsername("receptionist")
            
            if (existing == null) {
                android.util.Log.d("IvyDentalApp", "Creating test receptionist account...")
                val testReceptionist = ReceptionistEntity(
                    username = "receptionist",
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "receptionist@ivydental.com",
                    password = "receptionist123",
                    role = "Receptionist",
                    phoneNumber = "+63 912 345 6789"
                )
                val id = database.receptionistDao().insertReceptionist(testReceptionist)
                android.util.Log.d("IvyDentalApp", "Test receptionist inserted with ID: $id")
                
                // Verify it was saved
                kotlinx.coroutines.delay(300)
                val verify = database.receptionistDao().getReceptionistByUsername("receptionist")
                if (verify != null) {
                    android.util.Log.d("IvyDentalApp", "✓ SUCCESS: Test receptionist verified")
                    android.util.Log.d("IvyDentalApp", "  Username: ${verify.username}")
                    android.util.Log.d("IvyDentalApp", "  Password: ${verify.password}")
                    android.util.Log.d("IvyDentalApp", "  Role: ${verify.role}")
                } else {
                    android.util.Log.e("IvyDentalApp", "✗ ERROR: Test receptionist was not saved!")
                }
            } else {
                android.util.Log.d("IvyDentalApp", "Test receptionist already exists:")
                android.util.Log.d("IvyDentalApp", "  Username: ${existing.username}")
                android.util.Log.d("IvyDentalApp", "  Role: ${existing.role}")
            }
        } catch (e: Exception) {
            android.util.Log.e("IvyDentalApp", "✗ ERROR initializing test receptionist", e)
            e.printStackTrace()
        }
    }

    private suspend fun initializeTestManager(database: AppDatabase) {
        try {
            android.util.Log.d("IvyDentalApp", "=== Initializing Test Manager ===")
            val existing = database.receptionistDao().getReceptionistByUsername("manager")
            
            if (existing == null) {
                android.util.Log.d("IvyDentalApp", "Creating test manager account...")
                val testManager = ReceptionistEntity(
                    username = "manager",
                    firstName = "Admin",
                    lastName = "User",
                    email = "manager@ivydental.com",
                    password = "manager123",
                    role = "Manager",
                    phoneNumber = "+63 999 888 7777"
                )
                val id = database.receptionistDao().insertReceptionist(testManager)
                android.util.Log.d("IvyDentalApp", "Test manager inserted with ID: $id")
                
                // Verify it was saved
                kotlinx.coroutines.delay(300)
                val verify = database.receptionistDao().getReceptionistByUsername("manager")
                if (verify != null) {
                    android.util.Log.d("IvyDentalApp", "✓ SUCCESS: Test manager verified")
                    android.util.Log.d("IvyDentalApp", "  Username: ${verify.username}")
                    android.util.Log.d("IvyDentalApp", "  Password: ${verify.password}")
                    android.util.Log.d("IvyDentalApp", "  Role: ${verify.role}")
                } else {
                    android.util.Log.e("IvyDentalApp", "✗ ERROR: Test manager was not saved!")
                }
            } else {
                android.util.Log.d("IvyDentalApp", "Test manager already exists:")
                android.util.Log.d("IvyDentalApp", "  Username: ${existing.username}")
                android.util.Log.d("IvyDentalApp", "  Role: ${existing.role}")
            }
        } catch (e: Exception) {
            android.util.Log.e("IvyDentalApp", "✗ ERROR initializing test manager", e)
            e.printStackTrace()
        }
    }

    private suspend fun initializeTestPatients(database: AppDatabase) {
        try {
            android.util.Log.d("IvyDentalApp", "=== Initializing Test Patients ===")
            // Patient 1
            var existing = database.patientDao().getPatientByUsername("patient1")
            if (existing == null) {
                val p1 = com.example.ivydentalapp.data.local.entity.PatientEntity(
                    username = "patient1",
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@email.com",
                    password = "password123",
                    primaryPhone = "09123456789",
                    dob = "1990-01-01",
                    gender = "Male",
                    bloodType = "O+"
                )
                database.patientDao().insertPatient(p1)
                android.util.Log.d("IvyDentalApp", "Test patient1 created")
            }

            // Patient 2
            existing = database.patientDao().getPatientByUsername("patient2")
            if (existing == null) {
                val p2 = com.example.ivydentalapp.data.local.entity.PatientEntity(
                    username = "patient2",
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "jane.smith@email.com",
                    password = "password123",
                    primaryPhone = "09987654321",
                    dob = "1995-05-15",
                    gender = "Female",
                    bloodType = "A+"
                )
                database.patientDao().insertPatient(p2)
                android.util.Log.d("IvyDentalApp", "Test patient2 created")
            }
        } catch (e: Exception) {
            android.util.Log.e("IvyDentalApp", "Error initializing test patients", e)
        }
    }
    
    private suspend fun cleanupDuplicateStaff(database: AppDatabase) {
        try {
            android.util.Log.d("IvyDentalApp", "=== Cleaning Up Duplicate Staff ===")
            val allStaff = database.receptionistDao().getAllStaff()
            android.util.Log.d("IvyDentalApp", "Total staff found: ${allStaff.size}")
            
            if (allStaff.isEmpty()) {
                android.util.Log.d("IvyDentalApp", "No staff found, skipping cleanup")
                return
            }
            
            // Only run cleanup if there are more than 2 staff members (expected: 1 receptionist + 1 manager)
            if (allStaff.size <= 2) {
                android.util.Log.d("IvyDentalApp", "Only ${allStaff.size} staff members found, no cleanup needed")
                return
            }
            
            var deletedCount = 0
            
            // Handle duplicates by username first (most important - same username should only appear once)
            val staffByUsername = allStaff.groupBy { it.username }
            staffByUsername.forEach { (username, staffList) ->
                if (staffList.size > 1) {
                    android.util.Log.d("IvyDentalApp", "Found ${staffList.size} entries with username '$username', keeping one and deleting others")
                    // Keep the one with the lowest ID (likely the first created)
                    val toKeep = staffList.minByOrNull { it.staffId } ?: staffList.first()
                    staffList.filter { it.staffId != toKeep.staffId }.forEach { duplicate ->
                        android.util.Log.d("IvyDentalApp", "Deleting duplicate staff: ID=${duplicate.staffId}, username=${duplicate.username}, role=${duplicate.role}")
                        try {
                            database.receptionistDao().deleteStaffById(duplicate.staffId)
                            deletedCount++
                        } catch (e: Exception) {
                            android.util.Log.e("IvyDentalApp", "Error deleting staff ID ${duplicate.staffId}", e)
                        }
                    }
                }
            }
            
            // After username cleanup, check role-based duplicates
            val remainingStaff = database.receptionistDao().getAllStaff()
            
            // Keep only one receptionist (the first one found by username "receptionist")
            val receptionists = remainingStaff.filter { it.role == "Receptionist" }
            if (receptionists.size > 1) {
                android.util.Log.d("IvyDentalApp", "Found ${receptionists.size} receptionists, keeping one and deleting others")
                // Prefer keeping the one with username "receptionist"
                val toKeep = receptionists.find { it.username == "receptionist" } 
                    ?: receptionists.minByOrNull { it.staffId } 
                    ?: receptionists.first()
                receptionists.filter { it.staffId != toKeep.staffId }.forEach { duplicate ->
                    android.util.Log.d("IvyDentalApp", "Deleting duplicate receptionist: ID=${duplicate.staffId}, username=${duplicate.username}")
                    try {
                        database.receptionistDao().deleteStaffById(duplicate.staffId)
                        deletedCount++
                    } catch (e: Exception) {
                        android.util.Log.e("IvyDentalApp", "Error deleting receptionist ID ${duplicate.staffId}", e)
                    }
                }
            }
            
            // Keep only one manager (the first one found by username "manager")
            val managers = remainingStaff.filter { it.role == "Manager" }
            if (managers.size > 1) {
                android.util.Log.d("IvyDentalApp", "Found ${managers.size} managers, keeping one and deleting others")
                // Prefer keeping the one with username "manager"
                val toKeep = managers.find { it.username == "manager" } 
                    ?: managers.minByOrNull { it.staffId } 
                    ?: managers.first()
                managers.filter { it.staffId != toKeep.staffId }.forEach { duplicate ->
                    android.util.Log.d("IvyDentalApp", "Deleting duplicate manager: ID=${duplicate.staffId}, username=${duplicate.username}")
                    try {
                        database.receptionistDao().deleteStaffById(duplicate.staffId)
                        deletedCount++
                    } catch (e: Exception) {
                        android.util.Log.e("IvyDentalApp", "Error deleting manager ID ${duplicate.staffId}", e)
                    }
                }
            }
            
            val finalCount = database.receptionistDao().getAllStaff().size
            android.util.Log.d("IvyDentalApp", "Cleanup complete. Deleted $deletedCount duplicates. Final staff count: $finalCount")
        } catch (e: Exception) {
            android.util.Log.e("IvyDentalApp", "✗ ERROR cleaning up duplicate staff", e)
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeTestAppointments(database: AppDatabase) {
        try {
            android.util.Log.d("IvyDentalApp", "=== Initializing Test Appointments ===")
            
            // Get patient IDs
            val patient1 = database.patientDao().getPatientByUsername("patient1")
            val patient2 = database.patientDao().getPatientByUsername("patient2")
            
            if (patient1 == null || patient2 == null) {
                android.util.Log.e("IvyDentalApp", "Test patients not found, skipping appointment creation")
                return
            }
            
            // Get services
            val services = database.serviceDao().getAllServicesSync()
            if (services.isEmpty()) {
                android.util.Log.e("IvyDentalApp", "No services found, skipping appointment creation")
                return
            }
            
            val cleaning = services.find { it.serviceName.contains("Cleaning", ignoreCase = true) } ?: services[0]
            val filling = services.find { it.serviceName.contains("Filling", ignoreCase = true) } ?: services.getOrNull(1) ?: services[0]
            
            // Check if appointments already exist
            val existingAppts = database.appointmentDao().getAllAppointments()
            if (existingAppts.isNotEmpty()) {
                android.util.Log.d("IvyDentalApp", "Appointments already exist (${existingAppts.size}), skipping creation")
                return
            }
            
            // Create appointments for patient1
            val appt1 = com.example.ivydentalapp.data.local.entity.AppointmentEntity(
                patientId = patient1.patientId,
                dentistId = 1, // Default dentist
                scheduledDatetime = "2026-02-10 10:00:00",
                durationMinutes = 60,
                serviceType = cleaning.serviceName,
                price = cleaning.basePrice,
                serviceId = cleaning.serviceId,
                status = "Scheduled"
            )
            database.appointmentDao().insertAppointment(appt1)
            android.util.Log.d("IvyDentalApp", "Created appointment 1 for ${patient1.firstName}")
            
            val appt2 = com.example.ivydentalapp.data.local.entity.AppointmentEntity(
                patientId = patient1.patientId,
                dentistId = 1,
                scheduledDatetime = "2026-02-15 14:00:00",
                durationMinutes = 90,
                serviceType = filling.serviceName,
                price = filling.basePrice,
                serviceId = filling.serviceId,
                status = "Scheduled"
            )
            database.appointmentDao().insertAppointment(appt2)
            android.util.Log.d("IvyDentalApp", "Created appointment 2 for ${patient1.firstName}")
            
            // Create appointment for patient2
            val appt3 = com.example.ivydentalapp.data.local.entity.AppointmentEntity(
                patientId = patient2.patientId,
                dentistId = 1,
                scheduledDatetime = "2026-02-12 11:00:00",
                durationMinutes = 60,
                serviceType = cleaning.serviceName,
                price = cleaning.basePrice,
                serviceId = cleaning.serviceId,
                status = "Scheduled"
            )
            database.appointmentDao().insertAppointment(appt3)
            android.util.Log.d("IvyDentalApp", "Created appointment 3 for ${patient2.firstName}")
            
            android.util.Log.d("IvyDentalApp", "✓ Test appointments created successfully")
        } catch (e: Exception) {
            android.util.Log.e("IvyDentalApp", "Error initializing test appointments", e)
        }
    }
}
