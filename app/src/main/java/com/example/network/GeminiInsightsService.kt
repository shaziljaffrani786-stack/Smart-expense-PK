package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.CategoryBudget
import com.example.data.Expense
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request/Response Models ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiResponsePart(
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class GeminiResponseContent(
    @Json(name = "parts") val parts: List<GeminiResponsePart>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiResponseContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

// --- Retrofit Endpoint Definition ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun getInsights(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Service Client Implementation ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiInsightsService {

    suspend fun generateSpendingInsights(
        expenses: List<Expense>,
        budgets: List<CategoryBudget>,
        userName: String,
        monthlyIncome: Double
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiInsightsService", "Gemini API key is not configured.")
            return "Please configure your Gemini API Key in the AI Studio Secrets panel to get tailored local suggestions."
        }

        // Format expense history for prompt
        val expensesString = if (expenses.isEmpty()) {
            "No expenses recorded yet."
        } else {
            expenses.joinToString("\n") {
                "- Category: ${it.category}, Amount: Rs ${String.format("%,.0f", it.amount)}, Payment Method: ${it.paymentMethod}, Note: ${it.notes.ifEmpty { "None" }}"
            }
        }

        // Format budgets for prompt
        val budgetsString = if (budgets.isEmpty()) {
            "No budget limits set yet."
        } else {
            budgets.joinToString("\n") {
                "- Category: ${it.category}, Budget Limit: Rs ${String.format("%,.0f", it.amountLimit)}"
            }
        }

        val prompt = """
            You are a highly professional, friendly, and expert Pakistani financial advisor AI called "Smart Expense PK Advisor".
            Your goal is to analyze the user's spending data and provide practical, localized, actionable advice to help them save money.
            
            User's Name: $userName
            User's Monthly Income: Rs ${String.format("%,.0f", monthlyIncome)}
            
            --- USER BUDGETS ---
            $budgetsString
            
            --- USER CURRENT EXPENSES ---
            $expensesString
            
            Based on the details above, write a concise, premium structured advice report.
            Ensure you:
            1. Use localized PK references (e.g., offer suggestions about choosing cost-effective Metro/Orange line over ride-hailing services, choosing local utility saving habits, mentioning local shopping, saving on mobile plans/super cards, e.g., Jazz/Zong monthly packages, and avoiding high food delivery costs via foodpanda).
            2. Identify any category where they are over-spending or near-maximum budget limit, citing exact PKR values.
            3. Provide 3 specific bullet points on how they can save at least PKR 3,000 to PKR 10,000 this month.
            4. Keep the tone encouraging, professional, friendly, and empowering for a Pakistani budgeter. 
            5. Structure it beautifully with clear bold headings and high-contrast spacing. Keep it to around 150-250 words total. Mention "Rs" for PKR values, example: Rs 1,500.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        return try {
            val response = GeminiClient.apiService.getInsights(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No insights available right now. Please add more expenses to get localized recommendations."
        } catch (e: Exception) {
            Log.e("GeminiInsightsService", "Error calling Gemini API", e)
            "Could not connect to Gemini AI Server. Please check your internet connection or verify your API key is correctly configured. Error: ${e.localizedMessage}"
        }
    }
}
