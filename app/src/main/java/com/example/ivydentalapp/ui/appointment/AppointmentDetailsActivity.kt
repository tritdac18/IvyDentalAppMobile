package com.example.ivydentalapp.ui.appointment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.ivydentalapp.ui.theme.IvyDentalAppTheme
import com.example.ivydentalapp.ui.theme.Primary

class AppointmentDetailsActivity : ComponentActivity() {

    private lateinit var viewModel: AppointmentDetailsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appointmentId = intent.getIntExtra("APPOINTMENT_ID", -1)
        if (appointmentId == -1) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this, AppointmentDetailsViewModelFactory(application))[AppointmentDetailsViewModel::class.java]
        viewModel.loadAppointment(appointmentId)

        setContent {
            IvyDentalAppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Appointment Details") },
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
                    AppointmentDetailsContent(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailsContent(
    modifier: Modifier = Modifier,
    viewModel: AppointmentDetailsViewModel
) {
    val appointment by viewModel.appointment.collectAsState()
    val patient by viewModel.patient.collectAsState()
    val dentist by viewModel.dentist.collectAsState()

    if (appointment == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Appointment ID: #${appointment?.appointmentId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = when(appointment?.status) {
                        "Scheduled" -> Primary.copy(alpha=0.1f)
                        "Pending" -> Color(0xFFFEF3C7)
                        "Completed" -> Color(0xFFDCFCE7)
                        "Cancelled" -> Color(0xFFFEE2E2)
                        else -> Color(0xFFF3F4F6)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = appointment?.status ?: "Unknown",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when(appointment?.status) {
                            "Scheduled" -> Primary
                            "Pending" -> Color(0xFFD97706)
                            "Completed" -> Color(0xFF166534)
                            "Cancelled" -> Color(0xFF991B1B)
                            else -> Color(0xFF374151)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date & Time
        DetailSection(title = "Schedule") {
            DetailItem(
                icon = Icons.Default.CalendarToday,
                label = "Date & Time",
                value = appointment?.scheduledDatetime ?: "N/A"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Service Details
        DetailSection(title = "Service Information") {
            DetailItem(
                icon = Icons.Default.MedicalServices,
                label = "Service Type",
                value = appointment?.serviceType ?: "General Checkup"
            )
            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
            DetailItem(
                icon = Icons.Default.AttachMoney,
                label = "Price",
                value = "₱${appointment?.price ?: 0.0}"
            )


        }

        Spacer(modifier = Modifier.height(16.dp))

        // Patient Details
        DetailSection(title = "Patient Information") {
            if (patient != null) {
                DetailItem(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = "${patient!!.firstName} ${patient!!.lastName}"
                )
                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                DetailItem(
                    icon = Icons.Default.Person,
                    label = "Patient ID",
                    value = patient!!.patientId.toString()
                )
            } else {
                 Text("Loading patient info...", color = Color.Gray, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dentist Details
        DetailSection(title = "Dentist Information") {
            if (dentist != null) {
                DetailItem(
                    icon = Icons.Default.Person,
                    label = "Dentist",
                    value = "Dr. ${dentist!!.firstName} ${dentist!!.lastName}"
                )
            } else {
                DetailItem(
                    icon = Icons.Default.Person,
                    label = "Dentist",
                    value = "Not assigned / Loading..."
                )
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
