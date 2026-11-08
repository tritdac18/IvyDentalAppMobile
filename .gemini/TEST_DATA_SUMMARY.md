# Test Data Created!

The app will now automatically create:

## Test Accounts
- **Receptionist**: username: `receptionist`, password: `receptionist123`
- **Manager**: username: `manager`, password: `manager123`
- **Patient 1**: username: `patient1`, password: `password123` (John Doe)
- **Patient 2**: username: `patient2`, password: `password123` (Jane Smith)

## Test Appointments (Scheduled - Ready for Invoicing)
1. **John Doe** - Teeth Cleaning - Feb 10, 2026 at 10:00 AM
2. **John Doe** - Tooth Filling - Feb 15, 2026 at 2:00 PM  
3. **Jane Smith** - Teeth Cleaning - Feb 12, 2026 at 11:00 AM

## Next Steps

1. **IMPORTANT**: Uninstall the old app first to get a fresh database
   - Settings > Apps > Ivy Dental App > Uninstall
   - OR use: `adb uninstall com.example.ivydentalapp`

2. **Install the new version**
   - Run the app from Android Studio

3. **Test the appointment dropdown**:
   - Login as Receptionist
   - Go to Invoices
   - Select "John Doe" or "Jane Smith" from patient dropdown
   - Check Logcat for debug logs
   - The appointment dropdown should now show the scheduled appointments!

The debug logs will show:
```
D/InvoicesContent: LaunchedEffect triggered, selectedPatientId: X
D/InvoicesContent: Calling loadAppointmentsForPatient(X)
D/InvoiceViewModel: === APPOINTMENT DEBUG START ===
D/InvoiceViewModel: Total appointments for patient X: 2 (or 1)
D/InvoiceViewModel: Found X appointments WITHOUT invoices
D/InvoiceViewModel: === APPOINTMENT DEBUG END ===
```
