# Logcat Filters & Debugging Guide

## 🔍 Logcat Filters for Appointment Dropdown Issue

### Quick Filter (Recommended)
Copy and paste this into Android Studio's Logcat filter box:

```
tag:InvoiceViewModel | tag:InvoicesContent | tag:AppointmentDao | tag:IvyDentalApp
```

### Individual Filters

#### 1. **Invoice Creation Flow**
```
tag:InvoiceViewModel
```
Shows:
- When appointments are loaded for a patient
- How many appointments were found
- Details of each appointment (ID, status, service type)

#### 2. **UI Filtering**
```
tag:InvoicesContent
```
Shows:
- When patient is selected
- Total appointments in state
- Filtered appointments for selected patient
- Each appointment's details

#### 3. **Database Queries**
```
tag:AppointmentDao
```
Shows:
- SQL queries being executed
- Database-level appointment retrieval

#### 4. **App Initialization**
```
tag:IvyDentalApp
```
Shows:
- Database initialization
- Test data creation
- Account setup

### Combined Filter for Complete Flow
```
tag:InvoiceViewModel | tag:InvoicesContent | tag:AppointmentDao
```

---

## 📋 How to Use Logcat Filters in Android Studio

### Step 1: Open Logcat
1. Click on **Logcat** tab at the bottom of Android Studio
2. If not visible: **View** → **Tool Windows** → **Logcat**

### Step 2: Apply Filter
1. Look for the filter dropdown (usually says "Show only selected application")
2. In the search/filter box, paste one of the filters above
3. Press Enter

### Step 3: Test the Flow
1. Run your app
2. Navigate to **Receptionist** → **Invoices**
3. Click **"Create Invoice"**
4. Select a patient from the dropdown
5. Watch the logs appear in real-time

---

## 🐛 Expected Log Output

When everything works correctly, you should see:

```
D/InvoicesContent: LaunchedEffect triggered, selectedPatientId: null
D/InvoicesContent: LaunchedEffect triggered, selectedPatientId: 1
D/InvoicesContent: Calling loadAppointmentsForPatient(1)
D/InvoiceViewModel: Loading appointments for patient: 1
D/InvoiceViewModel: Found 2 appointments without invoices
D/InvoiceViewModel:   - Appointment 1: status=Scheduled, service=Teeth Cleaning
D/InvoiceViewModel:   - Appointment 2: status=Scheduled, service=Root Canal
D/InvoicesContent: Filtering appointments for patient: 1
D/InvoicesContent: Total appointments in state: 2
D/InvoicesContent: Filtered appointments: 2
D/InvoicesContent:   - APT-1: Teeth Cleaning, status=Scheduled
D/InvoicesContent:   - APT-2: Root Canal, status=Scheduled
```

---

## ❌ Common Issues & What to Look For

### Issue 1: No Appointments Found
**Log shows:**
```
D/InvoiceViewModel: Found 0 appointments without invoices
```

**Possible Causes:**
1. No appointments exist in the database
2. All appointments already have invoices
3. Appointments have wrong status (not "Scheduled", "Confirmed", or "Completed")

**Solution:**
- Create a new appointment with downpayment (should NOT create invoice)
- Check database directly using Database Inspector

### Issue 2: Appointments Not Filtered
**Log shows:**
```
D/InvoicesContent: Total appointments in state: 2
D/InvoicesContent: Filtered appointments: 0
```

**Possible Causes:**
- Patient ID mismatch
- `selectedPatientId` doesn't match `appointment.patientId`

**Solution:**
- Check the patient ID being selected
- Verify appointment.patientId matches

### Issue 3: Wrong Status
**Log shows:**
```
D/InvoiceViewModel:   - Appointment 1: status=Completed, service=...
```

**Possible Causes:**
- Appointment status changed after creation
- Downpayment processing changes status

**Solution:**
- Check `AppointmentViewModel.kt` to ensure status is "Scheduled"
- Verify payment processing doesn't change status

---

## 🔧 Emulator Performance Issues

### Why Your Emulator is Constantly Running

The emulator consumes resources even when idle because:

1. **Background Services**: Your app has background services running
2. **Database Operations**: Coroutines running in background
3. **LiveData/Flow Observers**: Continuously observing data changes
4. **System Services**: Android system processes

### Solutions to Reduce Emulator Load

#### Option 1: Stop Background Initialization (Recommended)
The app currently initializes test data in the background. You can disable this:

**File:** `IvyDentalApplication.kt` (lines 100-117)

The background initialization is already commented out in `onCreate()` (lines 22-35), but it's still running in the `database` lazy block.

#### Option 2: Close Emulator When Not Testing
- Click the **X** button on the emulator window
- Or use: **Tools** → **AVD Manager** → Stop the running device

#### Option 3: Use a Lighter Emulator Configuration
1. Open **AVD Manager**
2. Edit your emulator
3. Reduce:
   - RAM: 2048 MB (instead of 4096+)
   - VM Heap: 256 MB
   - Internal Storage: 2048 MB
4. Enable: **Hardware - GLES 2.0** (uses GPU acceleration)

#### Option 4: Use Physical Device
- Connect your Android phone via USB
- Enable **Developer Options** and **USB Debugging**
- Much faster and less resource-intensive

#### Option 5: Pause Emulator When Not in Use
- In the emulator toolbar, click the **⏸️ Pause** button
- This freezes the emulator state and reduces CPU usage

---

## 🎯 Quick Debugging Checklist

When appointment dropdown is not showing:

- [ ] Check Logcat with filter: `tag:InvoiceViewModel | tag:InvoicesContent`
- [ ] Verify appointments exist in database (use Database Inspector)
- [ ] Confirm appointment status is "Scheduled", "Confirmed", or "Completed"
- [ ] Ensure appointments don't already have invoices
- [ ] Check that patient ID matches between selection and appointments
- [ ] Rebuild project: **Build** → **Rebuild Project**
- [ ] Clear app data: **Settings** → **Apps** → **Ivy Dental** → **Clear Data**

---

## 📊 Using Database Inspector

### How to Open:
1. **View** → **Tool Windows** → **App Inspection**
2. Select **Database Inspector** tab
3. Select your app process
4. Browse tables: `tbl_appointments`, `tbl_invoices`, `tbl_patients`

### What to Check:
1. **tbl_appointments**: 
   - Do appointments exist?
   - What is their `status`?
   - Do they have `patient_id` set?

2. **tbl_invoices**:
   - Which `appointment_id` values have invoices?
   - Compare with appointments table

3. **tbl_patients**:
   - Verify patient IDs match

---

## 🚀 Performance Tips

### Reduce Logcat Noise
Add this to your filter to exclude verbose logs:
```
tag:InvoiceViewModel | tag:InvoicesContent -level:verbose
```

### Clear Logcat
Click the **🗑️ Clear** button in Logcat to start fresh

### Save Logs
Right-click in Logcat → **Export to Text File** to save for later analysis

---

## 📝 Next Steps

1. **Apply the logcat filter** and test the invoice creation flow
2. **Share the log output** if appointments still don't show
3. **Check Database Inspector** to verify data exists
4. **Reduce emulator load** using the solutions above

If you see `Found 0 appointments`, the issue is likely:
- No test appointments created
- Appointments already have invoices
- Database query filtering them out

If you see appointments found but dropdown empty, the issue is:
- UI filtering problem
- Patient ID mismatch
- State not updating properly
