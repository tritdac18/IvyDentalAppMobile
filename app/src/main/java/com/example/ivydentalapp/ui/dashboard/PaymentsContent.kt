package com.example.ivydentalapp.ui.dashboard

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsContent(patientId: Int) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    // Initialize Dashboard ViewModel
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(
            app.invoiceRepository,
            app.paymentRepository,
            app.appointmentRepository,
            app.paymentSimulationService,
            app.notificationRepository
        )
    )

    // Collect State
    val invoices by app.invoiceRepository.getInvoices(patientId).collectAsState(initial = emptyList())
    // We treat all "unpaid/partial" as the list to show
    val unpaidInvoices = remember(invoices) {
        invoices.filter { it.paymentStatus in listOf("Unpaid", "Partial") }
    }
    
    // manage selection locally or via VM? Dashboard VM uses vm.selectedInvoiceIds
    // But since we are copying Receptionist UI which uses local state, I'll use local state for now 
    // to match the structure, but keep VM in sync if needed. 
    // Actually, Dashboard VM `processPayment` takes `invoices` list directly. 
    // So local state is fine.
    var selectedInvoiceIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // GCash Details
    var gcashReferenceNumber by remember { mutableStateOf("") }
    var gcashAccountName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") } // Not supported by Dashboard VM processPayment yet?
    // Dashboard VM processPayment signature: (patientId, invoices) -> it reads gcash details from ITS state.
    // So we must update VM state.
    
    LaunchedEffect(gcashAccountName, gcashReferenceNumber) {
        viewModel.updateGcashDetails(gcashAccountName, gcashReferenceNumber)
    }

    // Success/Error Handling from VM
    val showSuccessDialog by viewModel.showSuccessDialog.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    
    // Calculate Breakdown (Copied Logic)
    val invoiceBalances = remember { mutableStateMapOf<Int, Double>() }
    val appointmentPrices = remember { mutableStateMapOf<Int, Double>() }
    
    val selectedInvoices = remember(selectedInvoiceIds, unpaidInvoices) {
        unpaidInvoices.filter { selectedInvoiceIds.contains(it.invoiceId) }
    }

    val totalAmount = selectedInvoices.sumOf { invoiceBalances[it.invoiceId] ?: (it.totalAmount ?: 0.0) }

    LaunchedEffect(selectedInvoices) {
        selectedInvoices.forEach { invoice ->
            try {
                val dp = app.paymentRepository.getTotalDownpaymentForInvoice(invoice.invoiceId)
                invoiceBalances[invoice.invoiceId] = (invoice.totalAmount ?: 0.0) - dp
                
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ivydentalapp.ui.theme.BackgroundLight)
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                "My Payments",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark
            )
            Text(
                "Manage your invoices and payments",
                style = MaterialTheme.typography.bodyMedium,
                color = com.example.ivydentalapp.ui.theme.TextGray
            )
        }
        
        // Important Note (Requested)
        Surface(
            color = Color(0xFFFFF7ED),
            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Important Notice", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412), style = MaterialTheme.typography.labelMedium)
                    Text(
                        "Cash transactions are processed at the counter. Discount validations (Senior/PWD) are also processed at the counter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A3412)
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 2. Unpaid Invoices List
            item {
                Text(
                    "PENDING INVOICES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (unpaidInvoices.isEmpty()) {
                item {
                    Text("No pending invoices.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                items(unpaidInvoices) { invoice ->
                    val isSelected = selectedInvoiceIds.contains(invoice.invoiceId)
                    InvoicePaymentItem(
                        invoice = invoice,
                        isSelected = isSelected,
                        onToggleSelection = {
                            if (isSelected) selectedInvoiceIds = selectedInvoiceIds - invoice.invoiceId
                            else selectedInvoiceIds = selectedInvoiceIds + invoice.invoiceId
                        }
                    )
                }
            }

            // 3. Breakdown (Only if selected)
            if (selectedInvoiceIds.isNotEmpty()) {
                item {
                    Text(
                        "TRANSACTION BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Primary.copy(alpha=0.3f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
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
                                                Text("LESS: PAID", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
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
                                Text("TOTAL PAYABLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
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

                // 4. Payment Method (GCash Only)
                item {
                    Text(
                        "PAYMENT METHOD (GCash)",
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
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    }
                }
                
                // 5. Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            // Call Dashboard VM
                            viewModel.processPayment(patientId, selectedInvoices)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isProcessing && gcashReferenceNumber.isNotBlank() && gcashAccountName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                         if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("PAY ₱${String.format("%,.2f", totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
    
    // Processing / Success Dialogs
    if (showSuccessDialog) {
         AlertDialog(
            onDismissRequest = { viewModel.setSuccessDialogVisible(false) },
            title = { Text("Payment Successful") },
            text = { Text("Your payment has been processed and submitted for review.") },
            confirmButton = {
                TextButton(onClick = { viewModel.setSuccessDialogVisible(false) }) {
                    Text("OK")
                }
            }
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
        border = if (isSelected) BorderStroke(2.dp, Primary) else BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)),
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "INV-${invoice.invoiceId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Date: ${invoice.generatedAt ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₱${String.format("%,.2f", invoice.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                 if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
