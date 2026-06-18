package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.ExpenseViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val isAddOpen by viewModel.isAddExpenseOpen.collectAsState()
            val editingExpense by viewModel.editingExpense.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                // Show navigation components exclusively for central views
                val showBottomNav = currentScreen in listOf("dashboard", "analytics", "budgets", "insights", "settings")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen == "dashboard",
                                    onClick = { viewModel.navigateTo("dashboard") },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "analytics",
                                    onClick = { viewModel.navigateTo("analytics") },
                                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Analytics") },
                                    label = { Text("Analytics") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "budgets",
                                    onClick = { viewModel.navigateTo("budgets") },
                                    icon = { Icon(Icons.Filled.Wallet, contentDescription = "Budgets") },
                                    label = { Text("Budgets") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "insights",
                                    onClick = { viewModel.navigateTo("insights") },
                                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Insights") },
                                    label = { Text("AI Insights") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "settings",
                                    onClick = { viewModel.navigateTo("settings") },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentScreen == "dashboard") {
                            FloatingActionButton(
                                onClick = { viewModel.setAddExpenseOpen(true) },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "splash" -> SplashScreen(viewModel)
                            "onboarding" -> OnboardingScreen(viewModel)
                            "dashboard" -> DashboardScreen(viewModel)
                            "analytics" -> AnalyticsScreen(viewModel)
                            "budgets" -> BudgetScreen(viewModel)
                            "insights" -> SmartInsightsScreen(viewModel)
                            "settings" -> SettingsScreen(viewModel)
                        }
                    }

                    // Global Modal add/edit form dialog
                    if (isAddOpen) {
                        ExpenseDialog(
                            viewModel = viewModel,
                            editingExpense = editingExpense,
                            onDismiss = { viewModel.setAddExpenseOpen(false) }
                        )
                    }
                }
            }
        }
    }
}
