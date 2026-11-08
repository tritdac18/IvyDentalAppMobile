package com.example.ivydentalapp.ui.receptionist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionistPaymentsContent(
    viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as IvyDentalApplication).invoiceRepository,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as IvyDentalApplication).paymentRepository,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as IvyDentalApplication).appointmentRepository,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as IvyDentalApplication).notificationRepository,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as IvyDentalApplication).paymentSimulationService
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedInvoiceIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var amountReceived by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    
    // Discount States
    var selectedPatientForDiscount by remember { mutableStateOf("") } // In real map, this would be an ID
    var discountType by remember { mutableStateOf("PWD (20% Discount)") }
    var discountRefId by remember { mutableStateOf("") }
    
    var gcashReferenceNumber by remember { mutableStateOf("") }
    var gcashAccountName by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    val invoiceBalances = remember { mutableStateMapOf<Int, Double>() }

    val filteredInvoices = remember(uiState.invoices, searchQuery) {
        uiState.invoices.filter { invoice ->
            searchQuery.isBlank() ||
            invoice.invoiceId.toString().contains(searchQuery, ignoreCase = true) ||
            invoice.patientId.toString().contains(searchQuery, ignoreCase = true)
        }
    }

    val selectedInvoices = remember(selectedInvoiceIds, uiState.invoices) {
        uiState.invoices.filter { selectedInvoiceIds.contains(it.invoiceId) }
    }
    
    // For Discount Dialog: Populate Patient Dropdown
    val distinctPatients = remember(uiState.invoices) {
         uiState.invoices.map { "Patient #${it.patientId}" }.distinct()
    }
    
    val appointmentPrices = remember { mutableStateMapOf<Int, Double>() }
    
    // Load balances and appointment prices for breakdown
    LaunchedEffect(selectedInvoices) {
        selectedInvoices.forEach { invoice ->
            try {
                // Get downpayment
                val dp = app.paymentRepository.getTotalDownpaymentForInvoice(invoice.invoiceId)
                invoiceBalances[invoice.invoiceId] = (invoice.totalAmount ?: 0.0) - dp
                
                // Get base price from appointment
                invoice.appointmentId?.let { aptId ->
                    val apt = app.appointmentRepository.getAppointmentById(aptId)
                    appointmentPrices[invoice.invoiceId] = apt?.price ?: (invoice.totalAmount ?: 0.0)
                } ?: run {
                    appointmentPrices[invoice.invoiceId] = invoice.totalAmount ?: 0.0
                }
            } catch (e: Exception) {
                invoiceBalances[invoice.invoiceId] = invoice.totalAmount ?: 0.0
                appointmentPrices[invoice.invoiceId] = invoice.totalAmount ?: 0.0
            }
        }
    }
    
    val totalAmount = selectedInvoices.sumOf { invoiceBalances[it.invoiceId] ?: (it.totalAmount ?: 0.0) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            selectedInvoiceIds = emptySet()
            amountReceived = ""
            notes = ""
            gcashReferenceNumber = ""
            gcashAccountName = ""
            showConfirmDialog = false
            showDiscountDialog = false
            discountRefId = ""
            selectedPatientForDiscount = ""
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ivydentalapp.ui.theme.BackgroundLight)
            .padding(16.dp)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            color = Color.Transparent
        ) {
            Column {
                Text(
                    "Billing & Payments",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = com.example.ivydentalapp.ui.theme.TextDark
                )
                Text(
                    "Process and settle outstanding balances",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
            }
        }

        // Messages Area
        Column {
            uiState.successMessage?.let { message ->
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(message, color = Color(0xFF065F46), fontWeight = FontWeight.Medium)
                    }
                }
            }
            uiState.error?.let { error ->
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(error, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Validate Discount Button (Moved to top)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { showDiscountDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                 Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                 Spacer(modifier = Modifier.width(4.dp))
                 Text("Validate Discount ID", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Search & Select Floating Invoices (Dropdown)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "FIND UNPAID INVOICES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            expanded = true
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        placeholder = { Text("Search by Patient ID or Invoice #") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                    
                    val dropdownInvoices = filteredInvoices.filter { !selectedInvoiceIds.contains(it.invoiceId) }
                    
                    ExposedDropdownMenu(
                        expanded = expanded && dropdownInvoices.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        dropdownInvoices.take(10).forEach { invoice ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("INV-${invoice.invoiceId} - Patient ID: ${invoice.patientId}", fontWeight = FontWeight.Bold)
                                        Text("Amount: ₱${String.format("%.2f", invoice.totalAmount ?: 0.0)} | Status: ${invoice.paymentStatus}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedInvoiceIds = selectedInvoiceIds + invoice.invoiceId
                                    searchQuery = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 2. Selected Invoices List
            if (selectedInvoices.isNotEmpty()) {
                item {
                    Text(
                        "INVOICES TO PAY (${selectedInvoices.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(selectedInvoices) { invoice ->
                    InvoicePaymentItem(
                        invoice = invoice,
                        isSelected = true,
                        onToggleSelection = {
                            selectedInvoiceIds = selectedInvoiceIds - invoice.invoiceId
                        }
                    )
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, com.example.ivydentalapp.ui.theme.BorderColor.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No invoices selected", style = MaterialTheme.typography.titleMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Search and select items above to begin", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // 3. Final Settlement Summary
            if (selectedInvoiceIds.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TRANSACTION BREAKDOWN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Primary.copy(alpha=0.3f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Detailed breakdown for each selected invoice
                            selectedInvoices.forEach { invoice ->
                                val total = invoice.totalAmount ?: 0.0
                                val balance = invoiceBalances[invoice.invoiceId] ?: total
                                val downpayment = total - balance
                                val basePrice = appointmentPrices[invoice.invoiceId] ?: total
                                val additionalExpenses = (total - basePrice).coerceAtLeast(0.0)
                                
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("INV-${invoice.invoiceId}", fontWeight = FontWeight.Bold, color = Primary)
                                        Text("₱${String.format("%,.2f", total)}", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    // Base Service
                                    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("• Base Service Fee", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text("₱${String.format("%,.2f", basePrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    
                                    // Additional Expenses
                                    if (additionalExpenses > 0.01) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("• Additional Charges", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Text("₱${String.format("%,.2f", additionalExpenses)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    
                                    // Downpayment Deduction
                                    if (downpayment > 0.01) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
                                            color = Color(0xFFD1FAE5).copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("LESS: PAID (DOWNPAYMENT)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                                Text("-₱${String.format("%,.2f", downpayment)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                            }
                                        }
                                    }
                                }
                                if (invoice != selectedInvoices.last()) Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha=0.3f))
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 2.dp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL REMAINING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
                                Text(
                                    "₱${String.format("%,.2f", totalAmount)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }

            // 4. Payment Method Card
            if (selectedInvoiceIds.isNotEmpty()) {
                item {
                    Text(
                        "SETTLEMENT METHOD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = Primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PaymentMethodCard(
                                    method = "Cash",
                                    isSelected = paymentMethod == "Cash",
                                    onClick = { paymentMethod = "Cash" },
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentMethodCard(
                                    method = "GCash",
                                    isSelected = paymentMethod == "GCash",
                                    onClick = { paymentMethod = "GCash" },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (paymentMethod == "Cash") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = com.example.ivydentalapp.ui.theme.BackgroundLight,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = amountReceived,
                                            onValueChange = { amountReceived = it },
                                            label = { Text("Amount Cash") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            prefix = { Text("₱") }
                                        )
                                        OutlinedTextField(
                                            value = if (amountReceived.isNotBlank() && totalAmount > 0.0) {
                                                val received = amountReceived.toDoubleOrNull() ?: 0.0
                                                String.format("%.2f", (received - totalAmount).coerceAtLeast(0.0))
                                            } else "0.00",
                                            onValueChange = {},
                                            label = { Text("Change") },
                                            enabled = false,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            prefix = { 
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("₱")
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Image(
                                            painter = painterResource(id = com.example.ivydentalapp.R.drawable.gcash_qr_ivy_dental),
                                            contentDescription = "GCash QR",
                                            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OutlinedTextField(
                                            value = gcashReferenceNumber,
                                            onValueChange = { gcashReferenceNumber = it },
                                            label = { Text("Reference Number") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = gcashAccountName,
                                            onValueChange = { gcashAccountName = it },
                                            label = { Text("Account Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Transaction Notes (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // 5. Final Confirmation Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading && (
                            (paymentMethod == "GCash" && gcashReferenceNumber.isNotBlank() && gcashAccountName.isNotBlank()) ||
                            (paymentMethod == "Cash" && amountReceived.isNotBlank() && 
                             (amountReceived.toDoubleOrNull() ?: 0.0) >= totalAmount)
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("COMPLETE SETTLEMENT", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Confirm Payment Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Process payment for ${selectedInvoiceIds.size} invoice(s)?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Total: ₱${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = if (paymentMethod == "Cash") {
                            amountReceived.toDoubleOrNull()
                        } else null
                        viewModel.processPayment(
                            invoiceIds = selectedInvoiceIds.toList(),
                            paymentMethod = paymentMethod,
                            amountReceived = amount,
                            notes = notes.takeIf { it.isNotBlank() },
                            gcashReferenceNumber = if (paymentMethod == "GCash") gcashReferenceNumber else null,
                            gcashAccountName = if (paymentMethod == "GCash") gcashAccountName else null
                        )
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Validate Discount Dialog (Updated to match design)
    if (showDiscountDialog) {
         AlertDialog(
             onDismissRequest = { showDiscountDialog = false },
             title = { 
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                     Text("Validate Discount ID", fontWeight = FontWeight.Bold)
                     IconButton(onClick = { showDiscountDialog = false }) {
                         Icon(Icons.Default.Close, contentDescription = "Close")
                     }
                 }
             },
             text = {
                 Column(modifier = Modifier.fillMaxWidth()) {
                     
                     // Patient Select (Dropdown)
                     Text("Select Patient *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                     Spacer(modifier = Modifier.height(4.dp))
                     
                     var expanded by remember { mutableStateOf(false) }
                     
                     ExposedDropdownMenuBox(
                         expanded = expanded,
                         onExpandedChange = { expanded = !expanded }
                     ) {
                         OutlinedTextField(
                             value = if(selectedPatientForDiscount.isEmpty()) "Select a patient..." else selectedPatientForDiscount,
                             onValueChange = {},
                             readOnly = true,
                             modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                             trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                             shape = RoundedCornerShape(8.dp),
                             colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                         )
                         ExposedDropdownMenu(
                             expanded = expanded,
                             onDismissRequest = { expanded = false }
                         ) {
                             if (distinctPatients.isEmpty()) {
                                 DropdownMenuItem(text={Text("No patients with invoices")}, onClick={})
                             } else {
                                 distinctPatients.forEach { patientName ->
                                     DropdownMenuItem(
                                         text={Text(patientName)}, 
                                         onClick={
                                             selectedPatientForDiscount = patientName
                                             expanded = false
                                         }
                                     )
                                 }
                             }
                         }
                     }
                     
                     if (selectedPatientForDiscount.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                             Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                                 Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text("Verify the ID for ${selectedPatientForDiscount}.", fontSize = 10.sp, color = Color(0xFF1E40AF), lineHeight = 14.sp)
                             }
                        }
                     }
                     
                     Spacer(modifier = Modifier.height(16.dp))

                     // Discount Type
                     Text("Discount Type *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                     Spacer(modifier = Modifier.height(4.dp))
                     Box {
                        var expandedType by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = discountType,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { expandedType = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, Modifier.clickable { expandedType = true }) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                             DropdownMenuItem(text={Text("Senior Citizen (20% Discount)")}, onClick={discountType="Senior Citizen (20% Discount)"; expandedType=false})
                             DropdownMenuItem(text={Text("PWD (20% Discount)")}, onClick={discountType="PWD (20% Discount)"; expandedType=false})
                        }
                     }
                     
                     Spacer(modifier = Modifier.height(16.dp))

                     // ID Number
                     Text("${if (discountType.contains("PWD")) "PWD" else "Senior Citizen"} ID Number *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                     Spacer(modifier = Modifier.height(4.dp))
                     OutlinedTextField(
                         value = discountRefId,
                         onValueChange = { discountRefId = it },
                         placeholder = { Text("0000000000") },
                         modifier = Modifier.fillMaxWidth(),
                         shape = RoundedCornerShape(8.dp)
                     )
                 }
             },
             confirmButton = {
                 Button(
                     onClick = { 
                         viewModel.applyDiscount(selectedInvoiceIds.toList(), discountType, discountRefId)
                         showDiscountDialog = false 
                     },
                     enabled = discountRefId.isNotBlank(),
                     colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                     modifier = Modifier.fillMaxWidth().height(48.dp),
                     shape = RoundedCornerShape(8.dp)
                 ) {
                     Text("Save Discount ID", fontWeight = FontWeight.Bold)
                 }
             },
             dismissButton = {
                 OutlinedButton(
                     onClick = { showDiscountDialog = false },
                     modifier = Modifier.fillMaxWidth().height(48.dp),
                     shape = RoundedCornerShape(8.dp),
                     border = BorderStroke(1.dp, Color.LightGray)
                 ) {
                     Text("Cancel", color = Color.Gray)
                 }
             },
             containerColor = Color.White,
             shape = RoundedCornerShape(16.dp)
         )
    }
}

@Composable
fun InvoicePaymentItem(
    invoice: InvoiceEntity,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f)),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "INV-${invoice.invoiceId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Text(
                        "Pat ID: ${invoice.patientId} | Apt ID: ${invoice.appointmentId ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₱${String.format("%,.2f", invoice.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                Text("Remove", color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else com.example.ivydentalapp.ui.theme.BorderColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                if (method == "Cash") Icons.Default.Payments else Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = if (isSelected) Primary else com.example.ivydentalapp.ui.theme.TextGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                method,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Primary else com.example.ivydentalapp.ui.theme.TextDark
            )
        }
    }
}
