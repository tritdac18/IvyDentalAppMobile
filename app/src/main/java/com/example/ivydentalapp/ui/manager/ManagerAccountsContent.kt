package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ManagerAccountsContent() {
    val context = LocalContext.current
    val app = context.applicationContext as IvyDentalApplication
    
    var searchQuery by remember { mutableStateOf("") }
    var allStaff by remember { mutableStateOf<List<ReceptionistEntity>>(emptyList()) }
    var allPatients by remember { mutableStateOf<List<PatientEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load staff and patients from database
    LaunchedEffect(Unit) {
        try {

            val staff = withContext(Dispatchers.IO) {
                app.database.receptionistDao().getAllStaff()
            }
            val patients = withContext(Dispatchers.IO) {
                app.database.patientDao().getAllPatients()
            }
            allStaff = staff
            allPatients = patients
        } catch (e: Exception) {
            e.printStackTrace()
            // Set empty lists on error to prevent hanging
            allStaff = emptyList()
            allPatients = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Filter based on search query
    val filteredStaff = if (searchQuery.isBlank()) {
        allStaff
    } else {
        allStaff.filter {
            "${it.firstName} ${it.lastName}".contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPatients = if (searchQuery.isBlank()) {
        allPatients
    } else {
        allPatients.filter {
            "${it.firstName} ${it.lastName}".contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.patientId.toString().contains(searchQuery, ignoreCase = true)
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
            Text(
                text = "Accounts Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search patients or staff...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Staff Section
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = com.example.ivydentalapp.ui.theme.Primary)
                }
            }
        } else {
            item {
                Text(
                    text = "Staff Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

        if (filteredStaff.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isNotBlank()) "No staff found matching your search." else "No staff members found.",
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredStaff,
                key = { staff -> "staff_${staff.staffId}" }
            ) { staff ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${staff.firstName} ${staff.lastName}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                staff.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    color = when(staff.role) {
                                        "Manager" -> Color(0xFFF3E8FF)
                                        "Dentist" -> Color(0xFFE0F2FE)
                                        else -> Color(0xFFF3F4F6)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = staff.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when(staff.role) {
                                            "Manager" -> Color(0xFF6B21A8)
                                            "Dentist" -> Color(0xFF0369A1)
                                            else -> Color(0xFF374151)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "@${staff.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = staff.status == "Active",
                                onCheckedChange = { isActive ->
                                    val newStatus = if (isActive) "Active" else "Inactive"
                                    // Optimistic update
                                    allStaff = allStaff.map {
                                        if (it.staffId == staff.staffId) it.copy(status = newStatus) else it
                                    }
                                    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                                         app.database.receptionistDao().updateStatus(staff.staffId, newStatus)
                                    }
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val intent = android.content.Intent(context, ReadOnlyProfileActivity::class.java).apply {
                                        putExtra("TYPE", "STAFF")
                                        putExtra("ID", staff.staffId)
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View Profile",
                                    tint = com.example.ivydentalapp.ui.theme.Primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Patients Section
        item {
            Text(
                text = "Patients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        if (filteredPatients.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isNotBlank()) "No patients found matching your search." else "No patients found.",
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredPatients,
                key = { patient -> "patient_${patient.patientId}" }
            ) { patient ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${patient.firstName} ${patient.lastName}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                patient.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Patient",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = com.example.ivydentalapp.ui.theme.Primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ID: ${patient.patientId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                val intent = android.content.Intent(context, ReadOnlyProfileActivity::class.java).apply {
                                    putExtra("TYPE", "PATIENT")
                                    putExtra("ID", patient.patientId)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View Profile",
                                tint = com.example.ivydentalapp.ui.theme.Primary
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
