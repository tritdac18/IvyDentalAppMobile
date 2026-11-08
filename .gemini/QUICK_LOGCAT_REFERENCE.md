# 🔍 Quick Logcat Filter Reference

## Copy-Paste Filters

### For Appointment Dropdown Issue
```
tag:InvoiceViewModel | tag:InvoicesContent
```

### For Database Issues
```
tag:AppointmentDao | tag:InvoiceDao | tag:IvyDentalApp
```

### For All Invoice-Related Logs
```
tag:InvoiceViewModel | tag:InvoicesContent | tag:AppointmentDao | tag:InvoiceDao
```

### Exclude Verbose Logs (Cleaner Output)
```
tag:InvoiceViewModel | tag:InvoicesContent -level:verbose -level:debug
```

---

## 🎯 What Each Tag Shows

| Tag | What It Shows |
|-----|---------------|
| `InvoiceViewModel` | Appointment loading, counts, statuses |
| `InvoicesContent` | UI filtering, patient selection |
| `AppointmentDao` | Database queries for appointments |
| `InvoiceDao` | Database queries for invoices |
| `IvyDentalApp` | App initialization, test data |

---

## 🚨 Quick Troubleshooting

### Dropdown is Empty?

**Step 1:** Apply this filter:
```
tag:InvoiceViewModel
```

**Step 2:** Look for this line:
```
Found X appointments WITHOUT invoices
```

**If X = 0:**
- No appointments exist OR
- All appointments have invoices OR
- Appointments have wrong status

**If X > 0 but dropdown still empty:**
- Apply filter: `tag:InvoicesContent`
- Check: `Filtered appointments: X`
- If 0, there's a patient ID mismatch

---

## 💻 Emulator Running Constantly?

### Quick Fixes:

1. **Pause Emulator:** Click ⏸️ in emulator toolbar
2. **Close Emulator:** Click ❌ to fully stop it
3. **Use Physical Device:** Faster and less resource-heavy
4. **Reduce RAM:** AVD Manager → Edit → Set RAM to 2048 MB

### Why It Happens:
- Background coroutines in `IvyDentalApplication.kt`
- Database observers (LiveData/Flow)
- Android system services

---

## 📱 How to Apply Filter in Android Studio

1. Open **Logcat** (bottom panel)
2. Find the **filter box** (top of Logcat)
3. **Paste** one of the filters above
4. Press **Enter**
5. **Run your app** and watch logs appear

---

## ✅ Expected Output (When Working)

```
D/InvoiceViewModel: Loading appointments for patient: 1
D/InvoiceViewModel: Total appointments for patient 1: 3
D/InvoiceViewModel: Found 2 appointments WITHOUT invoices
D/InvoiceViewModel:   - Appointment 1: status=Scheduled, service=Cleaning
D/InvoicesContent: Filtering appointments for patient: 1
D/InvoicesContent: Filtered appointments: 2
```

If you see this, the dropdown **should** show 2 appointments!

---

## 🛠️ Still Not Working?

1. **Clear App Data:**
   - Emulator Settings → Apps → Ivy Dental → Clear Data
   
2. **Rebuild Project:**
   - Build → Rebuild Project
   
3. **Check Database:**
   - View → Tool Windows → App Inspection → Database Inspector
   - Look at `tbl_appointments` table
   
4. **Share Logs:**
   - Right-click Logcat → Export to Text File
   - Share with developer

---

## 📋 Common Log Patterns

### ✅ Good (Appointments Found)
```
D/InvoiceViewModel: Found 2 appointments WITHOUT invoices
```

### ❌ Bad (No Appointments)
```
D/InvoiceViewModel: Found 0 appointments WITHOUT invoices
```

### ⚠️ Warning (Mismatch)
```
D/InvoicesContent: Total appointments in state: 2
D/InvoicesContent: Filtered appointments: 0
```
This means patient ID doesn't match!

---

## 🎓 Pro Tips

- **Clear Logcat** before testing (🗑️ button)
- **Use regex** in filter: `tag:Invoice.*` matches all Invoice tags
- **Save filters** as favorites in Android Studio
- **Export logs** for later analysis
- **Use Database Inspector** to verify data directly

---

For detailed guide, see: `LOGCAT_FILTERS_AND_DEBUGGING.md`
