package com.example.patterns.exercises.ex03_antipatterns.ap05_side_effect_in_composition

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 05: Side Effects During Composition
// ============================================================================
//
// THE BUG:
// Performing side effects (network calls, analytics, logging, DB writes)
// directly during composition. Composition can be called:
// - Multiple times
// - In any order
// - On any thread (not necessarily main)
// - And can be cancelled/restarted at any time
//
// SYMPTOMS:
// - Duplicate API calls
// - Analytics events counted multiple times
// - Race conditions
// - Inconsistent state
// ============================================================================

// Simulating a global counter that gets incremented during composition
// In real code this might be analytics, network calls, etc.
object CompositionCounter {
    var count = 0
        private set

    fun increment() {
        count++
        Log.d("SideEffectBroken", "Side effect executed! Count: $count")
    }

    fun reset() {
        count = 0
    }
}

/**
 * BROKEN: Performing side effects during composition
 */
@Composable
fun SideEffectInCompositionBroken(
    modifier: Modifier = Modifier
) {
    var recomposeCounter by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }

    // BAD: This runs EVERY time this composable is recomposed!
    // Composition can happen many times, in any order
    CompositionCounter.increment()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Side Effects in Composition",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Side effect called: ${CompositionCounter.count} times",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = BadColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Try triggering recompositions:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { recomposeCounter++ }) {
                    Text("Trigger Recomposition ($recomposeCounter)")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Typing also triggers recomposition
                val focusManager = LocalFocusManager.current
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Type here (each keystroke = recomposition)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(onClick = { CompositionCounter.reset() }) {
                    Text("Reset Counter")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What's wrong?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        The side effect runs during composition.

                        Problems:
                        • Runs on EVERY recomposition
                        • May run multiple times
                        • May be cancelled mid-execution
                        • No control over when it runs

                        Real examples of this bug:
                        • Analytics tracked multiple times
                        • API calls made repeatedly
                        • Database writes corrupted
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
