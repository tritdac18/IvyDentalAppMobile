package com.example.ivydentalapp.ui.receptionist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionistInvoicesContent() {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    val viewModel: InvoiceViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return InvoiceViewModel(
                    app,
                    app.invoiceRepository,
                    app.appointmentRepository,
                    app.database.patientDao(),
                    app.serviceRepository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateInvoicePage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.observeInvoices()
        viewModel.loadPatients()
        viewModel.loadAppointmentsForInvoice()
    }


    // Close create-invoice page on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            showCreateInvoicePage = false
        }
    }

    // Show success/error messages
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearMessages()
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Invoice Management",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Text(
                        "Manage billing and track payments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ivydentalapp.ui.theme.TextGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Success/Error Messages
        item {
            uiState.successMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF065F46)
                    )
                }
            }
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF991B1B)
                    )
                }
            }
        }

        // Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InvoiceStatCard(
                    title = "Outstanding Balance",
                    value = "₱${String.format("%.2f", uiState.outstandingBalance)}",
                    icon = Icons.Default.PendingActions,
                    iconColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f).height(110.dp)
                )
                InvoiceStatCard(
                    title = "Daily Revenue",
                    value = "₱${String.format("%.2f", uiState.dailyRevenue)}",
                    icon = Icons.Default.Payments,
                    iconColor = Primary,
                    modifier = Modifier.weight(1f).height(110.dp)
                )
                InvoiceStatCard(
                    title = "Invoices Generated",
                    value = "${uiState.invoicesGenerated}",
                    icon = Icons.Default.ReceiptLong,
                    iconColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f).height(110.dp)
                )
            }
        }

        // Create Invoice Section (below reports)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Create Invoice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Generate an invoice from an existing appointment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ivydentalapp.ui.theme.TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { 
                            showCreateInvoicePage = true
                            // Preload appointments without invoices
                            viewModel.loadAppointmentsForInvoice()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Invoice")
                    }
                }
            }
        }

        // Invoice List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Invoices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ivydentalapp.ui.theme.TextDark
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search ID/Patient...") },
                            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = Primary) },
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                                focusedBorderColor = Primary,
                                cursorColor = Primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Status Filters
                    var statusFilter by remember { mutableStateOf("All") }
                    val statuses = listOf("All", "Paid", "Unpaid", "Partial")
                    
                    ScrollableTabRow(
                        selectedTabIndex = statuses.indexOf(statusFilter),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        contentColor = Primary,
                        divider = {},
                        indicator = {}
                    ) {
                        statuses.forEach { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { statusFilter = status },
                                label = { Text(status) },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary.copy(alpha=0.1f),
                                    selectedLabelColor = Primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.invoices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No invoices found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = com.example.ivydentalapp.ui.theme.TextGray
                            )
                        }
                    } else {
                        val filteredInvoices = uiState.invoices.filter { invoice ->
                            val patient = uiState.patients.find { it.patientId == invoice.patientId }
                            val patientName = if (patient != null) "${patient.firstName} ${patient.lastName}" else ""

                            val matchSearch = searchQuery.isBlank() || 
                                              invoice.invoiceId.toString().contains(searchQuery, ignoreCase = true) ||
                                              invoice.patientId.toString().contains(searchQuery, ignoreCase = true) ||
                                              patientName.contains(searchQuery, ignoreCase = true)
                            val matchStatus = statusFilter == "All" || 
                                              (invoice.paymentStatus?.equals(statusFilter, ignoreCase = true) == true)
                            
                            matchSearch && matchStatus
                        }
                        
                        if (filteredInvoices.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No invoices match your filter.", color = Color.Gray)
                            }
                        }
                        
                        filteredInvoices.forEach { invoice ->
                            var showDetails by remember { mutableStateOf(false) }
                            Column {
                                InvoiceItem(
                                    invoice = invoice,
                                    onClick = { showDetails = !showDetails }
                                )
                            if (showDetails) {
                                InvoiceDetailsDialog(
                                    invoice = invoice,
                                    onDismiss = { showDetails = false }
                                )
                            }
                        }
                        if (invoice != filteredInvoices.last()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
    }

    // Full-page Create Invoice screen (instead of popup)
    if (showCreateInvoicePage) {
        ReceptionistCreateInvoicePage(
            patients = uiState.patients,
            appointments = uiState.appointments,
            onBack = { 
                showCreateInvoicePage = false
                viewModel.clearMessages()
            },
            onCreateInvoice = { patientId, appointmentId, totalAmount, additional ->
                viewModel.createInvoiceFromAppointment(
                    appointmentId = appointmentId,
                    patientId = patientId,
                    totalAmount = totalAmount,
                    additionalExpenses = additional
                )
            },
            onLoadAppointmentsForPatient = { patientId ->
                viewModel.loadAppointmentsForPatient(patientId)
            }
        )
    }

}

