package com.example.ivydentalapp.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import com.example.ivydentalapp.data.local.entity.NotificationEntity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.ui.auth.AuthViewModel
import com.example.ivydentalapp.ui.appointment.AppointmentViewModel
import com.example.ivydentalapp.ui.appointment.AppointmentViewModelFactory
import com.example.ivydentalapp.ui.appointment.InvoiceViewModel
import com.example.ivydentalapp.ui.appointment.InvoiceViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivydentalapp.ui.theme.Primary
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.ivydentalapp.IvyDentalApplication
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    currentUser: PatientEntity?,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.ivydentalapp.IvyDentalApplication
    
    // ViewModels
    val appointmentViewModel: com.example.ivydentalapp.ui.appointment.AppointmentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.example.ivydentalapp.ui.appointment.AppointmentViewModelFactory(
            app.appointmentRepository,
            app.invoiceRepository,
            app.paymentSimulationService,
            app.notificationRepository
        )
    )
    val invoiceViewModel: com.example.ivydentalapp.ui.appointment.InvoiceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.example.ivydentalapp.ui.appointment.InvoiceViewModelFactory(app.invoiceRepository)
    )
    val notificationViewModel: com.example.ivydentalapp.ui.notification.NotificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.example.ivydentalapp.ui.notification.NotificationViewModelFactory(app.notificationRepository)
    )

    var currentTab by remember { mutableStateOf(DashboardTab.Home) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Load Data
    LaunchedEffect(currentUser) {
        currentUser?.let {
            appointmentViewModel.loadAppointments(it.patientId)
            invoiceViewModel.loadInvoices(it.patientId)
            notificationViewModel.loadNotifications(it.patientId, "Patient")
        }
    }
    
    val appointmentUiState by appointmentViewModel.uiState.collectAsState()
    val invoiceUiState by invoiceViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    Scaffold(
        topBar = { 
            DashboardTopBar(
                hasUnread = notificationUiState.hasUnread,
                onNotificationClick = { showNotificationDialog = true }
            ) 
        },
        bottomBar = {
             NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                DashboardTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = Primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                DashboardTab.Home -> HomeContent(
                    currentUser = currentUser, 
                    onNavigateToTab = { tab -> currentTab = tab },
                    appointments = appointmentUiState.appointments,
                    invoices = invoiceUiState.invoices
                )
                DashboardTab.Appointments -> AppointmentsContent(currentUser = currentUser)
                DashboardTab.Payments -> PaymentsContent(patientId = currentUser?.patientId ?: 0)
                DashboardTab.Profile -> {
                    val authUiState by viewModel.uiState.collectAsState()
                    ProfileContent(
                        currentUser = currentUser, 
                        allergies = authUiState.userAllergies,
                        onLogout = onLogout,
                        onUpdateProfile = { updatedPatient, newAllergies ->
                             viewModel.updateProfile(updatedPatient, newAllergies)
                        }
                    )
                }
            }
            
            if (showNotificationDialog) {
                NotificationDialog(
                    notifications = notificationUiState.notifications,
                    onDismiss = { showNotificationDialog = false },
                    onMarkAsRead = { id -> notificationViewModel.markAsRead(id) },
                    onClearAll = { currentUser?.let { notificationViewModel.markAllAsRead(it.patientId, "Patient") } }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    hasUnread: Boolean = false,
    onNotificationClick: () -> Unit = {}
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.background(com.example.ivydentalapp.ui.theme.BlueGradient)
    ) {
        TopAppBar(
            title = {
                Text("Ivy Dental Clinic", style = MaterialTheme.typography.titleMedium, color = Color.White)
            },
            actions = {
                IconButton(onClick = onNotificationClick) {
                    Box {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                        if (hasUnread) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, androidx.compose.foundation.shape.CircleShape)
                                    .align(androidx.compose.ui.Alignment.TopEnd)
                            )
                        }
                    }
                }
                IconButton(onClick = { /* TODO: Maybe profile shortcut? */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

@Composable
fun NotificationDialog(
    notifications: List<com.example.ivydentalapp.data.local.entity.NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAsRead: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Notifications", fontWeight = FontWeight.Bold, color = Primary)
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Mark all read", fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No notifications", color = Color.Gray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(notifications) { notification ->
                        val isPayment = notification.title.contains("Payment", true)
                        val isDiscount = notification.title.contains("Discount", true)
                        val icon = when {
                            isPayment -> Icons.Default.Payments
                            isDiscount -> Icons.Default.Discount
                            else -> Icons.Default.Info
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (notification.isRead) Color.White else Color(0xFFF0F9FF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (!notification.isRead) BorderStroke(1.dp, Primary.copy(alpha=0.3f)) else BorderStroke(1.dp, Color.LightGray.copy(alpha=0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            onClick = { onMarkAsRead(notification.notificationId) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                Surface(
                                    color = if (notification.isRead) Color.Gray.copy(alpha=0.1f) else Primary.copy(alpha=0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        icon, 
                                        contentDescription = null, 
                                        tint = if (notification.isRead) Color.Gray else Primary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        notification.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (notification.isRead) Color.Gray else Color.Black
                                    )
                                    Text(
                                        notification.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (notification.isRead) Color.Gray.copy(alpha=0.8f) else Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription=null, modifier = Modifier.size(10.dp), tint=Color.LightGray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            notification.createdAt.replace("T", " ").take(16),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                                if (!notification.isRead) {
                                    Box(
                                        modifier = Modifier.size(8.dp).background(Primary, androidx.compose.foundation.shape.CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Primary)
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        tonalElevation = 6.dp
    )
}
