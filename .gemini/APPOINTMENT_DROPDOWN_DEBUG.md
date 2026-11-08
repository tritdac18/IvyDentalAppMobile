# Appointment Dropdown Debugging - Changes Made

## Issue
Appointment dropdown not showing scheduled appointments when creating invoices.

## Root Cause Analysis

### What We Verified:
1. ✅ **Appointments are created with "Scheduled" status** 
   - File: `AppointmentViewModel.kt` line 178
   - Status: `status = "Scheduled"`

2. ✅ **Database query includes "Scheduled" status**
   - File: `AppointmentDao.kt` lines 41-49
   - Query: `WHERE a.status IN ('Scheduled', 'Confirmed', 'Completed')`

3. ✅ **ViewModel loads appointments correctly**
   - File: `InvoiceViewModel.kt` `loadAppointmentsForPatient()`
   - Uses: `appointmentRepository.getAppointmentsWithoutInvoicesForPatient(patientId)`

4. ✅ **UI filters appointments by patient ID**
   - File: `ReceptionistInvoicesContent.kt` line 353
   - Filter: `uiState.appointments.filter { it.patientId == selectedPatientId }`

## Changes Made

### 1. Added Debug Logging to InvoiceViewModel.kt
**Location**: Lines 116-133
**Purpose**: Track how many appointments are loaded from database

```kotlin
fun loadAppointmentsForPatient(patientId: Int) {
    viewModelScope.launch {
        try {
            android.util.Log.d("InvoiceViewModel", "Loading appointments for patient: $patientId")
            val appointments = appointmentRepository.getAppointmentsWithoutInvoicesForPatient(patientId)
            android.util.Log.d("InvoiceViewModel", "Found ${appointments.size} appointments without invoices")
            appointments.forEach { apt ->
                android.util.Log.d("InvoiceViewModel", "  - Appointment ${apt.appointmentId}: status=${apt.status}, service=${apt.serviceType}")
            }
            // ... rest of code
        }
    }
}
```

### 2. Added Debug Logging to ReceptionistInvoicesContent.kt
**Location**: Lines 349-361
**Purpose**: Track how appointments are filtered for the selected patient

```kotlin
LaunchedEffect(uiState.appointments) {
    if (selectedPatientId != null) {
        android.util.Log.d("InvoicesContent", "Filtering appointments for patient: $selectedPatientId")
        android.util.Log.d("InvoicesContent", "Total appointments in state: ${uiState.appointments.size}")
        patientAppointments = uiState.appointments.filter { it.patientId == selectedPatientId }
        android.util.Log.d("InvoicesContent", "Filtered appointments: ${patientAppointments.size}")
        patientAppointments.forEach { apt ->
            android.util.Log.d("InvoicesContent", "  - APT-${apt.appointmentId}: ${apt.serviceType}, status=${apt.status}")
        }
    }
}
```

## How to Test

1. **Rebuild the app** (Build > Rebuild Project)
2. **Run the app** and navigate to Receptionist > Invoices
3. **Select a patient** from the dropdown
4. **Check Logcat** for the following tags:
   - `InvoiceViewModel` - Shows appointments loaded from database
   - `InvoicesContent` - Shows appointments filtered for UI

### Expected Log Output:
```
D/InvoiceViewModel: Loading appointments for patient: 1
D/InvoiceViewModel: Found 2 appointments without invoices
D/InvoiceViewModel:   - Appointment 5: status=Scheduled, service=Teeth Cleaning
D/InvoiceViewModel:   - Appointment 7: status=Scheduled, service=Root Canal
D/InvoicesContent: Filtering appointments for patient: 1
D/InvoicesContent: Total appointments in state: 2
D/InvoicesContent: Filtered appointments: 2
D/InvoicesContent:   - APT-5: Teeth Cleaning, status=Scheduled
D/InvoicesContent:   - APT-7: Root Canal, status=Scheduled
```

### Possible Issues to Look For:

1. **No appointments loaded from database**
   - Log shows: `Found 0 appointments without invoices`
   - **Cause**: Either no appointments exist, or they all have invoices already
   - **Solution**: Create a new appointment with downpayment (should NOT create invoice)

2. **Appointments loaded but not filtered**
   - Log shows: `Total appointments in state: 2` but `Filtered appointments: 0`
   - **Cause**: Patient ID mismatch
   - **Solution**: Check that `selectedPatientId` matches `appointment.patientId`

3. **Appointments have wrong status**
   - Log shows: `status=Confirmed` instead of `status=Scheduled`
   - **Cause**: Appointment status changed after creation
   - **Solution**: Check if downpayment processing changes status

## Next Steps

After testing with logs:
1. If appointments are found → Dropdown should work, move to UI redesign
2. If no appointments found → Investigate why (check database directly)
3. If appointments found but not showing → Check UI rendering logic

## Files Modified
- `InvoiceViewModel.kt` - Added logging to loadAppointmentsForPatient
- `ReceptionistInvoicesContent.kt` - Added logging to appointment filtering
