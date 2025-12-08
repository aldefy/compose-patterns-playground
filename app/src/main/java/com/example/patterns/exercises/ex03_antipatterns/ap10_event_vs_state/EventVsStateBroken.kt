package com.example.patterns.exercises.ex03_antipatterns.ap10_event_vs_state

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 10: Confusing Events with State
// ============================================================================
//
// THE BUG:
// Using state to represent one-time events. This leads to:
// - Events being processed multiple times
// - Needing to manually "reset" state after handling
// - Race conditions
//
// SYMPTOMS:
// - Snackbar shown multiple times for same error
// - Navigation triggered repeatedly
// - "Already handled" bugs
// ============================================================================

/**
 * BROKEN: Using state for one-time events
 */
@Composable
fun EventVsStateBroken(
    modifier: Modifier = Modifier
) {
    // BAD: Using state for a one-time event
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var clickCount by remember { mutableIntStateOf(0) }
    var handledCount by remember { mutableIntStateOf(0) }

    // BAD: This runs EVERY time showError is true during recomposition
    // If something else causes recomposition, the error shows again!
    LaunchedEffect(showError) {
        if (showError) {
            handledCount++
            // "Handle" the error
            kotlinx.coroutines.delay(100)
            // Oops, we need to manually reset!
            // But what if recomposition happens before we reset?
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Events as State",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Click count: $clickCount")
                Text("Event handled: $handledCount times")

                if (showError) {
                    Text(
                        text = errorMessage ?: "Error!",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    clickCount++
                    showError = true
                    errorMessage = "Error #$clickCount"
                }) {
                    Text("Trigger Error")
                }

                Button(onClick = {
                    showError = false
                    errorMessage = null
                }) {
                    Text("Clear Error (Manual Reset)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Problems:\n" +
                            "• Must manually reset state\n" +
                            "• Can be handled multiple times\n" +
                            "• Survives configuration changes (may re-show)",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        State is WHAT THE UI IS.
                        Events are WHAT HAPPENED.

                        "An error should be shown" = State ✓
                        "An error just occurred" = Event ✗

                        Using state for events causes:
                        • Re-triggering on recomposition
                        • Manual reset needed
                        • Race conditions during reset
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
