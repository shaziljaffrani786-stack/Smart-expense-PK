package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ui.ExpenseViewModel
import java.io.File

@Composable
fun OnboardingScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var incomeStr by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    var isNameError by remember { mutableStateOf(false) }
    var isIncomeError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Load our generated branding image
    val logoPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(File("/app/src/main/res/drawable/ic_expense_logo.jpg"))
            .crossfade(true)
            .build()
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Application Splash Banner Graphic
                Image(
                    painter = logoPainter,
                    contentDescription = "Smart Expense PK Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Smart Expense PK",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Your Smart AI-Powered Local Financial Companion in PKR",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Set Up Your Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Name Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                isNameError = false
                            },
                            label = { Text("Your Name") },
                            placeholder = { Text("e.g. Mohammad Ali") },
                            leadingIcon = { Icon(Icons.Filled.Person, "Name icon") },
                            isError = isNameError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isNameError) {
                            Text(
                                "Please list your name to personalize your reports",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Income/Salary Field
                        OutlinedTextField(
                            value = incomeStr,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    incomeStr = it
                                    isIncomeError = false
                                }
                            },
                            label = { Text("Monthly Income (PKR)") },
                            placeholder = { Text("e.g. 75000") },
                            leadingIcon = { Icon(Icons.Filled.Money, "Money icon") },
                            isError = isIncomeError,
                            suffix = { Text("Rs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isIncomeError) {
                            Text(
                                "Please define a valid salary value",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // PIN Code Field
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pin = it
                                    pinError = ""
                                }
                            },
                            label = { Text("Local Security PIN (4 Digits)") },
                            placeholder = { Text("e.g. 1234") },
                            leadingIcon = { Icon(Icons.Filled.Lock, "Lock icon") },
                            isError = pinError.isNotEmpty(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pinError.isNotEmpty()) {
                            Text(
                                pinError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        var isValid = true
                        if (name.trim().isEmpty()) {
                            isNameError = true
                            isValid = false
                        }
                        val salary = incomeStr.toDoubleOrNull()
                        if (salary == null || salary <= 0) {
                            isIncomeError = true
                            isValid = false
                        }
                        if (pin.length < 4) {
                            pinError = "Security PIN must be exactly 4 digits"
                            isValid = false
                        }

                        if (isValid && salary != null) {
                            viewModel.completeOnboarding(name.trim(), salary, pin)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register & Launch Wallet", modifier = Modifier.padding(4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Seed demo data helper for streaming emulator
                OutlinedButton(
                    onClick = { viewModel.seedDemoData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pre-fill with Pakistani Demo Data 🇵🇰")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "All your expense history and PIN login data are stored completely offline locally on this device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
