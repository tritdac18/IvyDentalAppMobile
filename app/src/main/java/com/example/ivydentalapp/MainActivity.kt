package com.example.ivydentalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.example.ivydentalapp.ui.auth.LoginScreen
import com.example.ivydentalapp.ui.auth.RegisterScreen
import com.example.ivydentalapp.ui.dashboard.DashboardScreen
import com.example.ivydentalapp.ui.receptionist.ReceptionistDashboardScreen
import com.example.ivydentalapp.ui.navigation.Screen
import com.example.ivydentalapp.ui.theme.IvyDentalAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up global exception handler to catch crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            android.util.Log.e("MainActivity", "Uncaught exception", exception)
            exception.printStackTrace()
        }
        
        enableEdgeToEdge()
        setContent {
            IvyDentalAppTheme {
                val navController = rememberNavController()
                
                val app = try {
                    application as? IvyDentalApplication
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error getting application", e)
                    null
                }
                
                if (app == null) {
                    // Show error message if application is not available
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.Text(
                                "Application initialization error",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                                color = androidx.compose.ui.graphics.Color.Red
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.material3.Text(
                                "Please restart the app",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    // Safely access repositories
                    val authRepo = try { app.authRepository } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error accessing authRepository", e)
                        null
                    }
                    if (authRepo == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                androidx.compose.material3.Text(
                                    "Error initializing app",
                                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                                    color = androidx.compose.ui.graphics.Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.Text(
                                    "Please restart the app",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        val viewModel: com.example.ivydentalapp.ui.auth.AuthViewModel = 
                            androidx.lifecycle.viewmodel.compose.viewModel(
                                factory = com.example.ivydentalapp.ui.auth.AuthViewModelFactory(
                                    authRepo,
                                    app.notificationRepository
                                )
                            )

                        NavHost(navController = navController, startDestination = Screen.Login.route) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                val uiState = viewModel.uiState.value
                                val destination = when (uiState.userType) {
                                    "receptionist" -> Screen.ReceptionistDashboard.route
                                    "manager" -> Screen.ManagerDashboard.route
                                    else -> Screen.Dashboard.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            },
                            viewModel = viewModel
                        )
                    }
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                    composable(Screen.Dashboard.route) {
                        val uiState by viewModel.uiState.collectAsState()
                        DashboardScreen(
                            currentUser = uiState.currentUser,
                            onLogout = {
                                viewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true } // Clear back stack
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                    composable(Screen.ReceptionistDashboard.route) {
                        val uiState by viewModel.uiState.collectAsState()
                        ReceptionistDashboardScreen(
                            currentUser = uiState.currentReceptionist,
                            onLogout = {
                                viewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true } // Clear back stack
                                }
                            },
                            onUpdateProfile = { receptionist ->
                                viewModel.updateReceptionistProfile(receptionist)
                            }
                        )
                    }
                    composable(Screen.ManagerDashboard.route) {
                        val uiState by viewModel.uiState.collectAsState()
                        com.example.ivydentalapp.ui.manager.ManagerDashboardScreen(
                            currentUser = uiState.currentReceptionist, // Manager is stored here too
                            onLogout = {
                                viewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onUpdateProfile = { receptionist ->
                                viewModel.updateReceptionistProfile(receptionist)
                            }
                        )
                    }
                    }
                    }
                }
            }
        }
    }
}