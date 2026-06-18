package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Expense
import com.example.ui.ExpenseViewModel
import com.example.ui.charts.getCategoryColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    viewModel: ExpenseViewModel,
    editingExpense: Expense?,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf(editingExpense?.amount?.let { if (it % 1 == 0.0) String.format("%.0f", it) else it.toString() } ?: "") }
    var notes by remember { mutableStateOf(editingExpense?.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(editingExpense?.category ?: "Food") }
    var selectedMethod by remember { mutableStateOf(editingExpense?.paymentMethod ?: "Cash") }
    var selectedTimestamp by remember { mutableStateOf(editingExpense?.date ?: System.currentTimeMillis()) }

    var isAmountError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val categories = listOf("Food", "Transport", "Shopping", "Education", "Entertainment", "Healthcare", "Bills", "Mobile & Internet", "Travel", "Other")
    val paymentMethods = listOf("Cash", "Bank Transfer", "EasyPaisa", "JazzCash", "Credit/Debit Card")

    // Dropdown States
    var categoryExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    // Date Format helper
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (editingExpense != null) "Edit Expense in PKR" else "Add Expense in PKR",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        isAmountError = false
                    },
                    label = { Text("Amount Spent") },
                    placeholder = { Text("e.g. 1500") },
                    prefix = { Text("Rs ") },
                    isError = isAmountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isAmountError) {
                    Text(
                        text = "Please write a valid budget or expense amount",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Localized Categories Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(getCategoryColor(category), RoundedCornerShape(3.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Wallet / Channel") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedMethod = method
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Interaction Label
                OutlinedTextField(
                    value = dateFormat.format(Date(selectedTimestamp)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date of Expense") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val newCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }
                                    selectedTimestamp = newCalendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Pick date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Note/Description field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Expense Details / Note") },
                    placeholder = { Text("e.g. Samosas, Careem cab, super card, petrol") },
                    leadingIcon = { Icon(Icons.Filled.Edit, "Notes icon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountStr.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0.0) {
                                isAmountError = true
                            } else {
                                viewModel.saveExpense(
                                    amount = parsedAmount,
                                    category = selectedCategory,
                                    notes = notes.trim(),
                                    date = selectedTimestamp,
                                    paymentMethod = selectedMethod
                                )
                            }
                        }
                    ) {
                        Text("Save Wallet Action")
                    }
                }
            }
        }
    }
}
