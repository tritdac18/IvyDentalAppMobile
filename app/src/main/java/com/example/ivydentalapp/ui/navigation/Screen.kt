package com.example.ivydentalapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object ReceptionistDashboard : Screen("receptionist_dashboard")
    object ManagerDashboard : Screen("manager_dashboard")
}
