package com.example.ivydentalapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivydentalapp.ui.theme.Primary

import androidx.compose.ui.draw.clip

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.ui.auth.AuthViewModel
import com.example.ivydentalapp.ui.auth.AuthViewModelFactory
import com.example.ivydentalapp.data.local.entity.PatientEntity
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import java.util.Date
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            (LocalContext.current.applicationContext as IvyDentalApplication).authRepository,
            (LocalContext.current.applicationContext as IvyDentalApplication).notificationRepository
        )
    )
) {
    // Scroll state for long form
    val scrollState = rememberScrollState()
    
    // Form State
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Dropdown State
    var bloodType by remember { mutableStateOf("Unknown") }
    var gender by remember { mutableStateOf("Prefer not to say") }
    
    // Allergy State
    var allergyInput by remember { mutableStateOf("") }
    val allergies = remember { mutableStateListOf<String>() }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Update dob when date is selected
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dob = sdf.format(Date(it))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Navigate to login when registration is successful
    LaunchedEffect(uiState.isRegistrationSuccess) {
        if (uiState.isRegistrationSuccess && !uiState.isLoading) {
            kotlinx.coroutines.delay(500) // Small delay to show success message
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.background(com.example.ivydentalapp.ui.theme.BlueGradient)
            ) {
                TopAppBar(
                    title = { Text("Create Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToLogin) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        },
        containerColor = com.example.ivydentalapp.ui.theme.BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Patient Registration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Enter your details below to create a new account.",
                style = MaterialTheme.typography.bodyMedium,
                color = com.example.ivydentalapp.ui.theme.TextGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 24.dp)
            )

            // Form container
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ivydentalapp.ui.theme.BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Username
                    RTextField(value = username, onValueChange = { username = it }, label = "Username", icon = Icons.Default.Person, placeholder = "johndoe123")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Name Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        RTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name", modifier = Modifier.weight(1f))
                        RTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Date Picker Field
                        Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                             RTextField(
                                value = dob, 
                                onValueChange = {}, 
                                label = "DOB", 
                                icon = Icons.Default.CalendarToday, 
                                placeholder = "YYYY-MM-DD",
                                readOnly = true,
                                onClick = { showDatePicker = true }
                            )
                        }
                        
                        RTextField(value = phone, onValueChange = { phone = it }, label = "Phone", icon = Icons.Default.Phone, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    RTextField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email, placeholder = "jane@example.com")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Medical Row (Blood/Gender Dropdowns)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        RDropdown(
                            label = "Blood Type",
                            options = listOf("Unknown", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                            selectedOption = bloodType,
                            onOptionSelected = { bloodType = it },
                            modifier = Modifier.weight(1f)
                        )
                        RDropdown(
                            label = "Gender",
                            options = listOf("Prefer not to say", "Male", "Female", "Other"),
                            selectedOption = gender,
                            onOptionSelected = { gender = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Allergies (Dynamic List)
                    Text(
                        text = "Allergies", 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = FontWeight.Bold, 
                        color = com.example.ivydentalapp.ui.theme.TextDark
                    )
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RTextField(
                            value = allergyInput, 
                            onValueChange = { allergyInput = it }, 
                            label = "", 
                            placeholder = "Add allergy...",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { 
                            if (allergyInput.isNotBlank()) {
                                allergies.add(allergyInput.trim())
                                allergyInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = com.example.ivydentalapp.ui.theme.Primary)
                        }
                    }
                    // Chips for allergies
                    if (allergies.isNotEmpty()) {
                        OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            allergies.forEach { allergy ->
                                SuggestionChip(
                                    onClick = { allergies.remove(allergy) },
                                    label = { Text(allergy) },
                                    icon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                     HorizontalDivider(color = com.example.ivydentalapp.ui.theme.BorderColor)
                     Spacer(modifier = Modifier.height(24.dp))

                    // Passwords
                    RTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, isPassword = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    RTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", icon = Icons.Default.Lock, isPassword = true)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Submit Button with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(com.example.ivydentalapp.ui.theme.BlueGradient) // Gradient Button
                            .clickable(enabled = !uiState.isLoading) { 
                                when {
                                    username.isBlank() -> viewModel.setValidationError("Username is required")
                                    email.isBlank() -> viewModel.setValidationError("Email is required")
                                    password.isBlank() -> viewModel.setValidationError("Password is required")
                                    password != confirmPassword -> viewModel.setValidationError("Passwords do not match")
                                    else -> {
                                        val patient = PatientEntity(
                                            username = username,
                                            firstName = firstName,
                                            lastName = lastName,
                                            email = email,
                                            password = password,
                                            dob = dob,
                                            primaryPhone = phone,
                                            bloodType = bloodType,
                                            gender = gender
                                        )
                                        viewModel.register(patient, allergies.toList())
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                         if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already a patient? ", color = com.example.ivydentalapp.ui.theme.TextGray)
                Text(
                    text = "Log In",
                    color = com.example.ivydentalapp.ui.theme.Mint, // Mint for action
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            
             Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    placeholder: String = "",
    isPassword: Boolean = false,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ivydentalapp.ui.theme.TextDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        
        // Handle click for read-only fields (like Date Picker)
        Box(modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }) {
             OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = com.example.ivydentalapp.ui.theme.TextGray.copy(alpha=0.6f)) },
                leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = com.example.ivydentalapp.ui.theme.Primary) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = com.example.ivydentalapp.ui.theme.Primary,
                    unfocusedBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = com.example.ivydentalapp.ui.theme.Primary,
                    disabledContainerColor = Color.White,
                    disabledBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                    disabledTextColor = com.example.ivydentalapp.ui.theme.TextDark
                ),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                enabled = !readOnly,
                readOnly = readOnly
            )
            // Overlay for click detection if readOnly (OutlinedTextField captures clicks even when disabled/readOnly sometimes)
            if (readOnly && onClick != null) {
                Box(modifier = Modifier.matchParentSize().clickable { onClick.invoke() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = com.example.ivydentalapp.ui.theme.TextDark,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp),
                 colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = com.example.ivydentalapp.ui.theme.Primary,
                    unfocusedBorderColor = com.example.ivydentalapp.ui.theme.BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