@Composable
fun InvoiceDetailsDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    var appointment by remember { mutableStateOf<AppointmentEntity?>(null) }
    var payments by remember { mutableStateOf<List<com.example.ivydentalapp.data.local.entity.PaymentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(invoice) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (invoice.appointmentId != null) {
                    appointment = app.appointmentRepository.getAppointmentById(invoice.appointmentId)
                }
                payments = app.paymentRepository.getPaymentsForInvoices(listOf(invoice.invoiceId))
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    val basePrice = appointment?.price ?: 0.0
    val totalAmount = invoice.totalAmount ?: 0.0
    // Calculate difference to determine if it's an additional expense or a discount
    val priceDifference = totalAmount - basePrice
    val additional = if (priceDifference > 0) priceDifference else 0.0
    val discount = if (priceDifference < 0) -priceDifference else 0.0
    
    val downpayment = payments.filter { it.paymentType == "Downpayment" || (it.notes?.contains("Downpayment", true) == true) }.sumOf { it.amountPaid }
    val settlements = payments.filter { it.paymentType != "Downpayment" && (it.notes?.contains("Downpayment", true) != true) }
    val totalSettled = settlements.sumOf { it.amountPaid }
    val totalPaid = downpayment + totalSettled
    val remaining = (totalAmount - totalPaid).coerceAtLeast(0.0)
    val change = (totalPaid - totalAmount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Invoice Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Basic Info
                    Text("INVOICE ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("INV-${invoice.invoiceId}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("DATE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(formatDate(invoice.generatedAt), style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("SERVICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(appointment?.serviceType ?: "General Service", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Breakdown
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Transaction Breakdown", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(bottom=8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Service Price", style = MaterialTheme.typography.bodyMedium)
                                Text("₱${String.format("%,.2f", basePrice)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            
                            if (additional > 0.01) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Additional Expenses", style = MaterialTheme.typography.bodyMedium)
                                    Text("₱${String.format("%,.2f", additional)}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            if (discount > 0.01) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF10B981))
                                    Text("-₱${String.format("%,.2f", discount)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF10B981))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL AMOUNT", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("₱${String.format("%,.2f", totalAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("PAYMENT STATUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(invoice.paymentStatus ?: "Unpaid")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Payment Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (payments.isEmpty()) {
                                Text("No payments recorded.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            } else {
                                // Downpayments
                                if (downpayment > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Downpayment (Paid)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF059669))
                                        Text("₱${String.format("%,.2f", downpayment)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                // Settlements
                                if (totalSettled > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Settlement Paid", style = MaterialTheme.typography.bodyMedium)
                                        Text("₱${String.format("%,.2f", totalSettled)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                Divider(color = Color.LightGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL PAID", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("₱${String.format("%,.2f", totalPaid)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                
                                if (remaining > 0.01) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("BALANCE DUE", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Red)
                                        Text("₱${String.format("%,.2f", remaining)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Red)
                                    }
                                }

                                if (change > 0.01) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("CHANGE / OVERPAYMENT", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        Text("₱${String.format("%,.2f", change)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                    }
                                }
                                
                                // Show latest payment info
                                val latestPayment = payments.maxByOrNull { it.paymentId }
                                if (latestPayment != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Latest Transaction: ${formatDate(latestPayment.createdAt)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("Method: ${latestPayment.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionistCreateInvoicePage(
    patients: List<PatientEntity>,
    appointments: List<AppointmentEntity>,
    onBack: () -> Unit,
    onCreateInvoice: (Int, Int, Double, Double) -> Unit,
    onLoadAppointmentsForPatient: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    
    var patientSearchQuery by remember { mutableStateOf("") }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var selectedPatientId by remember { mutableStateOf<Int?>(null) }
    var selectedAppointmentId by remember { mutableStateOf<Int?>(null) }
    var patientAppointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var additionalExpenseName by remember { mutableStateOf("") }
    var additionalExpensePrice by remember { mutableStateOf("") }
    var additionalExpenseExpanded by remember { mutableStateOf(false) }
    
    // Predefined additional expenses
    val additionalExpenseOptions = listOf(
        "Medication" to 500.0,
        "X-Ray Film" to 200.0,
        "Anesthesia" to 300.0,
        "Lab Test" to 800.0,
        "Other" to 0.0
    )
    
    // Load appointments when patient is selected (delegate to caller ViewModel if provided)
    LaunchedEffect(selectedPatientId) {
        selectedPatientId?.let { patientId ->
            onLoadAppointmentsForPatient?.invoke(patientId)
        }
    }

    // Keep local list of appointments for the selected patient based on the appointments passed in
    LaunchedEffect(selectedPatientId, appointments) {
        patientAppointments = if (selectedPatientId != null) {
            appointments.filter { it.patientId == selectedPatientId }
        } else {
            emptyList()
        }
    }

    val filteredPatients = remember(patientSearchQuery, patients) {
        if (patientSearchQuery.isBlank()) emptyList()
        else patients.filter {
            val fullName = "${it.firstName} ${it.lastName}"
            fullName.contains(patientSearchQuery, ignoreCase = true) ||
            it.patientId.toString().contains(patientSearchQuery, ignoreCase = true) ||
            it.email.contains(patientSearchQuery, ignoreCase = true) ||
            it.username.contains(patientSearchQuery, ignoreCase = true)
        }
    }

    val selectedAppointment = patientAppointments.find { it.appointmentId == selectedAppointmentId }
    val servicePrice = selectedAppointment?.price ?: 0.0
    val additional = additionalExpensePrice.toDoubleOrNull() ?: 0.0
    val totalAmount = servicePrice + additional

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patient Selection
            Column {
                Text("Patient *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = patientSearchQuery,
                        onValueChange = { 
                            patientSearchQuery = it
                            showPatientDropdown = it.isNotBlank()
                        },
                        placeholder = { Text("Type patient name...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    if (showPatientDropdown && filteredPatients.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column {
                                filteredPatients.take(5).forEach { patient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedPatientId = patient.patientId
                                                patientSearchQuery = "${patient.firstName} ${patient.lastName}"
                                                showPatientDropdown = false
                                            }
                                            .padding(16.dp),
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
                }
            }

            // Appointment Selection
            Column {
                Text("Appointment *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (selectedPatientId == null) {
                    OutlinedTextField(
                        value = "Select a patient first",
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) }
                    )
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedAppointment?.let { 
                                "APT-${it.appointmentId} - ${formatDate(it.scheduledDatetime)}"
                            } ?: "Select appointment...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            if (patientAppointments.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No appointments without invoices") },
                                    onClick = { expanded = false },
                                    enabled = false
                                )
                            } else {
                                patientAppointments.forEach { appointment ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text("APT-${appointment.appointmentId} - ${formatDate(appointment.scheduledDatetime)}")
                                        },
                                        onClick = {
                                            selectedAppointmentId = appointment.appointmentId
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Service Info
            selectedAppointment?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Service Price: ₱${String.format("%.2f", servicePrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Additional Expenses
            Column {
                Text("Additional Expenses (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = additionalExpenseExpanded,
                    onExpandedChange = { additionalExpenseExpanded = !additionalExpenseExpanded }
                ) {
                    OutlinedTextField(
                        value = if (additionalExpenseName.isNotEmpty()) "$additionalExpenseName - ₱${String.format("%.2f", additional)}" else "Select expense type...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = additionalExpenseExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                    ExposedDropdownMenu(
                        expanded = additionalExpenseExpanded,
                        onDismissRequest = { additionalExpenseExpanded = false }
                    ) {
                        additionalExpenseOptions.forEach { (name, price) ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold)
                                        if (price > 0) {
                                            Text("₱${String.format("%.2f", price)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        } else {
                                            Text("Enter custom amount", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                },
                                onClick = {
                                    additionalExpenseName = name
                                    if (price > 0) {
                                        additionalExpensePrice = price.toString()
                                    } else {
                                        additionalExpensePrice = ""
                                    }
                                    additionalExpenseExpanded = false
                                }
                            )
                        }
                    }
                }
                if (additionalExpenseName == "Other" || (additionalExpenseName.isNotEmpty() && additionalExpensePrice.isEmpty())) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = additionalExpensePrice,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() || char == '.' }) {
                                additionalExpensePrice = it
                            }
                        },
                        label = { Text("Amount (₱)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("₱", modifier = Modifier.padding(start = 16.dp)) }
                    )
                }
            }

            // Total Amount
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Amount:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "₱${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedPatientId != null && selectedAppointmentId != null) {
                        onCreateInvoice(selectedPatientId!!, selectedAppointmentId!!, totalAmount, additional)
                    }
                },
                enabled = selectedPatientId != null && selectedAppointmentId != null && totalAmount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Generate Invoice", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun InvoiceStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark
            )
        }
    }
}

@Composable
fun InvoiceItem(invoice: InvoiceEntity, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "INV-${invoice.invoiceId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Text(
                "Patient ID: ${invoice.patientId} | Appointment ID: ${invoice.appointmentId}",
                style = MaterialTheme.typography.bodySmall,
                color = com.example.ivydentalapp.ui.theme.TextGray
            )
            Text(
                "Generated: ${formatDate(invoice.generatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = com.example.ivydentalapp.ui.theme.TextGray
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "₱${String.format("%.2f", invoice.totalAmount ?: 0.0)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark
            )
            StatusBadge(status = invoice.paymentStatus ?: "Unpaid")
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, bgColor) = when (status) {
        "Paid" -> Color(0xFF10B981) to Color(0xFFD1FAE5)
        "Unpaid" -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)
        "Partial" -> Color(0xFF3B82F6) to Color(0xFFDBEAFE)
        else -> Color(0xFF6B7280) to Color(0xFFF3F4F6)
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// Utility function for formatting date
fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "N/A"
    return try {
        // Try parsing ISO first
        val parsed = java.time.LocalDateTime.parse(dateStr)
        parsed.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        try {
            // Fallback for just date
             val parsedDate = java.time.LocalDate.parse(dateStr)
             parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e2: Exception) {
            dateStr // Return raw if parsing fails
        }
    }
}


