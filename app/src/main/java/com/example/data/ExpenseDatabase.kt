package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String, // e.g. Food, Transport, Shopping, Education, Entertainment, Healthcare, Bills, Mobile & Internet, Travel, Other
    val notes: String,
    val date: Long, // timestamp
    val paymentMethod: String = "Cash" // Cash, Bank Transfer, EasyPaisa, JazzCash, Card
)

@Entity(tableName = "budgets")
data class CategoryBudget(
    @PrimaryKey val category: String, // Category name, or "Total" for overall monthly budget
    val amountLimit: Double
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val userName: String,
    val monthlyIncome: Double,
    val securityPin: String = "", // Optional local PIN
    val isOnboarded: Boolean = false
)

// --- DAO ---

@Dao
interface ExpenseDao {
    // Expense Queries
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    // Budget Queries
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<CategoryBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CategoryBudget)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteBudgetByCategory(category: String)

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()

    // User Profile Queries
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

// --- Database Configuration ---

@Database(entities = [Expense::class, CategoryBudget::class, UserProfile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_expense_pk_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
