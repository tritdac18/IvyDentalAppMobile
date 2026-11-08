package com.example.ivydentalapp.ui.receptionist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceptionistHomeContent(
    currentUser: ReceptionistEntity?,
    onNavigateToTab: (ReceptionistTab) -> Unit
) {
    val firstName = currentUser?.firstName ?: "Receptionist"
    val currentDate = remember { 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ivydentalapp.ui.theme.BackgroundLight),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp), // Adjusted top padding to 10dp
        verticalArrangement = Arrangement.spacedBy(10.dp) // Adjusted spacing to 10dp
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Changed from Bottom to CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Text(
                        text = "Welcome back, $firstName! Here's what's happening at the clinic today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ivydentalapp.ui.theme.TextGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // Date Display
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ivydentalapp.ui.theme.BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = com.example.ivydentalapp.ui.theme.TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = com.example.ivydentalapp.ui.theme.TextGray
                        )
                    }
                }
            }
        }

        // Statistics Cards - First Row


        // Today's Appointments Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Appointments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ivydentalapp.ui.theme.TextDark
                        )
                        TextButton(onClick = { onNavigateToTab(ReceptionistTab.Appointments) }) {
                            Text("View All", color = Primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = com.example.ivydentalapp.ui.theme.TextGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No appointments scheduled for today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = com.example.ivydentalapp.ui.theme.TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 100.dp), // Reduced from 120.dp
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp) // Reduced from 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = iconColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .padding(6.dp) // Reduced from 8.dp
                            .size(20.dp) // Reduced from 24.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Reduced from 12.dp
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = com.example.ivydentalapp.ui.theme.TextGray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp)) // Reduced from 4.dp
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge, // Slightly smaller than headlineMedium
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark
            )
        }
    }
}
