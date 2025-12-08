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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalFocusManager
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
 * Type in the "Notes" field and watch the filter count increase!
 * The filter runs on every keystroke even though the search didn't change.
 */
@Composable
fun DerivedStateMisuseBroken(
    modifier: Modifier = Modifier
) {
    var items by remember {
        mutableStateOf(List(100) { "Item $it" })
    }
    var searchQuery by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }  // Unrelated field

    // Track recomposition count (this increments on every recomposition)
    val recomposeCount = remember { object { var count = 0 } }
    recomposeCount.count++

    // BAD: This recalculates on EVERY recomposition!
    // Even typing in the Notes field triggers re-filtering!
    val filteredItems = items.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Log.d("DerivedState", "Filter called! Recompose count: ${recomposeCount.count}")

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
                        label = { Text("Notes (type here to see the bug)") },
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
                            containerColor = BadColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Recompositions: ${recomposeCount.count}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BadColor
                            )
                            Text(
                                text = "Filter runs every recomposition!",
                                style = MaterialTheme.typography.bodySmall,
                                color = BadColor
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
Type anything in the "Notes" field.

Watch the filter count increase with every keystroke!

The search query didn't change, but the filter runs anyway because:
• notes changed → recomposition triggered
• filter is computed inline → runs every recomposition
• 100 items filtered on every keystroke = lag

With 1000+ items, typing would feel sluggish.
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
