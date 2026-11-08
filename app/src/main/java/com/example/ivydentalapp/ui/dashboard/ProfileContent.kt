package com.example.ivydentalapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.ivydentalapp.ui.theme.Primary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileContent(
    currentUser: com.example.ivydentalapp.data.local.entity.PatientEntity?,
    allergies: List<String>,
    onLogout: () -> Unit,
    onUpdateProfile: (com.example.ivydentalapp.data.local.entity.PatientEntity, List<String>) -> Unit
) {
    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var isEditing by remember { mutableStateOf(false) }
    
    // Form State
    var firstName by remember(currentUser) { mutableStateOf(currentUser.firstName) }
    var lastName by remember(currentUser) { mutableStateOf(currentUser.lastName) }
    var phone by remember(currentUser) { mutableStateOf(currentUser.primaryPhone ?: "") }
    var dob by remember(currentUser) { mutableStateOf(currentUser.dob ?: "") }
    var gender by remember(currentUser) { mutableStateOf(currentUser.gender ?: "Prefer not to say") }
    var bloodType by remember(currentUser) { mutableStateOf(currentUser.bloodType ?: "Unknown") }
    
    // Allergies State
    val currentAllergies = remember(allergies) { mutableStateListOf(*allergies.toTypedArray()) }
    var allergyInput by remember { mutableStateOf("") }
    
    // Components (Reused local versions for simplicity)
    @Composable
    fun EditTextField(value: String, onValueChange: (String) -> Unit, label: String, readOnly: Boolean = false) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            enabled = !readOnly,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledContainerColor = Color(0xFFF5F5F5)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if(isEditing) "Edit Profile" else "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (isEditing) {
                Row {
                    TextButton(onClick = { isEditing = false }) { Text("Cancel", color = Color.Red) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedPatient = currentUser.copy(
                            firstName = firstName,
                            lastName = lastName,
                            primaryPhone = phone,
                            dob = dob,
                            gender = gender,
                            bloodType = bloodType
                        )
                        onUpdateProfile(updatedPatient, currentAllergies.toList())
                        isEditing = false
                    }) {
                        Text("Save")
                    }
                }
            }
        }

        // Avatar Section
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Primary.copy(alpha = 0.1f), CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (!isEditing) {
                    Text(
                        text = "${currentUser.firstName} ${currentUser.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Personal Information
        SectionHeader("Personal Information")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isEditing) {
                    EditTextField(value = currentUser.username, onValueChange = {}, label = "Username (Read-only)", readOnly = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { EditTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name") }
                        Box(Modifier.weight(1f)) { EditTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name") }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    EditTextField(value = phone, onValueChange = { phone = it }, label = "Phone")
                    Spacer(modifier = Modifier.height(12.dp))
                    EditTextField(value = dob, onValueChange = { dob = it }, label = "Date of Birth (YYYY-MM-DD)")
                    Spacer(modifier = Modifier.height(12.dp))
                    EditTextField(value = gender, onValueChange = { gender = it }, label = "Gender")
                } else {
                    ProfileItem(Icons.Outlined.Person, "Username", currentUser.username)
                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileItem(Icons.Outlined.Phone, "Phone", currentUser.primaryPhone ?: "N/A")
                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileItem(Icons.Default.Person, "Date of Birth", currentUser.dob ?: "N/A")
                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileItem(Icons.Default.Person, "Gender", currentUser.gender ?: "N/A")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Medical Information
        SectionHeader("Medical Information")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isEditing) {
                    EditTextField(value = bloodType, onValueChange = { bloodType = it }, label = "Blood Type")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Allergies", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = allergyInput,
                            onValueChange = { allergyInput = it },
                            placeholder = { Text("Add allergy") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { 
                            if (allergyInput.isNotBlank()) {
                                currentAllergies.add(allergyInput.trim())
                                allergyInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                    // Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        currentAllergies.forEach { allergy ->
                            SuggestionChip(
                                onClick = { currentAllergies.remove(allergy) },
                                label = { Text(allergy) },
                                icon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                } else {
                    ProfileItem(Icons.Outlined.MedicalServices, "Blood Type", currentUser.bloodType ?: "Unknown")
                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                    
                    Text("Allergies", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (allergies.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            allergies.forEach { allergy ->
                                SuggestionChip(onClick = {}, label = { Text(allergy) })
                            }
                        }
                    } else {
                        Text("No known allergies", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        if (!isEditing) {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
