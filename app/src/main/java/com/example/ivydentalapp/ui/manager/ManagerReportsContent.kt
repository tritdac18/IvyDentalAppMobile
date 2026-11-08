package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerReportsContent() {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    val viewModel: ManagerReportsViewModel = viewModel(
        factory = ManagerReportsViewModelFactory(app)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedPeriod by remember { mutableStateOf("This Month") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Transactions, 1: Appointment History
    var periodExpanded by remember { mutableStateOf(false) }
    
    val periods = listOf("Today", "This Week", "This Month", "Last 30 Days", "This Year")

    LaunchedEffect(selectedPeriod) {
        viewModel.loadData(selectedPeriod)
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
            Column {
                Text(
                    text = "Staff Sales & Performance Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ivydentalapp.ui.theme.TextDark
                )
                Text(
                    text = "Overview of clinic revenue and appointment metrics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
            }
        }

        // Filters (Dropdown)
        item {
            ExposedDropdownMenuBox(
                expanded = periodExpanded,
                onExpandedChange = { periodExpanded = !periodExpanded },
                modifier = Modifier.width(200.dp)
            ) {
                OutlinedTextField(
                    value = selectedPeriod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time Period") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                    modifier = Modifier.menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = periodExpanded,
                    onDismissRequest = { periodExpanded = false }
                ) {
                    periods.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                selectedPeriod = period
                                periodExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            // Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReportStatCard("Revenue", "₱${String.format("%.2f", uiState.totalRevenue)}", uiState.revenueChange, Icons.Default.Payments, Color(0xFF8B5CF6), Modifier.weight(1f))
                        ReportStatCard("Visits", "${uiState.totalVisits}", uiState.visitsChange, Icons.Default.Groups, Color(0xFF3B82F6), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReportStatCard("Bookings", "${uiState.totalBookings}", uiState.bookingsChange, Icons.Default.Event, Color(0xFFF59E0B), Modifier.weight(1f))
                        ReportStatCard("New Customers", "${uiState.newCustomers}", uiState.newCustomersChange, Icons.Default.PersonAdd, Color(0xFF10B981), Modifier.weight(1f))
                    }
                }
            }
        }

        // Tabs (Transactions / Appointments)
        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Transactions") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Appointment History") })
            }
        }

        // Tab Content
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedTab == 0) {
                        Text("Transaction History", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        
                        if (uiState.transactions.isEmpty()) {
                            Text("No transactions found for this period.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                        } else {
                            uiState.transactions.forEach { payment ->
                                TransactionItem(payment)
                                Divider(color = Color(0xFFF1F5F9))
                            }
                        }
                    } else {
                        Text("Completed Appointments", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                        if (uiState.appointmentHistory.isEmpty()) {
                            Text("No completed appointments found for this period.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                        } else {
                            uiState.appointmentHistory.forEach { appointment ->
                                AppointmentHistoryItem(appointment)
                                Divider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(change, style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981)) // Green for positive
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun TransactionItem(payment: com.example.ivydentalapp.data.local.entity.PaymentEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Inv #${payment.invoiceId} - ${payment.paymentType}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(payment.createdAt ?: "No Date", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text("₱${payment.amountPaid}", fontWeight = FontWeight.Bold, color = Primary)
    }
}

@Composable
fun AppointmentHistoryItem(appointment: com.example.ivydentalapp.data.local.entity.AppointmentEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(appointment.serviceType ?: "General Service", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(appointment.scheduledDatetime, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Surface(
            color = Color(0xFFDCFCE7),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Completed",
                color = Color(0xFF166534),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
