package com.example.ivydentalapp.ui.receptionist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.data.local.entity.ServiceEntity
import com.example.ivydentalapp.ui.theme.Primary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ReceptionistAppointmentsContent() {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("upcoming") } // upcoming, completed
    var appointments by remember { mutableStateOf<List<com.example.ivydentalapp.data.local.entity.AppointmentEntity>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showWalkInPage by remember { mutableStateOf(false) }
    
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
        
        // Filter by status with flexible groups
        val statusGroups = mapOf(
            "upcoming" to listOf("Scheduled", "Pending", "Confirmed"),
            "completed" to listOf("Completed", "Cancelled", "No-Show", "Missed")
        )

        filtered = when (filter) {
            "upcoming" -> filtered.filter { it.status in (statusGroups["upcoming"] ?: emptyList()) }
                .sortedBy { it.scheduledDatetime } // Ascending for upcoming
            "completed" -> filtered.filter { it.status in (statusGroups["completed"] ?: emptyList()) }
                .sortedByDescending { it.scheduledDatetime } // Descending for history
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ivydentalapp.ui.theme.BackgroundLight)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... (Header items unchanged) ...
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
                        "Manage and view all patient appointments",
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
                        label = { Text("History", style = MaterialTheme.typography.labelSmall) }, // Changed "Completed" to "History" to include missed
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
                    appointments = appointments, // Show ALL dots on calendar, regardless of list filter
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
                            "No appointments found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = com.example.ivydentalapp.ui.theme.TextGray
                        )
                    }
                }
            }

            items(filteredAppointments) { appointment ->
                AppointmentEntityCard(appointment, onClick = {
                    val intent = android.content.Intent(context, com.example.ivydentalapp.ui.appointment.AppointmentDetailsActivity::class.java).apply {
                        putExtra("APPOINTMENT_ID", appointment.appointmentId)
                    }
                    context.startActivity(intent)
                })
            }

            // Space for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB for New Walk-in Appointment
        FloatingActionButton(
            onClick = { showWalkInPage = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Walk-in Appointment", tint = Color.White)
        }

        // Full-screen Walk-in Booking Page (similar to patient booking)
        if (showWalkInPage) {
            ReceptionistWalkInBookingPage(
                onBack = { showWalkInPage = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionistWalkInBookingPage(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    // ViewModel Factory
    val viewModel: ReceptionistAppointmentViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ReceptionistAppointmentViewModel(
                    app,
                    app.appointmentRepository,
                    app.notificationRepository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // State for DB-loaded data
    var allPatients by remember { mutableStateOf<List<PatientEntity>>(emptyList()) }
    var allServices by remember { mutableStateOf<List<ServiceEntity>>(emptyList()) }
    var allDentists by remember { mutableStateOf<List<ReceptionistEntity>>(emptyList()) }

    // Load patients, services, and dentists from database
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val patients = app.database.patientDao().getAllPatients()
            val services = app.serviceRepository.getActiveServices()
            // Use staff table and filter dentists by role
            val staff = app.database.receptionistDao().getAllStaff()
            val dentists = staff.filter { it.role.equals("Dentist", ignoreCase = true) }

            withContext(Dispatchers.Main) {
                allPatients = patients
                allServices = services
                allDentists = dentists
            }
        }
    }

    // Handle Success
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
            onBack()
        }
    }

    // Handle Error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Existing, 1: New
    val tabs = listOf("Existing Patient", "New Patient")

    // Existing patient search/selection
    var patientSearchQuery by remember { mutableStateOf("") }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }
    val filteredPatients = remember(patientSearchQuery, allPatients) {
        if (patientSearchQuery.isBlank()) emptyList()
        else allPatients.filter {
            "${it.firstName} ${it.lastName}".contains(patientSearchQuery, ignoreCase = true) ||
                it.patientId.toString().contains(patientSearchQuery)
        }
    }

    // Service selection
    var selectedService by remember { mutableStateOf<ServiceEntity?>(null) }
    var serviceExpanded by remember { mutableStateOf(false) }

    // Dentist selection
    var selectedDentist by remember { mutableStateOf<ReceptionistEntity?>(null) }
    var dentistExpanded by remember { mutableStateOf(false) }

            // Simple text-based date/time for now (must match pattern: yyyy-MM-dd and hh:mm a)
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Walk-in Appointment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs for Existing/New patient
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == 0) {
                // Existing Patient Search
                Column {
                    OutlinedTextField(
                        value = patientSearchQuery,
                        onValueChange = { query ->
                            patientSearchQuery = query
                            showPatientDropdown = query.isNotBlank()
                        },
                        label = { Text("Search Patient (Name/ID)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showPatientDropdown && filteredPatients.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column {
                                filteredPatients.take(5).forEach { patient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedPatient = patient
                                                patientSearchQuery = "${patient.firstName} ${patient.lastName} (ID: ${patient.patientId})"
                                                showPatientDropdown = false
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${patient.firstName} ${patient.lastName} (ID: ${patient.patientId})",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (patient != filteredPatients.last()) {
                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    selectedPatient?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selected: ${it.firstName} ${it.lastName} (ID: ${it.patientId})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
            } else {
                // New Patient Form (Placeholder for now, logic to create patient first would be needed)
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("New Patient Registration not yet implemented on this screen. Please register patient first.", color = Color.Red)
                }
            }

            Divider()
            Text("Appointment Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            // Service
            ExposedDropdownMenuBox(
                expanded = serviceExpanded,
                onExpandedChange = { serviceExpanded = !serviceExpanded }
            ) {
                OutlinedTextField(
                    value = selectedService?.serviceName ?: "Select Service *",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = serviceExpanded,
                    onDismissRequest = { serviceExpanded = false }
                ) {
                    allServices.forEach { service ->
                        DropdownMenuItem(
                            text = { Text("${service.serviceName} (₱${service.basePrice})") },
                            onClick = {
                                selectedService = service
                                serviceExpanded = false
                            }
                        )
                    }
                }
            }

            // Dentist
            ExposedDropdownMenuBox(
                expanded = dentistExpanded,
                onExpandedChange = { dentistExpanded = !dentistExpanded }
            ) {
                OutlinedTextField(
                    value = selectedDentist?.let { "${it.firstName} ${it.lastName}" } ?: "Select Dentist (Optional)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dentist (Optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dentistExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dentistExpanded,
                    onDismissRequest = { dentistExpanded = false }
                ) {
                    allDentists.forEach { dentist ->
                        DropdownMenuItem(
                            text = { Text("${dentist.firstName} ${dentist.lastName}") },
                            onClick = {
                                selectedDentist = dentist
                                dentistExpanded = false
                            }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date * (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Time * (e.g. 02:00 PM)") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = true))

            Button(
                onClick = {
                    if (selectedPatient != null && selectedService != null && dateText.isNotBlank() && timeText.isNotBlank()) {
                        // Basic validation
                        try {
                            // Construct datetime
                            val datetime = "$dateText $timeText"
                            // Can add more validation here
                            
                            viewModel.bookWalkInAppointment(
                                patientId = selectedPatient!!.patientId,
                                dentistId = selectedDentist?.staffId ?: 0, // Assuming 0 if not selected
                                serviceType = selectedService!!.serviceName,
                                price = selectedService!!.basePrice,
                                scheduledDateTime = datetime,
                                durationMinutes = selectedService!!.durationMinutes
                            )
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Invalid input", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Please fill all required fields", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Book Appointment", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}



@Composable
fun CalendarView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    appointments: List<com.example.ivydentalapp.data.local.entity.AppointmentEntity>,
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit = {}
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
                                val isSelected = selectedDate == date
                                val isToday = date == LocalDate.now()
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Primary
                                                hasAppointment -> Primary.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { onDateSelected(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayIndex.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) Color.White else if (isToday) Primary else Color.Black,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
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
