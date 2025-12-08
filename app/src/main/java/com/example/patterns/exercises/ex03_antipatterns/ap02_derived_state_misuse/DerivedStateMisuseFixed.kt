package com.example.patterns.exercises.ex03_antipatterns.ap02_derived_state_misuse

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
 * Type in the "Notes" field - filter count stays the same!
 * derivedStateOf only recalculates when items or searchQuery change.
 */
@Composable
fun DerivedStateMisuseFixed(
    modifier: Modifier = Modifier
) {
    var items by remember {
        mutableStateOf(List(100) { "Item $it" })
    }
    var searchQuery by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }  // Unrelated field

    // Track recomposition count (non-state holder to avoid infinite loops)
    val recomposeCount = remember { object { var count = 0 } }
    recomposeCount.count++

    // Track filter calls (non-state holder)
    val filterCallCount = remember { object { var count = 0 } }

    // GOOD: derivedStateOf prevents recalculation when notes changes!
    val filteredItems by remember {
        derivedStateOf {
            filterCallCount.count++
            Log.d("DerivedState", "Filter called! Count: ${filterCallCount.count}")
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
                        text = "Shopping List with Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val focusManager = LocalFocusManager.current

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search items") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.clearFocus() }
                        )
                    )

                    // Unrelated notes field - typing here should NOT re-filter!
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (type here - filter won't run!)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
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
                                text = "Recompositions: ${recomposeCount.count}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoodColor
                            )
                            Text(
                                text = "Filter calls: ${filterCallCount.count}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoodColor
                            )
                            Text(
                                text = "Notes length: ${notes.length} chars",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
Type anything in the "Notes" field.

Watch the recomposition count increase, but filter calls stay the same!

derivedStateOf only recalculates when:
• items changes (if you were to add items)
• searchQuery changes (typing in search)

NOT when notes changes - that's the fix!
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
