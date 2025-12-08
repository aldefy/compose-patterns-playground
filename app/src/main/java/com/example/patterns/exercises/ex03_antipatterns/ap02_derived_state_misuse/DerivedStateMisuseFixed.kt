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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
 * derivedStateOf prevents recalculation when unrelated state changes.
 * The filter only runs when items or searchQuery actually change!
 */
@Composable
fun DerivedStateMisuseFixed(
    modifier: Modifier = Modifier
) {
    var items by remember {
        mutableStateOf(List(100) { "Item $it" })
    }
    var searchQuery by remember { mutableStateOf("") }
    var unrelatedCounter by remember { mutableIntStateOf(0) }
    var filterCallCount by remember { mutableIntStateOf(0) }

    // GOOD: derivedStateOf prevents recalculation when unrelatedCounter changes!
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
                            containerColor = GoodColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Filter called: $filterCallCount times",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoodColor
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
                                // This should NOT re-filter - and it doesn't!
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

        // Fix Explanation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GoodColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Why This Works",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
Click "Trigger Recomposition" multiple times.

The filter count stays the same!

derivedStateOf only recalculates when:
- items changes (Add Item)
- searchQuery changes (typing)

NOT on unrelated recompositions!
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

// Unrelated state changes don't trigger filter
var unrelatedCounter by remember { ... }
unrelatedCounter++ // <- NO filter call!
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
