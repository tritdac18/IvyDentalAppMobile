# Payment UI Redesign Plan

## Issues to Fix

### 1. Appointment Dropdown Not Showing Scheduled Appointments
**Status**: Query is correct, issue might be with appointment creation status
**Solution**: Verify appointments are being created with status "Scheduled" or "Confirmed"

### 2. Receptionist Payments UI Redesign
**Current**: Simple list with checkboxes at bottom
**Target**: PHP-style layout with detailed breakdown

#### Layout Structure (from payments.php image):
```
┌─────────────────────────────────────┬──────────────────────────┐
│ LEFT COLUMN (7/12)                  │ RIGHT COLUMN (5/12)      │
│                                     │                          │
│ ┌─────────────────────────────────┐ │ ┌──────────────────────┐ │
│ │ Transaction Context             │ │ │ INVOICE DETAILS      │ │
│ │ - Search Invoice ID/Patient     │ │ │ #INV-2026-046        │ │
│ │ - Selected Invoices List        │ │ │                      │ │
│ │ - Total: ₱9750.00              │ │ │ Patient Info         │ │
│ └─────────────────────────────────┘ │ │                      │ │
│                                     │ │ Service: Braces      │ │
│ ┌─────────────────────────────────┐ │ │ Subtotal: ₱15000     │ │
│ │ Payment Details                 │ │ │                      │ │
│ │ - Payment Method (Cash/PayMongo)│ │ │ TRANSACTION BREAKDOWN│ │
│ │ - Amount Received               │ │ │ Total Amount: ₱15000 │ │
│ │ - Change                        │ │ │ Downpayment: ₱5250   │ │
│ │ - Notes                         │ │ │ Balance Before: ₱9750│ │
│ └─────────────────────────────────┘ │ │ PWD Discount: -₱1950 │ │
│                                     │ │ Remaining Balance:   │ │
│                                     │ │ ₱ 7800.00           │ │
│                                     │ │                      │ │
│                                     │ │ [Confirm Payment]    │ │
│                                     │ │ [Cancel]             │ │
│                                     │ └──────────────────────┘ │
└─────────────────────────────────────┴──────────────────────────┘
```

### 3. Invoices Section Redesign
**Target**: Match invoices.php layout

#### Layout Structure (from invoices.php image):
```
┌──────────────────────────────────────────────────────────────┐
│ LEFT SIDEBAR (Create New Invoice)                            │
│ - Invoice ID (auto)                                          │
│ - Date                                                       │
│ - Patient (dropdown)                                         │
│ - Appointment (dropdown)                                     │
│ - Additional Expenses (optional)                             │
│ - Total Amount                                               │
│ [Generate Invoice Button]                                    │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│ RIGHT SECTION (Recent Invoices Table)                        │
│ ┌────────┬─────────┬────────┬────────┬────────┬──────────┐  │
│ │INV ID  │ PATIENT │ APPT ID│ AMOUNT │ STATUS │ ACTIONS  │  │
│ ├────────┼─────────┼────────┼────────┼────────┼──────────┤  │
│ │INV-854 │ Ander   │ APT-89 │ ₱620   │ Paid   │ Edit Del │  │
│ │INV-841 │ Nerissa │ APT-78 │ ₱9,750 │ Paid   │ Edit Del │  │
│ └────────┴─────────┴────────┴────────┴────────┴──────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## Implementation Steps

1. ✅ **Database & Logic** (Already Complete)
   - Downpayments don't create invoices
   - Manual invoice creation links downpayments
   - Cleanup orphaned partial invoices

2. 🔧 **Receptionist Payments UI** (To Do)
   - Redesign to 2-column layout
   - Left: Transaction Context + Payment Details cards (stacked)
   - Right: Invoice Details card (sticky)
   - Remove bottom invoice selection
   - Add detailed breakdown with downpayment deduction

3. 🔧 **Invoices Section UI** (To Do)
   - Redesign to sidebar + table layout
   - Left: Create Invoice form (compact card)
   - Right: Recent Invoices table
   - Ensure appointment dropdown shows scheduled appointments

## Key Features to Implement

### Invoice Details Card (Right Side)
- Patient name with initials badge
- Service name
- Subtotal
- Transaction Breakdown section:
  - Total Amount
  - Downpayment Paid (if any)
  - Balance Before Discount
  - PWD/Senior Discount (if applicable)
  - **Remaining Balance to Pay** (prominent)
- Payment Details section:
  - Payment Method
  - Amount Received
  - Change
- Confirm Payment button
- Cancel Transaction button

### Transaction Context (Left Top)
- Search bar for Invoice ID or Patient Name
- Dropdown results (not checkboxes)
- Selected invoices list with remove option
- Total amount display

### Payment Details (Left Bottom)
- Payment Method selector (Cash / PayMongo)
- For Cash:
  - Amount Received input
  - Change calculation (auto)
- Notes textarea (optional)

## Files to Modify

1. `ReceptionistPaymentsContent.kt` - Complete redesign
2. `ReceptionistInvoicesContent.kt` - Layout update
3. `InvoiceViewModel.kt` - Verify appointment loading
4. `AppointmentDao.kt` - Verify query includes "Scheduled"

## Next Steps

1. Start with Receptionist Payments UI redesign
2. Then update Invoices section layout
3. Test appointment dropdown functionality
4. Verify all calculations work correctly
