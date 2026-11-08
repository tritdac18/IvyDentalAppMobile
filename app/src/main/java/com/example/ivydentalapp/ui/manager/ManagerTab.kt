package com.example.ivydentalapp.ui.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class ManagerTab(
    val title: String,
    val icon: ImageVector
) {
    Dashboard("Dashboard", Icons.Filled.Dashboard),
    Appointments("Appointments", Icons.Filled.CalendarMonth),
    Reports("Reports", Icons.Filled.Description),
    Accounts("Accounts", Icons.Filled.Group),
    ServiceManagement("Services", Icons.Filled.Business),
    Profile("Profile", Icons.Filled.Person)
}
