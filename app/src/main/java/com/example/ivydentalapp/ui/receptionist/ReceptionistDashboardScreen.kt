package com.example.ivydentalapp.ui.receptionist

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.ui.theme.Primary
import com.example.ivydentalapp.ui.dashboard.DashboardTopBar
import androidx.compose.foundation.layout.Box

@Composable
fun ReceptionistDashboardScreen(
    onLogout: () -> Unit,
    currentUser: ReceptionistEntity?,
    onUpdateProfile: (ReceptionistEntity) -> Unit
) {
    var currentTab by remember { mutableStateOf(ReceptionistTab.Home) }

    Scaffold(
        topBar = { ReceptionistTopBar() },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                ReceptionistTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { 
                            Text(
                                // Shorten "Appointments" to prevent truncation
                                if (tab == ReceptionistTab.Appointments) "Appts" else tab.title,
                                maxLines = 1,
                                softWrap = false
                            ) 
                        },
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
                ReceptionistTab.Home -> ReceptionistHomeContent(
                    currentUser = currentUser,
                    onNavigateToTab = { tab -> currentTab = tab }
                )
                ReceptionistTab.Appointments -> ReceptionistAppointmentsContent()
                ReceptionistTab.Invoices -> ReceptionistInvoicesContent()
                ReceptionistTab.Payments -> ReceptionistPaymentsContent()
                ReceptionistTab.Profile -> ReceptionistProfileContent(
                    currentUser = currentUser,
                    onLogout = onLogout,
                    onUpdateProfile = onUpdateProfile
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionistTopBar() {
    // Use the same top bar as the patient's dashboard
    DashboardTopBar()
}

