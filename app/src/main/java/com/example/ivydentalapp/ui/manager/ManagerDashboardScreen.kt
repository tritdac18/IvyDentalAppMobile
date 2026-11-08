package com.example.ivydentalapp.ui.manager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.ui.theme.Primary
import com.example.ivydentalapp.ui.receptionist.ReceptionistProfileContent
import com.example.ivydentalapp.ui.receptionist.ReceptionistTopBar

// Manager uses the same entity type as Receptionist for now (admin/staff)
@Composable
fun ManagerDashboardScreen(
    onLogout: () -> Unit,
    currentUser: ReceptionistEntity?,
    onUpdateProfile: (ReceptionistEntity) -> Unit
) {
    var currentTab by remember { mutableStateOf(ManagerTab.Dashboard) }

    Scaffold(
        topBar = { ReceptionistTopBar() }, // Re-use the nice blue topbar
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                ManagerTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { 
                            Text(
                                tab.title,
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall
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
                ManagerTab.Dashboard -> ManagerHomeContent(
                    currentUser = currentUser,
                    onNavigateToTab = { tab -> currentTab = tab }
                )
                ManagerTab.Appointments -> ManagerAppointmentsContent()
                ManagerTab.Reports -> ManagerReportsContent()
                ManagerTab.Accounts -> ManagerAccountsContent()
                ManagerTab.ServiceManagement -> ManagerServiceManagementContent()
                ManagerTab.Profile -> ReceptionistProfileContent(
                    currentUser = currentUser,
                    onUpdateProfile = onUpdateProfile,
                    onLogout = onLogout
                )
            }
        }
    }
}
