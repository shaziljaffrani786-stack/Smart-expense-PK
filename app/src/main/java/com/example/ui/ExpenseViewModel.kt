package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiInsightsService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AiInsightsState {
    object Idle : AiInsightsState
    object Loading : AiInsightsState
    data class Success(val insights: String) : AiInsightsState
    data class Error(val message: String) : AiInsightsState
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ExpenseRepository(database.expenseDao())
    private val geminiService = GeminiInsightsService()

    // Database flows
    val expenses = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets = repository.allBudgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userProfile = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // UI States
    private val _aiInsightsState = MutableStateFlow<AiInsightsState>(AiInsightsState.Idle)
    val aiInsightsState: StateFlow<AiInsightsState> = _aiInsightsState.asStateFlow()

    // Screen navigation state (Onboarding, Home, AddExpense, Analytics, Budgets, Settings, AIInsights)
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Dark theme configuration (persisted in ViewModel memory / locally)
    private val _isDarkTheme = MutableStateFlow(true) // Premium slate-dark theme by default
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Add Expense dialog visibility
    private val _isAddExpenseOpen = MutableStateFlow(false)
    val isAddExpenseOpen: StateFlow<Boolean> = _isAddExpenseOpen.asStateFlow()

    // Edit Expense state (if editing)
    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setAddExpenseOpen(open: Boolean) {
        _isAddExpenseOpen.value = open
        if (!open) _editingExpense.value = null
    }

    fun setEditingExpense(expense: Expense?) {
        _editingExpense.value = expense
        if (expense != null) {
            _isAddExpenseOpen.value = true
        }
    }

    // --- Onboarding & Setup Actions ---

    fun completeOnboarding(name: String, income: Double, pin: String) {
        viewModelScope.launch {
            val profile = UserProfile(
                userName = name,
                monthlyIncome = income,
                securityPin = pin,
                isOnboarded = true
            )
            repository.saveUserProfile(profile)

            // Setup default category budgets to help user get started quickly
            val defaultBudgets = listOf(
                CategoryBudget("Food", income * 0.25),
                CategoryBudget("Transport", income * 0.12),
                CategoryBudget("Shopping", income * 0.15),
                CategoryBudget("Bills", income * 0.20)
            )
            defaultBudgets.forEach { budget ->
                repository.insertBudget(budget)
            }

            _currentScreen.value = "dashboard"
        }
    }

    fun updateProfile(name: String, income: Double) {
        viewModelScope.launch {
            val currentPin = userProfile.value?.securityPin ?: ""
            val profile = UserProfile(
                userName = name,
                monthlyIncome = income,
                securityPin = currentPin,
                isOnboarded = true
            )
            repository.saveUserProfile(profile)
        }
    }

    // --- Expense Management ---

    fun saveExpense(amount: Double, category: String, notes: String, date: Long, paymentMethod: String) {
        viewModelScope.launch {
            val currentEditing = _editingExpense.value
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    amount = amount,
                    category = category,
                    notes = notes,
                    date = date,
                    paymentMethod = paymentMethod
                )
                repository.updateExpense(updated)
                _editingExpense.value = null
            } else {
                val newExpense = Expense(
                    amount = amount,
                    category = category,
                    notes = notes,
                    date = date,
                    paymentMethod = paymentMethod
                )
                repository.insertExpense(newExpense)
            }
            _isAddExpenseOpen.value = false
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- Budget Management ---

    fun saveBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(CategoryBudget(category, limit))
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudgetByCategory(category)
        }
    }

    // --- AI Suggestions Engine ---

    fun requestAiInsights() {
        _aiInsightsState.value = AiInsightsState.Loading
        viewModelScope.launch {
            try {
                val expenseList = expenses.value
                val budgetList = budgets.value
                val profile = userProfile.value
                val name = profile?.userName ?: "Valued Pakistani Budgeter"
                val income = profile?.monthlyIncome ?: 50000.0

                val insights = geminiService.generateSpendingInsights(
                    expenses = expenseList,
                    budgets = budgetList,
                    userName = name,
                    monthlyIncome = income
                )
                _aiInsightsState.value = AiInsightsState.Success(insights)
            } catch (e: Exception) {
                _aiInsightsState.value = AiInsightsState.Error("Failed to synthesize recommendations: ${e.localizedMessage}")
            }
        }
    }

    // --- Utility: Seed Demo Data so user can test charts instantly ---

    fun seedDemoData() {
        viewModelScope.launch {
            // Setup User Profile
            val profile = UserProfile(
                userName = "Mohammad Ali",
                monthlyIncome = 85000.0,
                securityPin = "1234",
                isOnboarded = true
            )
            repository.saveUserProfile(profile)

            // Setup Custom Budgets
            val demoBudgets = listOf(
                CategoryBudget("Food", 15000.0),
                CategoryBudget("Transport", 10000.0),
                CategoryBudget("Shopping", 12000.0),
                CategoryBudget("Bills", 18000.0),
                CategoryBudget("Mobile & Internet", 4000.0),
                CategoryBudget("Entertainment", 5000.0)
            )
            repository.clearAllBudgets()
            demoBudgets.forEach { repository.insertBudget(it) }

            // Setup Seed Expenses
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val demoExpenses = listOf(
                Expense(amount = 2500.0, category = "Food", notes = "Grocery at Metro Cash & Carry", date = now - 1 * dayMs, paymentMethod = "Bank Transfer"),
                Expense(amount = 550.0, category = "Transport", notes = "InDrive Ride to Office", date = now - 2 * dayMs, paymentMethod = "Cash"),
                Expense(amount = 4500.0, category = "Bills", notes = "K-Electric Electricity Bill", date = now - 3 * dayMs, paymentMethod = "EasyPaisa"),
                Expense(amount = 6000.0, category = "Shopping", notes = "Kurta from J. (Junaid Jamshed)", date = now - 4 * dayMs, paymentMethod = "Card"),
                Expense(amount = 1200.0, category = "Food", notes = "Biryani Dinner with Friends", date = now - 4 * dayMs, paymentMethod = "Cash"),
                Expense(amount = 1500.0, category = "Mobile & Internet", notes = "Zong Monthly Super Card", date = now - 5 * dayMs, paymentMethod = "JazzCash"),
                Expense(amount = 350.0, category = "Transport", notes = "Rickshaw Fare", date = now - 6 * dayMs, paymentMethod = "Cash"),
                Expense(amount = 2800.0, category = "Healthcare", notes = "Medicines from Fazal Din Pharma", date = now - 7 * dayMs, paymentMethod = "Card"),
                Expense(amount = 900.0, category = "Entertainment", notes = "Chai and Paratha at Quetta Hotel", date = now - 10 * dayMs, paymentMethod = "Cash")
            )
            repository.clearAllExpenses()
            demoExpenses.forEach { repository.insertExpense(it) }

            _currentScreen.value = "dashboard"
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllExpenses()
            repository.clearAllBudgets()
            val cleanProfile = UserProfile(
                userName = "",
                monthlyIncome = 0.0,
                securityPin = "",
                isOnboarded = false
            )
            repository.saveUserProfile(cleanProfile)
            _aiInsightsState.value = AiInsightsState.Idle
            _currentScreen.value = "onboarding"
        }
    }
}
