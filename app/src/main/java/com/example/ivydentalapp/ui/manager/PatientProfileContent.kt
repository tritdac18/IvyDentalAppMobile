package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.ui.receptionist.ProfileItem
import com.example.ivydentalapp.ui.receptionist.SectionHeader
import com.example.ivydentalapp.ui.theme.Primary

@Composable
fun PatientProfileContent(
    patient: PatientEntity?
) {
    if (patient == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Patient Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
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
                Text(
                    text = "${patient.firstName} ${patient.lastName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${patient.patientId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = when(patient.status) {
                        "Active" -> Color(0xFFDCFCE7)
                        "Inactive" -> Color(0xFFFEE2E2)
                        else -> Color(0xFFFEF3C7)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = patient.status.replace("_", " ").capitalize(),
                        color = when(patient.status) {
                            "Active" -> Color(0xFF166534)
                            "Inactive" -> Color(0xFF991B1B)
                            else -> Color(0xFF92400E)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
                ProfileItem(Icons.Default.Person, "Username", patient.username)
                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                ProfileItem(Icons.Default.Cake, "Date of Birth", patient.dob ?: "Not set")
                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                ProfileItem(Icons.Default.Person, "Gender", patient.gender ?: "Not set")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Contact Information
        SectionHeader("Contact Information")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileItem(Icons.Outlined.Phone, "Phone", patient.primaryPhone ?: "N/A")
                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                ProfileItem(Icons.Default.Email, "Email", patient.email)
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
                ProfileItem(Icons.Default.Bloodtype, "Blood Type", patient.bloodType ?: "Unknown")
                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                // Note: Allergies are in a separate table, but for now we'll just show a placeholder or basic info
                // Need to fetch allergies ideally, but the entity doesn't have them directly.
                ProfileItem(Icons.Default.Warning, "Allergies", "View in Medical Records") 
            }
        }
        
    }
}
