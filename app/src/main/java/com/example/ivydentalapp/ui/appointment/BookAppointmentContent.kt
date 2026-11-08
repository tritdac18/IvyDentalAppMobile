package com.example.ivydentalapp.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentContent(
    viewModel: AppointmentViewModel,
    services: List<Service>,
    doctors: List<Doctor>,
    onConfirm: (Service, Doctor, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    
    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    // Time Slots for a dentist's working hours
    val timeSlots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM")

    // Day appointments for currently selected dentist/date
    val dayAppointments by viewModel.dayAppointments.collectAsState()

    val amPmFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.US)
    }
    val legacyFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)
    }

    fun parseExistingDateTime(raw: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(raw, amPmFormatter)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(raw, legacyFormatter)
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun isTimeSlotAvailable(timeLabel: String): Boolean {
        val service = selectedService ?: return false
        val doctor = selectedDoctor ?: return false
        if (selectedDate.isEmpty()) return false

        return try {
            val now = LocalDateTime.now()
            val slotStart = LocalDateTime.parse("$selectedDate $timeLabel", amPmFormatter)
            val slotEnd = slotStart.plusMinutes(service.durationMinutes.toLong())

            // Do not allow booking for past times (including earlier times today)
            if (!slotStart.isAfter(now)) {
                return false
            }

            // Check overlap with existing appointments for this dentist/day
            dayAppointments.any() // just to read state; actual filtering below

            for (appt in dayAppointments) {
                if (appt.dentistId != doctor.id) continue
                val existingStart = parseExistingDateTime(appt.scheduledDatetime) ?: continue
                val existingEnd = existingStart.plusMinutes(appt.durationMinutes.toLong())

                // Overlap if start < existingEnd && end > existingStart
                if (slotStart.isBefore(existingEnd) && slotEnd.isAfter(existingStart)) {
                    return false
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    // Update date string when date picked
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            selectedDate = sdf.format(Date(it))

            // When date changes, reload day appointments if dentist selected
            selectedDoctor?.let { doctor ->
                viewModel.loadDoctorDayAppointments(doctor.id, selectedDate)
            } ?: viewModel.clearDoctorDayAppointments()

            // Clear previously selected time when date changes
            selectedTime = ""
        }
    }

    // When doctor or service changes, refresh availability and reset time selection
    LaunchedEffect(selectedDoctor, selectedService, selectedDate) {
        if (selectedDoctor != null && selectedDate.isNotEmpty()) {
            viewModel.loadDoctorDayAppointments(selectedDoctor!!.id, selectedDate)
        } else {
            viewModel.clearDoctorDayAppointments()
        }
        selectedTime = ""
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Appointment",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Service Selection
                Text("Select Service", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                var serviceExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { serviceExpanded = !serviceExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedService?.name ?: "Choose a service",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false }
                    ) {
                        services.forEach { service ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(service.name, fontWeight = FontWeight.Bold)
                                        Text("₱${service.price} • ${service.durationMinutes} mins", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedService = service
                                    serviceExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Doctor Selection
                Text("Select Dentist", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                var doctorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = doctorExpanded,
                    onExpandedChange = { doctorExpanded = !doctorExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDoctor?.name ?: "Choose a dentist",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = doctorExpanded,
                        onDismissRequest = { doctorExpanded = false }
                    ) {
                        doctors.forEach { doctor ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(doctor.name, fontWeight = FontWeight.Bold)
                                        Text(doctor.specialization, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedDoctor = doctor
                                    doctorExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date
                Text("Date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDate.ifEmpty { "Select Date" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false, 
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time
                Text("Time", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                var timeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = timeExpanded,
                    onExpandedChange = { timeExpanded = !timeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTime.ifEmpty { "Select Time" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = timeExpanded,
                        onDismissRequest = { timeExpanded = false }
                    ) {
                        timeSlots.forEach { time ->
                            val slotAvailable = isTimeSlotAvailable(time)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        time,
                                        color = if (slotAvailable) Color.Unspecified else Color.Gray
                                    )
                                },
                                onClick = {
                                    if (slotAvailable) {
                                        selectedTime = time
                                        timeExpanded = false
                                    }
                                },
                                enabled = slotAvailable
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                if (selectedService != null && selectedDoctor != null && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                    onConfirm(selectedService!!, selectedDoctor!!, selectedDate, selectedTime)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = selectedService != null && selectedDoctor != null && selectedDate.isNotEmpty() && selectedTime.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ivydentalapp.ui.theme.Primary)
        ) {
            Text("Proceed to Downpayment", fontWeight = FontWeight.Bold)
        }
    }
}
