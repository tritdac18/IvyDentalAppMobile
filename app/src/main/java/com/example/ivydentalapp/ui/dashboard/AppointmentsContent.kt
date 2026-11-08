package com.example.ivydentalapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivydentalapp.ui.theme.Primary
import com.example.ivydentalapp.ui.appointment.PaymentSimulationDialog
import com.example.ivydentalapp.ui.appointment.AppointmentUiState
import com.example.ivydentalapp.ui.appointment.PendingAppointment
import com.example.ivydentalapp.ui.appointment.Service
import com.example.ivydentalapp.ui.appointment.Doctor
import com.example.ivydentalapp.ui.appointment.BookAppointmentContent
import com.example.ivydentalapp.ui.appointment.GCashDownpaymentDialog
import com.example.ivydentalapp.ui.appointment.AppointmentDetailsActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ... (existing imports will be handled by context matching, but I need to ensure java.time are added)

@Composable
fun AppointmentsContent(
    currentUser: com.example.ivydentalapp.data.local.entity.PatientEntity?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.example.ivydentalapp.IvyDentalApplication
    val viewModel: com.example.ivydentalapp.ui.appointment.AppointmentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.example.ivydentalapp.ui.appointment.AppointmentViewModelFactory(
            app.appointmentRepository,
            app.invoiceRepository,
            app.paymentSimulationService,
            app.notificationRepository
        )
    )

    var showBookingForm by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Initial Load
    LaunchedEffect(currentUser) {
        currentUser?.let { viewModel.loadAppointments(it.patientId) }
    }
    
    // Check booking success
    LaunchedEffect(uiState.isBookingSuccess) {
        if (uiState.isBookingSuccess) {
            showBookingForm = false
            currentUser?.let { viewModel.loadAppointments(it.patientId) }
            viewModel.resetBookingSuccess()
             android.widget.Toast.makeText(context, "Appointment booked successfully!", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    // Show errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
             android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
             // access the viewmodel to clear error if method exists, otherwise it might persist
             viewModel.clearError() // Assuming method exists or I add it
        }
    }

    if (showBookingForm) {
        com.example.ivydentalapp.ui.appointment.BookAppointmentContent(
            viewModel = viewModel,
            services = viewModel.availableServices,
            doctors = viewModel.availableDoctors,
            onConfirm = { service, doctor, date, time ->
                currentUser?.let {
                    viewModel.proceedToDownpayment(it.patientId, service, doctor, date, time)
                }
            },
            onCancel = { showBookingForm = false }
        )
    } else {
        // Existing Calendar/List View
        Box(modifier = Modifier.fillMaxSize()) {
            AppointmentList(appointments = uiState.appointments, onNewBooking = { showBookingForm = true })
            
            // FAB
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(com.example.ivydentalapp.ui.theme.BlueGradient)
                    .clickable { showBookingForm = true }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Booking", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // GCash Downpayment Dialog
    val pendingAppointment = uiState.pendingAppointment
    if (uiState.showDownpaymentDialog && pendingAppointment != null) {
        val downpaymentAmount = pendingAppointment.service.price * 0.5
        com.example.ivydentalapp.ui.appointment.GCashDownpaymentDialog(
            amount = downpaymentAmount,
            onConfirm = { referenceNumber, accountName ->
                viewModel.processDownpayment(referenceNumber, accountName)
            },
            onDismiss = {
                viewModel.dismissDownpaymentDialog()
            }
        )
    }
}

@Composable
fun AppointmentList(
    appointments: List<com.example.ivydentalapp.data.local.entity.AppointmentEntity>, 
    onNewBooking: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedFilter by remember { mutableStateOf("Upcoming") } // Upcoming, Completed
    var searchQuery by remember { mutableStateOf("") }

    val filteredAppointments = remember(appointments, selectedFilter, searchQuery) {
        appointments.filter { appointment ->
            val matchesFilter = when (selectedFilter) {
                "Upcoming" -> appointment.status in listOf("Scheduled", "Pending", "Confirmed")
                "Completed" -> appointment.status in listOf("Completed", "Cancelled", "No Show", "Paid")
                else -> true
            }
            
            val matchesSearch = if (searchQuery.isBlank()) true else {
                (appointment.serviceType?.contains(searchQuery, ignoreCase = true) == true) ||
                (appointment.scheduledDatetime.contains(searchQuery, ignoreCase = true)) ||
                (appointment.status.contains(searchQuery, ignoreCase = true))
            }
            
            matchesFilter && matchesSearch
        }.sortedBy { it.scheduledDatetime } // Sort by date
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Appointments",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search service, date, or status") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha=0.5f)
                )
            )
        }
        
        // Filter Tabs
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "Upcoming",
                    onClick = { selectedFilter = "Upcoming" },
                    label = { Text("Upcoming") }
                )
                FilterChip(
                    selected = selectedFilter == "Completed",
                    onClick = { selectedFilter = "Completed" },
                    label = { Text("Completed") }
                )
            }
        }

        item {
            CalendarView(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it },
                appointments = filteredAppointments // Show filtered on calendar to match list view context
            )
        }
        
        if (filteredAppointments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No appointments found", color = Color.Gray)
                }
            }
        } else {
            items(filteredAppointments) { appointment ->
                val context = androidx.compose.ui.platform.LocalContext.current
                AppointmentEntityCard(appointment, onClick = {
                    val intent = android.content.Intent(context, com.example.ivydentalapp.ui.appointment.AppointmentDetailsActivity::class.java).apply {
                        putExtra("APPOINTMENT_ID", appointment.appointmentId)
                    }
                    context.startActivity(intent)
                })
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    appointments: List<com.example.ivydentalapp.data.local.entity.AppointmentEntity>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                    }
                    IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DayOfWeek.values().forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Days Grid
            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // adjust to 0-6 (Sun-Sat)? No DayOfWeek is 1-7 (Mon-Sun).
            // Let's assume Mon-Sun for simplicity or standard java.time
            // Java DayOfWeek: 1=Mon, 7=Sun.
            // If we want Sun start, we handle it. standard is often Mon or Sun.
            // Let's stick to Mon start as per DayOfWeek.values() order.
            
            val dayOffset = currentMonth.atDay(1).dayOfWeek.value - 1 // 0 for Mon
            
            val totalCells = daysInMonth + dayOffset
            val rows = (totalCells + 6) / 7
            
            Column {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val dayIndex = row * 7 + col - dayOffset + 1
                            if (dayIndex in 1..daysInMonth) {
                                val date = currentMonth.atDay(dayIndex)
                                val hasAppointment = appointments.any { 
                                    it.scheduledDatetime.startsWith(date.toString()) 
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .background(
                                            if (hasAppointment) Primary.copy(alpha=0.2f) else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayIndex.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (date == LocalDate.now()) Primary else Color.Black,
                                        fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentEntityCard(
    appointment: com.example.ivydentalapp.data.local.entity.AppointmentEntity,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ivydentalapp.ui.theme.BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .background(com.example.ivydentalapp.ui.theme.BackgroundLight, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dateTimeParts = appointment.scheduledDatetime.split(" ")
                val date = if(dateTimeParts.isNotEmpty()) dateTimeParts[0] else ""
                val time = if(dateTimeParts.size > 1) dateTimeParts[1] else ""
                
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ivydentalapp.ui.theme.TextDark
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.serviceType ?: "General Service", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Price: ₱${appointment.price}", style = MaterialTheme.typography.bodySmall, color = com.example.ivydentalapp.ui.theme.TextGray)
            }
            
            Surface(
                color = when(appointment.status) {
                    "Scheduled" -> com.example.ivydentalapp.ui.theme.Primary.copy(alpha=0.1f)
                    "Pending" -> com.example.ivydentalapp.ui.theme.Pending.copy(alpha=0.1f)
                    "Completed" -> com.example.ivydentalapp.ui.theme.Success.copy(alpha=0.1f)
                    else -> com.example.ivydentalapp.ui.theme.TextGray.copy(alpha=0.1f)
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = appointment.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when(appointment.status) {
                        "Scheduled" -> com.example.ivydentalapp.ui.theme.Primary
                        "Pending" -> com.example.ivydentalapp.ui.theme.Pending
                        "Completed" -> com.example.ivydentalapp.ui.theme.Success
                        else -> com.example.ivydentalapp.ui.theme.TextGray
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
