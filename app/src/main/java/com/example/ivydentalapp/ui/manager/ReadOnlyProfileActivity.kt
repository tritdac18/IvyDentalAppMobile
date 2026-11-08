package com.example.ivydentalapp.ui.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ivydentalapp.ui.receptionist.ReceptionistProfileContent
import com.example.ivydentalapp.ui.theme.IvyDentalAppTheme

class ReadOnlyProfileActivity : ComponentActivity() {

    private lateinit var viewModel: ReadOnlyProfileViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("TYPE") ?: return // Should finish if invalid
        val id = intent.getIntExtra("ID", -1)

        viewModel = ViewModelProvider(this, ReadOnlyProfileViewModelFactory(application))[ReadOnlyProfileViewModel::class.java]

        if (id != -1) {
            if (type == "STAFF") {
                viewModel.loadStaff(id)
            } else if (type == "PATIENT") {
                viewModel.loadPatient(id)
            }
        }

        setContent {
            IvyDentalAppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (type == "STAFF") "Staff Profile" else "Patient Profile") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        if (type == "STAFF") {
                            val staff by viewModel.staff.collectAsState()
                            ReceptionistProfileContent(
                                currentUser = staff,
                                onLogout = {}, // No-op
                                onUpdateProfile = {}, // No-op
                                isReadOnly = true
                            )
                        } else {
                            val patient by viewModel.patient.collectAsState()
                            PatientProfileContent(patient = patient)
                        }
                    }
                }
            }
        }
    }
}
