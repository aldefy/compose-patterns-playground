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
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 02: derivedStateOf Misuse
// ============================================================================
//
// THE BUG:
// Not using derivedStateOf for expensive computations that should be cached.
//
// REAL-WORLD SCENARIO:
// A screen with a search filter AND other input fields (like notes).
// Every keystroke in the notes field triggers a recomposition.
// Without derivedStateOf, the filter runs on EVERY keystroke - even though
// the search query didn't change!
//
// THE SYMPTOM:
// Typing in an unrelated field feels sluggish because the expensive
// filter computation runs on every keystroke.
//
// ============================================================================

/**
 * BROKEN: Missing derivedStateOf for expensive filtering
 *
 * Tap the quantity buttons and watch the filter count increase!
 * The filter runs on every tap even though the search didn't change.
 */
@Composable
fun DerivedStateMisuseBroken(
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

    // BAD: This recalculates on EVERY recomposition!
    // Even changing quantity triggers re-filtering!
    val filteredItems = remember(searchQuery, quantity) {
        // Increment once per filter call (not per item)
        filterCallCount++
        Log.d("DerivedState", "Filter called! Count: $filterCallCount")
        items.filter { it.contains(searchQuery, ignoreCase = true) }
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
                text = "Missing derivedStateOf",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BadColor
            )
        }

        // Instructions FIRST
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Try This",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BadColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
1. Tap + or - to change quantity
2. Watch "Filter ran X times" increase!

The search didn't change, but filter runs anyway. That's wasted work!
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
                    containerColor = BadColor.copy(alpha = 0.1f)
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
                            containerColor = BadColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Filter ran $filterCallCount times",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BadColor
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
                        text = "Broken Code:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BadColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
// BAD: Recalculates on EVERY recomposition!
val filteredItems = items.filter {
    it.contains(searchQuery)
}

// Typing in notes triggers recomposition
var notes by remember { mutableStateOf("") }
notes = "a"  // Filter runs!
notes = "ab" // Filter runs again!
// ... filter runs on every keystroke
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Real world impact
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Real World Impact",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
This happens in real apps when you have:
• A search/filter on a long list
• PLUS other inputs on the same screen
• PLUS timers, animations, or live data

Every unrelated state change runs your expensive filter!
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
