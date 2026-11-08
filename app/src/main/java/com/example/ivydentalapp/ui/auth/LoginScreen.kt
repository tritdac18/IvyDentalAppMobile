package com.example.ivydentalapp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivydentalapp.ui.theme.Primary

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.ui.auth.AuthViewModel
import com.example.ivydentalapp.ui.auth.AuthViewModelFactory

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            (LocalContext.current.applicationContext as IvyDentalApplication).authRepository,
            (LocalContext.current.applicationContext as IvyDentalApplication).notificationRepository
        )
    )
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Handle Login Success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(com.example.ivydentalapp.ui.theme.BackgroundLight)) {
        // Background Header (Gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(com.example.ivydentalapp.ui.theme.BlueGradient) // Use new BlueGradient
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo placeholder
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                     Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ivy Dental Clinic",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "High quality dental care",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Login Form Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .offset(y = (-48).dp) // Pull up intersection
                .heightIn(min = 400.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ivydentalapp.ui.theme.TextDark
                )
                Text(
                    text = "Sign in to your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ivydentalapp.ui.theme.TextGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Email/Username Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it 
                        viewModel.clearError() 
                    },
                    label = { Text("Email or Username") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = com.example.ivydentalapp.ui.theme.Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.example.ivydentalapp.ui.theme.Primary,
                        unfocusedBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                        focusedLabelColor = com.example.ivydentalapp.ui.theme.Primary,
                        cursorColor = com.example.ivydentalapp.ui.theme.Primary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it 
                        viewModel.clearError()
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = com.example.ivydentalapp.ui.theme.Primary) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = com.example.ivydentalapp.ui.theme.TextGray)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.example.ivydentalapp.ui.theme.Primary,
                        unfocusedBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                        focusedLabelColor = com.example.ivydentalapp.ui.theme.Primary,
                        cursorColor = com.example.ivydentalapp.ui.theme.Primary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )

                // Forgot Password
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { /* TODO */ }) {
                        Text("Forgot password?", color = com.example.ivydentalapp.ui.theme.Primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(com.example.ivydentalapp.ui.theme.BlueGradient)
                        .clickable(enabled = !uiState.isLoading) { 
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.login(email, password)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign in", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Register Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New patient? ", color = com.example.ivydentalapp.ui.theme.TextGray)
                    Text(
                        text = "Register here",
                        color = com.example.ivydentalapp.ui.theme.Mint, // Use Mint for accent
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
