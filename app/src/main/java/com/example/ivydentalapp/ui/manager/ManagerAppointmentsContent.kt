package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.ui.theme.Primary
import com.example.ivydentalapp.ui.receptionist.AppointmentEntityCard
import com.example.ivydentalapp.ui.receptionist.CalendarView
import com.example.ivydentalapp.IvyDentalApplication
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ManagerAppointmentsContent() {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("upcoming") } // upcoming, completed
    var appointments by remember { mutableStateOf<List<com.example.ivydentalapp.data.local.entity.AppointmentEntity>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Load all appointments
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            val allAppointments = app.appointmentRepository.getAllAppointments()
            withContext(Dispatchers.Main) {
                appointments = allAppointments
            }
        }
    }
    
    // Filter appointments
    val filteredAppointments = remember(appointments, filter, searchQuery, selectedDate) {
        var filtered = appointments
        
        // Filter by status
        filtered = when (filter) {
            "upcoming" -> filtered.filter { it.status !in listOf("Completed", "Cancelled", "No-Show") }
            "completed" -> filtered.filter { it.status == "Completed" }
            else -> filtered
        }
        
        // Filter by search query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.serviceType?.contains(searchQuery, ignoreCase = true) == true ||
                it.appointmentId.toString().contains(searchQuery, ignoreCase = true) ||
                it.patientId.toString().contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filter by selected date
        selectedDate?.let { date ->
            filtered = filtered.filter {
                it.scheduledDatetime.startsWith(date.toString())
            }
        }
        
        filtered
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ivydentalapp.ui.theme.BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Appointments",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ivydentalapp.ui.theme.TextDark
                )
                Text(
                    "View all patient appointments (Read Only)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search appointments...", color = com.example.ivydentalapp.ui.theme.TextGray.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                    focusedBorderColor = Primary,
                    cursorColor = Primary
                )
            )
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == "upcoming",
                    onClick = { filter = "upcoming" },
                    label = { Text("Upcoming", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = filter == "completed",
                    onClick = { filter = "completed" },
                    label = { Text("Completed", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Calendar View
        item {
            var currentMonth by remember { mutableStateOf(YearMonth.now()) }
            CalendarView(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it },
                appointments = appointments,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = if (selectedDate == date) null else date
                }
            )
        }
        
        // Show selected date info
        if (selectedDate != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Appointments on ${selectedDate!!.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { selectedDate = null }) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Appointments List Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Detailed View",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (filteredAppointments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${filter} appointments found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ivydentalapp.ui.theme.TextGray
                    )
                }
            }
        } else {
            items(filteredAppointments) { appointment ->
                AppointmentEntityCard(appointment, onClick = {
                    val intent = android.content.Intent(context, com.example.ivydentalapp.ui.appointment.AppointmentDetailsActivity::class.java).apply {
                        putExtra("APPOINTMENT_ID", appointment.appointmentId)
                    }
                    context.startActivity(intent)
                })
            }
        }
    }
}
