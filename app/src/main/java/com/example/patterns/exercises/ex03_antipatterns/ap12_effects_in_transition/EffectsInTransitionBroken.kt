package com.example.patterns.exercises.ex03_antipatterns.ap12_effects_in_transition

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// ANTI-PATTERN 12: Side Effects in State Transitions
// ============================================================================
//
// THE BUG:
// Executing side effects directly during state transitions:
// - Makes transitions impure and hard to test
// - Side effects can't be easily inspected
// - Race conditions and ordering issues
// - Retry/rollback becomes impossible
//
// This is the core insight of the state machine pattern:
// Transitions should return EFFECTS, not execute them!
// ============================================================================

sealed interface PageState {
    data object Idle : PageState
    data object Loading : PageState
    data class Loaded(val data: String) : PageState
    data class Error(val message: String) : PageState
}

/**
 * BROKEN: Executing effects inside the transition function
 *
 * This function is impure - it has side effects!
 */
class BrokenStateMachine {
    var state: PageState by mutableStateOf(PageState.Idle)
        private set

    var effectsExecuted = mutableListOf<String>()

    // BAD: This function is IMPURE - it executes side effects!
    suspend fun load() {
        // Setting state - this is fine
        state = PageState.Loading

        // BAD: Executing effect directly in transition!
        Log.d("BrokenSM", "Making API call...")
        effectsExecuted.add("API Call")

        // BAD: Network call in transition
        delay(1000) // Simulating network

        // BAD: Analytics in transition
        Log.d("BrokenSM", "Tracking analytics...")
        effectsExecuted.add("Analytics")

        // Randomly succeed or fail
        if (Math.random() > 0.5) {
            state = PageState.Loaded("Data from server")
            effectsExecuted.add("Success logged")
        } else {
            state = PageState.Error("Network error")
            effectsExecuted.add("Error logged")
        }
    }

    fun reset() {
        state = PageState.Idle
        effectsExecuted.clear()
    }
}

/**
 * BROKEN: Side effects executed during transitions
 */
@Composable
fun EffectsInTransitionBroken(
    modifier: Modifier = Modifier
) {
    val stateMachine = remember { BrokenStateMachine() }
    val scope = rememberCoroutineScope()
    var loadAttempts by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Effects in Transitions",
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
                    text = "State: ${stateMachine.state::class.simpleName}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Load attempts: $loadAttempts",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Effects executed:",
                    style = MaterialTheme.typography.labelMedium
                )
                stateMachine.effectsExecuted.forEach { effect ->
                    Text("• $effect", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    loadAttempts++
                    scope.launch { stateMachine.load() }
                }) {
                    Text("Load Data")
                }

                Button(onClick = { stateMachine.reset() }) {
                    Text("Reset")
                }
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
                    text = """// BAD: Impure transition function
suspend fun load() {
    state = Loading

    // ❌ Side effects in transition!
    api.fetchData()      // Network
    analytics.track()    // Analytics
    Log.d("tag", "...")  // Logging

    state = Loaded(data)
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
                    text = "Why is this bad?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """The load() function is IMPURE - it executes
side effects directly!

Problems:
• Can't test without mocking
• Can't inspect what effects will run
• Can't retry individual effects
• Hard to debug ordering issues

SOLUTION: Return effects as DATA""",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
