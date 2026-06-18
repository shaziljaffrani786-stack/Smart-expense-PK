package com.example.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Define modern category colors (Pastel-Luxe Harmonized Palette)
val FoodColor = Color(0xFFE07A5F)         // Terracotta Warm Red
val TransportColor = Color(0xFF5390D9)    // Sophisticated Slate Blue
val ShoppingColor = Color(0xFFE9C46A)     // Soft Mustard Gold
val EducationColor = Color(0xFF9D4EDD)    // Premium Amethyst Purple
val EntertainmentColor = Color(0xFFF4A261)// Warm Amber Peach
val HealthcareColor = Color(0xFF2A9D8F)   // Eucalyptus Teal Green
val BillsColor = Color(0xFF1A759F)        // Petrol Blue
val MobileColor = Color(0xFF4A4E69)       // Dusty Indigo
val TravelColor = Color(0xFFC77DFF)       // Light Lilac
val OtherColor = Color(0xFF90A4AE)        // Steel Mist Gray

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food" -> FoodColor
        "Transport" -> TransportColor
        "Shopping" -> ShoppingColor
        "Education" -> EducationColor
        "Entertainment" -> EntertainmentColor
        "Healthcare" -> HealthcareColor
        "Bills" -> BillsColor
        "Mobile & Internet" -> MobileColor
        "Travel" -> TravelColor
        else -> OtherColor
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedPieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0.0) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No chart data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    // Pie slices percentage
    val slices = data.entries.map {
        PieSlice(
            name = it.key,
            value = it.value,
            percentage = (it.value / total).toFloat(),
            color = getCategoryColor(it.key)
        )
    }.filter { it.percentage > 0f }

    // Animation progress
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = slice.percentage * 360f * animationProgress.value
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        size = Size(size.width, size.height),
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Total Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Rs ${String.format("%,.0f", total)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid-based category legend
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            slices.forEach { slice ->
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${slice.name}: ${String.format("%.0f", slice.percentage * 100)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

data class PieSlice(
    val name: String,
    val value: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun SpendBarChart(
    weeklySpend: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (weeklySpend.isEmpty() || weeklySpend.all { it.second == 0.0 }) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No daily trend records found.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val maxVal = weeklySpend.maxOfOrNull { it.second } ?: 1.0
    val maxLimit = if (maxVal == 0.0) 1.0 else maxVal

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(weeklySpend) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(modifier = modifier) {
        Text(
            text = "Daily Outflow (Last 7 Days)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklySpend.forEach { (day, amount) ->
                val ratio = (amount / maxLimit).toFloat()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Hover-style budget text above bar
                    if (amount > 0) {
                        Text(
                            text = if (amount >= 1000) "${String.format("%.1fk", amount / 1000.0)}" else "${amount.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Bar drawn inside box
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.75f)
                            .width(18.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(ratio * animationProgress.value)
                                .background(
                                    color = if (ratio > 0.8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Transport" -> Icons.Default.DirectionsCar
        "Shopping" -> Icons.Default.ShoppingBag
        "Education" -> Icons.Default.School
        "Entertainment" -> Icons.Default.LocalPlay
        "Healthcare" -> Icons.Default.MedicalServices
        "Bills" -> Icons.Default.ReceiptLong
        "Mobile & Internet" -> Icons.Default.Wifi
        "Travel" -> Icons.Default.Flight
        else -> Icons.Default.Category
    }
}
