package com.example.ivydentalapp.ui.appointment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ivydentalapp.ui.theme.Primary

@Composable
fun GCashDownpaymentDialog(
    amount: Double,
    onConfirm: (String, String) -> Unit, // referenceNumber, accountName
    onDismiss: () -> Unit
) {
    var referenceNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GCash Downpayment Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ivydentalapp.ui.theme.TextDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Downpayment Amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ivydentalapp.ui.theme.TextGray
                )
                
                Text(
                    text = "₱${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // GCash QR code for Ivy Dental (gcash_qr_ivy_dental.jpg in res/drawable)
                Image(
                    painter = painterResource(id = com.example.ivydentalapp.R.drawable.gcash_qr_ivy_dental),
                    contentDescription = "GCash QR for Ivy Dental",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Please complete GCash payment to confirm your appointment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("GCash Reference Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("GCash Account Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            if (referenceNumber.isNotBlank() && accountName.isNotBlank()) {
                                onConfirm(referenceNumber, accountName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = referenceNumber.isNotBlank() && accountName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        Text(
                            "Confirm Payment",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

