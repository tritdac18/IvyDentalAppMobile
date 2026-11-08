package com.example.ivydentalapp.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ManagerReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IvyDentalApplication
    
    // UI State
    data class ReportsUiState(
        val totalRevenue: Double = 0.0,
        val totalVisits: Int = 0,
        val totalBookings: Int = 0,
        val newCustomers: Int = 0,
        val transactions: List<PaymentEntity> = emptyList(),
        val appointmentHistory: List<AppointmentEntity> = emptyList(),
        val isLoading: Boolean = false,
        val revenueChange: String = "+0%", // Placeholder calculation logic
        val visitsChange: String = "+0%",
        val bookingsChange: String = "+0%",
        val newCustomersChange: String = "+0%"
    )

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    private var allPayments: List<PaymentEntity> = emptyList()
    private var allAppointments: List<AppointmentEntity> = emptyList()
    private var allPatients: List<PatientEntity> = emptyList()

    fun loadData(period: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Fetch all data initially if not loaded 
            // In a real app with large data, we would query with date ranges directly from specific DAOs
            // taking advantage of SQL filtering. For now, we load all and filter in memory for simplicity given the scope.
            
            // To properly implement "New Customers", we need created_at in PatientEntity.
            // If it's missing or null in existing data, we might assume 0 or count by fallback.
            
            // Fetch concurrent data
            val payments = app.database.paymentDao().getAllPayments().first()
            
            val appointments = app.appointmentRepository.getAllAppointments()
            val patients = app.database.patientDao().getAllPatients()

            allPayments = payments
            allAppointments = appointments
            allPatients = patients

            calculateMetrics(period)
        }
    }
    


    fun calculateMetrics(period: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val (startDate, endDate) = getDateRange(period)
            
            // Filtering logic
            val filteredPayments = allPayments.filter { payment ->
                isDateInRange(payment.createdAt, startDate, endDate)
            }
            
            // "Bookings" likely refers to appointments MADE or SCHEDULED in that period? 
            // Or appointments happening in that period? Usually reports track "Appointments scheduled for this period".
            val filteredAppointments = allAppointments.filter { appointment ->
                isDateInRange(appointment.scheduledDatetime, startDate, endDate)
            }
            
            val completedAppointments = filteredAppointments.filter { it.status == "Completed" }
            
            val filteredPatients = allPatients.filter { patient ->
                 isDateInRange(patient.createdAt, startDate, endDate)
            }

            val totalRevenue = filteredPayments.sumOf { it.amountPaid }
            val totalVisits = completedAppointments.size // Visits = Completed appointments
            val totalBookings = filteredAppointments.size
            val newCustomers = filteredPatients.size

            _uiState.value = ReportsUiState(
                totalRevenue = totalRevenue,
                totalVisits = totalVisits,
                totalBookings = totalBookings,
                newCustomers = newCustomers,
                transactions = filteredPayments.sortedByDescending { it.createdAt },
                appointmentHistory = completedAppointments.sortedByDescending { it.scheduledDatetime },
                isLoading = false
            )
        }
    }

    private fun getDateRange(period: String): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            "Today" -> today to today
            "This Week" -> today.minusDays(today.dayOfWeek.value.toLong() - 1) to today // Start of week (Mon) to Today
            "This Month" -> today.withDayOfMonth(1) to today // Start of month to Today
            "Last 30 Days" -> today.minusDays(30) to today
            "This Year" -> today.withDayOfYear(1) to today
            else -> today.minusDays(30) to today
        }
    }

    private fun isDateInRange(dateString: String?, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (dateString.isNullOrBlank()) return false
        return try {
            // Check formatted string. Assuming ISO-like or "yyyy-MM-dd HH:mm"
            // Most entries seemed to simply have dates.
            // Let's try to parse flexibly.
            val dateStr = dateString.split(" ")[0] // Take date part
            val date = LocalDate.parse(dateStr) // Standard ISO parse (yyyy-MM-dd)
            !date.isBefore(startDate) && !date.isAfter(endDate)
        } catch (e: Exception) {
            // Fallback for different formats? 
            // In the app, dates seem to be "2025-01-20" or similar.
            false
        }
    }
}

class ManagerReportsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManagerReportsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
