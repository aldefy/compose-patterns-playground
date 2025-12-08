package com.example.patterns.exercises.ex03_antipatterns.ap02_derived_state_misuse

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Use derivedStateOf correctly
// ============================================================================
//
// WHEN TO USE derivedStateOf:
// 1. You have state that changes frequently (e.g., every keystroke)
// 2. You compute a value from that state
// 3. The computed value changes LESS frequently than the source
//
// Example: Scrolling a list of 1000 items
// - Scroll position changes every frame (60+ times/second)
// - "Show back-to-top button" only changes when crossing a threshold
// - derivedStateOf prevents recomposition on every scroll
//
// WHEN NOT TO USE derivedStateOf:
// - Simple property access
// - Computations that change as often as the source
// - One-time computations (use remember with keys instead)
// ============================================================================

/**
 * FIXED: Use derivedStateOf for expensive filtering
 *
 * Tap quantity buttons - filter count stays the same!
 * derivedStateOf only recalculates when searchQuery changes.
 */
@Composable
fun DerivedStateMisuseFixed(
    modifier: Modifier = Modifier
) {
    // Grocery items to filter
    val items = remember {
        listOf("Apple", "Banana", "Orange", "Milk", "Bread", "Eggs", "Cheese", "Butter", "Yogurt", "Chicken")
    }
    var searchQuery by remember { mutableStateOf("") }

    // Quantity selector - changing this should NOT re-filter!
    var quantity by remember { mutableIntStateOf(1) }

    // Track filter calls - use state so UI updates
    var filterCallCount by remember { mutableIntStateOf(0) }

    // GOOD: derivedStateOf only recalculates when searchQuery changes!
    // Quantity changes do NOT trigger re-filtering
    val filteredItems by remember {
        derivedStateOf {
            filterCallCount++
            Log.d("DerivedState", "Filter called! Count: $filterCallCount")
            items.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "derivedStateOf Fixed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoodColor
            )
        }

        // Instructions FIRST
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GoodColor.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Try This",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
1. Tap + or - to change quantity
2. Watch "Filter ran X times" - it stays the same!

derivedStateOf caches the result. No wasted work!
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Interactive demo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GoodColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Grocery List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search groceries") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Show filtered items
                    Text(
                        text = "Results: ${filteredItems.joinToString(", ").ifEmpty { "(no matches)" }}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Quantity selector - unrelated to search!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quantity: $quantity",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if (quantity > 1) quantity-- }) {
                                Text("-")
                            }
                            Button(onClick = { quantity++ }) {
                                Text("+")
                            }
                        }
                    }

                    // This is the key metric!
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = GoodColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Filter ran $filterCallCount times",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoodColor
                            )
                        }
                    }
                }
            }
        }

        // Code snippet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Fixed Code:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
// GOOD: Only recalculates when items/query change!
val filteredItems by remember {
    derivedStateOf {
        items.filter {
            it.contains(searchQuery)
        }
    }
}

// Typing in notes causes recomposition
var notes by remember { mutableStateOf("") }
notes = "a"  // Recomposition, but NO filter call!
notes = "ab" // Recomposition, but NO filter call!
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Rule of thumb
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "When to Use derivedStateOf",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
Use derivedStateOf when:
- Computing expensive values from state
- The result changes LESS often than inputs
- You have unrelated state that causes recomposition

Don't use for simple property access or string concat.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
