package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allBudgets: Flow<List<CategoryBudget>> = expenseDao.getAllBudgets()
    val userProfile: Flow<UserProfile?> = expenseDao.getUserProfile()

    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun clearAllExpenses() = expenseDao.clearAllExpenses()

    suspend fun insertBudget(budget: CategoryBudget) = expenseDao.insertBudget(budget)

    suspend fun deleteBudgetByCategory(category: String) = expenseDao.deleteBudgetByCategory(category)

    suspend fun clearAllBudgets() = expenseDao.clearAllBudgets()

    suspend fun saveUserProfile(profile: UserProfile) = expenseDao.insertUserProfile(profile)
}
