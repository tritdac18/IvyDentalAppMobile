package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.ui.theme.Primary
import com.example.ivydentalapp.ui.receptionist.StatCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ManagerHomeContent(
    currentUser: ReceptionistEntity?,
    onNavigateToTab: (ManagerTab) -> Unit
) {
    val firstName = currentUser?.firstName ?: "Manager"
    val currentDate = remember { 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ivydentalapp.ui.theme.BackgroundLight),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Manager Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Text(
                        text = "Welcome back, $firstName! Overview of clinic performance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ivydentalapp.ui.theme.TextGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                        TextButton(onClick = { onNavigateToTab(ManagerTab.Appointments) }) {
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
