package com.example.patterns.exercises.ex03_antipatterns.ap06_flow_collect_wrong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

// ============================================================================
// ANTI-PATTERN 06: Wrong Way to Collect Flows
// ============================================================================
//
// THE BUG:
// Collecting flows incorrectly in Compose:
// 1. Creating flow inside composable (new instance each recomposition)
// 2. Using wrong LaunchedEffect key
//
// SYMPTOMS:
// - Flow restarts on every recomposition
// - Missing emissions / data resets
// - Duplicate processing
// ============================================================================

data class StockPrice(val symbol: String, val price: Double, val change: Double)

/**
 * BROKEN: Creating flow inside composable - restarts on every recomposition
 */
@Composable
fun FlowCollectWrongBroken(
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    var prices by remember { mutableStateOf<List<StockPrice>>(emptyList()) }
    var updateCount by remember { mutableStateOf(0) }

    // BAD: Creating flow INSIDE the composable!
    // Every recomposition creates a NEW flow instance
    val stockFlow = flow {
        var tick = 0
        while (true) {
            val newPrices = listOf(
                StockPrice("AAPL", 178.0 + (tick % 10) * 0.5, (tick % 10) * 0.5),
                StockPrice("GOOGL", 141.0 + (tick % 8) * 0.3, (tick % 8) * 0.3),
                StockPrice("MSFT", 378.0 + (tick % 12) * 0.4, (tick % 12) * 0.4),
            )
            emit(newPrices)
            tick++
            delay(1000)
        }
    }

    // BAD: This collects a NEW flow each time messageText changes!
    LaunchedEffect(messageText) {
        stockFlow.collect { newPrices ->
            prices = newPrices
            updateCount++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Text field at TOP - stays visible with keyboard
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Type here to break the flow") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { messageText = "" }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Clear")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Stock prices display - key info visible with keyboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Updates: $updateCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BadColor
                    )
                    Text(
                        text = "Resets on typing!",
                        style = MaterialTheme.typography.labelMedium,
                        color = BadColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                prices.forEach { stock ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stock.symbol, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", stock.price)}")
                        Text("+${String.format("%.2f", stock.change)}", color = BadColor)
                    }
                }

                if (prices.isEmpty()) {
                    Text("Loading...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable content below
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BadColor.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Try This",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BadColor
                        )
                        Text(
                            text = "Watch prices update, then type above.\n" +
                                "Updates counter resets with each keystroke!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BadColor.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "The Bug",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BadColor
                        )
                        Text(
                            text = """// BAD: Flow created inside composable!
val stockFlow = flow { // ❌ NEW instance
    emit(prices)
}

LaunchedEffect(messageText) { // ❌
    stockFlow.collect { ... }
}""",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
