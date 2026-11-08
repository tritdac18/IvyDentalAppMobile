package com.example.ivydentalapp.ui.receptionist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class ReceptionistTab(
    val title: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Filled.Dashboard),
    Appointments("Appointments", Icons.Filled.CalendarMonth),
    Invoices("Invoices", Icons.Filled.Description),
    Payments("Payments", Icons.Filled.Payments),
    Profile("Profile", Icons.Filled.Person)
}

