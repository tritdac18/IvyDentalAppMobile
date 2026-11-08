package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.ServiceEntity
import com.example.ivydentalapp.ui.theme.Primary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ManagerServiceManagementContent(
    viewModel: ServiceViewModel = viewModel(
        factory = ServiceViewModelFactory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as IvyDentalApplication).serviceRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddServicePage by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ServiceEntity?>(null) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            showAddServicePage = false
            editingService = null
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
                        text = "Service Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Text(
                        text = "Manage dental services and pricing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ivydentalapp.ui.theme.TextGray
                    )
                }
                Button(
                    onClick = { 
                        editingService = null
                        showAddServicePage = true 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Service")
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

        // Service List
        if (uiState.services.isEmpty() && !uiState.isLoading) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No services found. Add your first service.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = com.example.ivydentalapp.ui.theme.TextGray
                        )
                    }
                }
            }
        } else {
            items(uiState.services) { service ->
                ServiceItem(
                    service = service,
                    onEdit = { 
                        editingService = service
                        showAddServicePage = true
                    },
                    onDelete = { showDeleteDialog = service },
                    onToggleStatus = { viewModel.toggleServiceStatus(service) }
                )
            }
        }
    }

    // Add/Edit Service Full-Screen Page
    if (showAddServicePage) {
        AddEditServicePage(
            service = editingService,
            viewModel = viewModel,
            onBack = {
                showAddServicePage = false
                editingService = null
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { service ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Service", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete \"${service.serviceName}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteService(service.serviceId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ServiceItem(
    service: ServiceEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    service.serviceName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                service.description?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${service.durationMinutes} mins • ₱${String.format("%.2f", service.basePrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (service.isActive) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (service.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (service.isActive) Color(0xFF166534) else Color(0xFF374151),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleStatus() }
                ) {
                    Switch(
                        checked = service.isActive,
                        onCheckedChange = { onToggleStatus() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (service.isActive) "Set Inactive" else "Set Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ivydentalapp.ui.theme.TextGray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServicePage(
    service: ServiceEntity?,
    viewModel: ServiceViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    val uiState by viewModel.uiState.collectAsState()
    
    var serviceName by remember { mutableStateOf(service?.serviceName ?: "") }
    var price by remember { mutableStateOf(service?.basePrice?.toString() ?: "") }
    var duration by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "") }
    var isActive by remember { mutableStateOf(service?.isActive ?: true) }
    
    // Prerequisites
    var allServices by remember { mutableStateOf<List<ServiceEntity>>(emptyList()) }
    var selectedPrerequisites by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var validityPeriodDays by remember { mutableStateOf("30") }
    
    // Load all services for prerequisites
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val services = app.serviceRepository.getActiveServices()
            withContext(Dispatchers.Main) {
                allServices = services.filter { it.serviceId != service?.serviceId }
            }
        }
    }

    // Close page on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (service != null) "Edit Service" else "Add New Service",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                .background(com.example.ivydentalapp.ui.theme.BackgroundLight)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success/Error Messages
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

            // Service Information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        if (service != null) "Edit Service" else "Add New Service",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )

                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Service Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("e.g., General Checkup") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() || char == '.' }) {
                                    price = it
                                }
                            },
                            label = { Text("Base Price (₱) *") },
                            modifier = Modifier.weight(1f),
                            prefix = { Text("₱", modifier = Modifier.padding(start = 8.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("0.00") }
                        )
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() }) {
                                    duration = it
                                }
                            },
                            label = { Text("Default Duration (minutes) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("60") }
                        )
                    }

                    // Is Active checkbox (only when editing)
                    if (service != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isActive,
                                onCheckedChange = { isActive = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Service is active and available for booking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = com.example.ivydentalapp.ui.theme.TextDark
                            )
                        }
                    } else {
                        // Info message for new services
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "New services are automatically set as active and available for booking. You can change this later if needed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }

            // Prerequisites Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Prerequisites (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Text(
                        "Select which services must be completed before this service can be booked. Patients will need to complete these prerequisite services first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ivydentalapp.ui.theme.TextGray
                    )

                    if (allServices.isEmpty()) {
                        Text(
                            "No existing services available. Prerequisites can be added after creating more services.",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ivydentalapp.ui.theme.TextGray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ivydentalapp.ui.theme.BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allServices.forEach { existingService ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedPrerequisites.contains(existingService.serviceId),
                                            onCheckedChange = { checked ->
                                                selectedPrerequisites = if (checked) {
                                                    selectedPrerequisites + existingService.serviceId
                                                } else {
                                                    selectedPrerequisites - existingService.serviceId
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            existingService.serviceName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = com.example.ivydentalapp.ui.theme.TextDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = validityPeriodDays,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() }) {
                                    validityPeriodDays = it
                                }
                            },
                            label = { Text("Prerequisite Validity Period (days)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("30") }
                        )
                        Text(
                            "Number of days a prerequisite appointment remains valid. Default: 30 days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ivydentalapp.ui.theme.TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val priceValue = price.toDoubleOrNull() ?: 0.0
                        val durationValue = duration.toIntOrNull() ?: 0
                        if (serviceName.isNotBlank() && priceValue > 0 && durationValue > 0) {
                            if (service != null) {
                                viewModel.updateService(
                                    service.copy(
                                        serviceName = serviceName,
                                        basePrice = priceValue,
                                        durationMinutes = durationValue,
                                        isActive = isActive
                                    )
                                )
                            } else {
                                viewModel.addService(
                                    serviceName,
                                    null, // No description field in PHP form
                                    priceValue,
                                    durationValue
                                )
                            }
                            // TODO: Save prerequisites and validity period when database schema supports it
                        }
                    },
                    enabled = serviceName.isNotBlank() && 
                              price.toDoubleOrNull()?.let { it > 0 } == true &&
                              duration.toIntOrNull()?.let { it > 0 } == true,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        if (service != null) "Update Service" else "Add Service",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
