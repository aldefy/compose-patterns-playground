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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 02: derivedStateOf Misuse
// ============================================================================
//
// THE BUG:
// Using derivedStateOf when you DON'T need it, or not using it when you DO.
//
// WHEN YOU NEED IT:
// - When you're computing a value from state that changes frequently
// - But the computed value changes less frequently than the source
// - Example: isValid computed from a long list where few items change
//
// WHEN YOU DON'T NEED IT:
// - Simple property access (state.name)
// - Computations that change as often as the source
// - Already stable values
//
// OVERHEAD OF MISUSE:
// - Extra object allocation on every read
// - No actual benefit (computation happens anyway)
// - Makes code harder to understand
// ============================================================================

/**
 * BROKEN: Missing derivedStateOf for expensive filtering
 *
 * This recalculates the filtered list on EVERY recomposition,
 * even when unrelated state changes!
 */
@Composable
fun DerivedStateMisuseBroken(
    modifier: Modifier = Modifier
) {
    var items by remember {
        mutableStateOf(List(100) { "Item $it" })
    }
    var searchQuery by remember { mutableStateOf("") }
    var unrelatedCounter by remember { mutableIntStateOf(0) }
    var filterCallCount by remember { mutableIntStateOf(0) }

    // BAD: This recalculates on EVERY recomposition!
    // Even clicking "Trigger Recomposition" will re-filter!
    val filteredItems = items.filter {
        it.contains(searchQuery, ignoreCase = true)
    }.also {
        filterCallCount++
        Log.d("DerivedState", "Filter called! Count: $filterCallCount")
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
                        text = "List Filter Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val focusManager = LocalFocusManager.current
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search items") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Text(
                        text = "Found ${filteredItems.size} of ${items.size} items",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // This is the key metric!
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = BadColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Filter called: $filterCallCount times",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BadColor
                            )
                            Text(
                                text = "Unrelated counter: $unrelatedCounter",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // This SHOULD trigger filter (items changed)
                                items = items + "New Item ${items.size}"
                            }
                        ) {
                            Text("Add Item")
                        }

                        Button(
                            onClick = {
                                // This should NOT re-filter, but it does!
                                unrelatedCounter++
                            }
                        ) {
                            Text("Trigger Recomposition")
                        }
                    }

                    Button(
                        onClick = {
                            filterCallCount = 0
                            unrelatedCounter = 0
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Counters")
                    }
                }
            }
        }

        // Bug Explanation
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
                        text = "The Bug",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BadColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
Click "Trigger Recomposition" multiple times.

Watch the filter count increase even though:
- Items didn't change
- Search query didn't change

The filter runs on EVERY recomposition!
With 1000 items, this wastes CPU cycles.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
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

// Even unrelated state changes trigger re-filter
var unrelatedCounter by remember { ... }
unrelatedCounter++ // <- Triggers filter!
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

