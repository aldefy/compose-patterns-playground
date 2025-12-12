package com.example.patterns.exercises.ex03_antipatterns.ap10_event_vs_state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
// Using state to represent one-time events. StateFlow/State retains the last
// value, so if recomposition happens while the "event" state is still set,
// the event gets re-handled.
//
// Reference: Manuel Vivo (Google) - "ViewModel Events" anti-pattern
// https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95
//
// SYMPTOMS:
// - Snackbar shown multiple times for same error
// - Navigation triggered repeatedly on config change
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
    var errorEvent by remember { mutableStateOf<String?>(null) }
    var recomposeCounter by remember { mutableIntStateOf(0) }
    var handledCount by remember { mutableIntStateOf(0) }

    // BAD: This pattern re-handles the event every time recomposeCounter changes!
    // The errorEvent state persists, so checking it again triggers duplicate handling.
    LaunchedEffect(recomposeCounter) {
        if (errorEvent != null) {
            handledCount++
            // Simulates "handling" the error (e.g., showing snackbar)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Event as State Bug",
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
                Text(
                    text = "Event handled: $handledCount times",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (handledCount > 1) BadColor else MaterialTheme.colorScheme.onSurface
                )

                if (errorEvent != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorEvent!!,
                        color = BadColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        errorEvent = "Save failed!"
                    }) {
                        Text("Trigger Error")
                    }

                    Button(onClick = {
                        recomposeCounter++
                    }) {
                        Text("Recompose ($recomposeCounter)")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Click 'Trigger Error' → handled once\n" +
                            "2. Click 'Recompose' → handled AGAIN! (bug)",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.05f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "The Bug",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BadColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """// BAD: State for one-time event
var errorEvent by remember {
    mutableStateOf<String?>(null)
}

// Re-triggers on every state read!
snapshotFlow { errorEvent }
    .collect { error ->
        if (error != null) {
            showSnackbar(error) // ❌ Shows again!
        }
    }""",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why it happens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """State persists its value. When recomposition happens:

• errorEvent is still "Save failed!"
• snapshotFlow emits again
• Handler runs again → duplicate snackbar!

Same bug occurs on:
• Screen rotation (config change)
• Any state change causing recomposition""",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
