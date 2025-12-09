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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ============================================================================
// FIX: Proper Flow Collection in Compose
// ============================================================================
//
// SOLUTIONS:
// 1. Create flows OUTSIDE composables (in ViewModel/Repository)
// 2. Use collectAsState() for simple state mapping
// 3. Use remember { flow } if you must create in composable
// ============================================================================

/**
 * FIXED: Proper flow collection with remember + collectAsState
 */
@Composable
fun FlowCollectWrongFixed(
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }

    // GOOD: Create flow ONCE with remember
    // In real apps, this would come from ViewModel
    val stockFlow: Flow<List<StockPrice>> = remember {
        flow {
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
    }

    // GOOD: Use collectAsState - handles lifecycle automatically
    val prices by stockFlow.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Text field at TOP - stays visible with keyboard
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Type here (flow survives!)") },
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
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Prices keep updating while you type!",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoodColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                prices.forEach { stock ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stock.symbol, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", stock.price)}")
                        Text("+${String.format("%.2f", stock.change)}", color = GoodColor)
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
                        containerColor = GoodColor.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Try This",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoodColor
                        )
                        Text(
                            text = "Watch prices update, then type above.\n" +
                                "Prices continue smoothly - no reset!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = GoodColor.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "The Fix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoodColor
                        )
                        Text(
                            text = """// GOOD: Create flow once
val stockFlow = remember { // ✓
    flow { emit(prices) }
}

val prices by stockFlow
    .collectAsState(initial = emptyList())""",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Why this works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• remember { } keeps same flow instance\n" +
                                "• collectAsState() handles lifecycle\n" +
                                "• Production: ViewModel + collectAsStateWithLifecycle()",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
