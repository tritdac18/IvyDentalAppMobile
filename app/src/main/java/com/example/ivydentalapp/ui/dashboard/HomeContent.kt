package com.example.ivydentalapp.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivydentalapp.ui.theme.Primary

@Composable
fun HomeContent(
    currentUser: com.example.ivydentalapp.data.local.entity.PatientEntity?,
    onNavigateToTab: (DashboardTab) -> Unit,
    appointments: List<com.example.ivydentalapp.data.local.entity.AppointmentEntity>,
    invoices: List<com.example.ivydentalapp.data.local.entity.InvoiceEntity>
) {
    val firstName = currentUser?.firstName ?: "Guest"
    var selectedInvoice by remember { mutableStateOf<com.example.ivydentalapp.data.local.entity.InvoiceEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welocme Message
        Text(
            text = "Welcome back, $firstName \uD83D\uDC4B",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage your profile, view appointments, and access your dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = { onNavigateToTab(DashboardTab.Profile) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("My Profile")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onNavigateToTab(DashboardTab.Appointments) }, // Ideally opens booking
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Appointment")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Appointments Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigateToTab(DashboardTab.Appointments) }) {
                        Text("View All", color = Primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info Box
                Surface(
                    color = Color(0xFFFFF7ED), // Amber-50
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Minor Patients must bring their registered guardians during the appointment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real Appointment Items (Top 3)
                if (appointments.isEmpty()) {
                    Text(
                        "No upcoming appointments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    appointments.take(3).forEach { appointment ->
                        AppointmentItem(appointment)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Transactions Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Info Box
                Surface(
                    color = Color(0xFFEFF6FF), // Blue-50
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please head to the counter to process your invoices.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Real Transaction Items (Top 3)
                if (invoices.isEmpty()) {
                    Text(
                        "No recent transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    invoices.take(3).forEach { invoice ->
                        Box(modifier = Modifier.clickable { selectedInvoice = invoice }) {
                            TransactionItem(invoice)
                        }
                    }
                }
            }
        }
    }
    
    // Invoice Detail Dialog
    if (selectedInvoice != null) {
        InvoiceDetailDialog(
            invoice = selectedInvoice!!,
            onDismiss = { selectedInvoice = null }
        )
    }
}

@Composable
fun AppointmentItem(appointment: com.example.ivydentalapp.data.local.entity.AppointmentEntity) {
    val dateTimeParts = appointment.scheduledDatetime.split(" ")
    val date = if(dateTimeParts.isNotEmpty()) dateTimeParts[0] else ""
    val time = if(dateTimeParts.size > 1) dateTimeParts[1] else ""
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(date, fontWeight = FontWeight.Medium)
            Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Column(modifier = Modifier.weight(1.5f)) {
            Text(appointment.serviceType ?: "General", fontWeight = FontWeight.Medium)
            Text("₱${appointment.price}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        
        Box(
            modifier = Modifier
                .background(
                    when(appointment.status) {
                         "Scheduled" -> Color(0xFFDBEAFE)
                         "Pending" -> Color(0xFFFEF3C7) // Amber-100
                         else -> Color.LightGray.copy(alpha=0.3f)
                    }, 
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                appointment.status, 
                color = when(appointment.status) {
                     "Scheduled" -> Color(0xFF1E40AF)
                     "Pending" -> Color(0xFF92400E) // Amber-800
                     else -> Color.DarkGray
                }, 
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha=0.3f))
}

@Composable
fun TransactionItem(invoice: com.example.ivydentalapp.data.local.entity.InvoiceEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("INV-${invoice.invoiceId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(invoice.generatedAt ?: "N/A", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text("₱${String.format("%,.2f", invoice.totalAmount ?: 0.0)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Surface(
                color = if(invoice.paymentStatus == "Unpaid") Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                shape = RoundedCornerShape(4.dp)
            ) {
                 Text(
                    text = (invoice.paymentStatus ?: "Unknown").uppercase(), 
                    style = MaterialTheme.typography.labelSmall,
                    color = if(invoice.paymentStatus == "Unpaid") Color(0xFF92400E) else Color(0xFF166534),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha=0.5f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailDialog(
    invoice: com.example.ivydentalapp.data.local.entity.InvoiceEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        content = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Title Bar
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Invoice ID
                    Text("INVOICE ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("INV-${invoice.invoiceId}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date
                    Text("DATE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(invoice.generatedAt?.replace("T", " ")?.take(10) ?: "N/A", style = MaterialTheme.typography.bodyMedium)
                    
                     Spacer(modifier = Modifier.height(16.dp))
                    
                    // Service
                    Text("SERVICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(invoice.appointmentId?.let { "Appointment #$it" } ?: "General Service", style = MaterialTheme.typography.bodyMedium)
                   
                    Spacer(modifier = Modifier.height(16.dp))

                    // Total Amount
                    Text("TOTAL AMOUNT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("₱${String.format("%,.2f", invoice.totalAmount ?: 0.0)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Status
                    Text("PAYMENT STATUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if(invoice.paymentStatus == "Paid") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                         Text(
                            text = invoice.paymentStatus ?: "Unknown", 
                            style = MaterialTheme.typography.labelMedium,
                            color = if(invoice.paymentStatus == "Paid") Color(0xFF166534) else Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha=0.5f))
                    
                    Text("Payment Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PAYMENT METHOD", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("E-Wallet", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) // Assuming E-Wallet/GCash for simpler view
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("AMOUNT PAID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("₱${String.format("%,.2f", invoice.totalAmount ?: 0.0)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                             Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Jan 26, 2026, 07:23 AM", style = MaterialTheme.typography.labelSmall, color = Color.Gray) // Placeholder as we don't have pay history readily available in this Entity
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}
