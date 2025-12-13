package com.example.patterns.exercises.ex03_antipatterns.ap12_effects_in_transition

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
import com.example.patterns.core.state.TransitionResult
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// FIX: Return Effects as Data (TransitionResult Pattern)
// ============================================================================
//
// THE SOLUTION:
// Transitions are PURE functions that return:
// 1. The new state
// 2. A list of effects to execute
//
// Effects are just DATA describing what should happen.
// A separate effect handler executes them.
//
// Benefits:
// - Transitions are pure and testable
// - Effects can be inspected before execution
// - Easy to retry, rollback, or reorder
// - Clear separation of concerns
// ============================================================================

// Effects as sealed interface - just DATA
sealed interface PageEffect {
    data object LoadFromApi : PageEffect
    data class TrackAnalytics(val event: String) : PageEffect
    data class LogMessage(val message: String) : PageEffect
}

// Events that can occur
sealed interface PageEvent {
    data object LoadRequested : PageEvent
    data class LoadSucceeded(val data: String) : PageEvent
    data class LoadFailed(val error: String) : PageEvent
    data object Reset : PageEvent
}

/**
 * FIXED: Pure transition function
 *
 * This function has NO side effects - it just returns data!
 */
fun pageTransition(
    state: PageState,
    event: PageEvent
): TransitionResult<PageState, PageEffect> {
    return when (event) {
        is PageEvent.LoadRequested -> {
            // PURE: Return new state + effects to execute
            TransitionResult(
                newState = PageState.Loading,
                effects = listOf(
                    PageEffect.LogMessage("Starting load..."),
                    PageEffect.LoadFromApi,
                    PageEffect.TrackAnalytics("load_started")
                )
            )
        }

        is PageEvent.LoadSucceeded -> {
            TransitionResult(
                newState = PageState.Loaded(event.data),
                effects = listOf(
                    PageEffect.LogMessage("Load succeeded"),
                    PageEffect.TrackAnalytics("load_success")
                )
            )
        }

        is PageEvent.LoadFailed -> {
            TransitionResult(
                newState = PageState.Error(event.error),
                effects = listOf(
                    PageEffect.LogMessage("Load failed: ${event.error}"),
                    PageEffect.TrackAnalytics("load_error")
                )
            )
        }

        is PageEvent.Reset -> {
            TransitionResult(PageState.Idle)
        }
    }
}

/**
 * State machine that uses pure transitions
 */
class FixedStateMachine {
    var state: PageState by mutableStateOf(PageState.Idle)
        private set

    var pendingEffects = mutableListOf<String>()
    var executedEffects = mutableListOf<String>()

    fun processEvent(event: PageEvent): List<PageEffect> {
        // 1. Get transition result (PURE!)
        val result = pageTransition(state, event)

        // 2. Update state
        state = result.newState

        // 3. Return effects for separate handling
        pendingEffects.clear()
        pendingEffects.addAll(result.effects.map { it::class.simpleName ?: "Unknown" })

        return result.effects
    }

    suspend fun executeEffect(effect: PageEffect, onEvent: (PageEvent) -> Unit) {
        executedEffects.add(effect::class.simpleName ?: "Unknown")

        when (effect) {
            is PageEffect.LoadFromApi -> {
                delay(1000) // Simulate network
                if (Math.random() > 0.5) {
                    onEvent(PageEvent.LoadSucceeded("Data from server"))
                } else {
                    onEvent(PageEvent.LoadFailed("Network error"))
                }
            }
            is PageEffect.TrackAnalytics -> {
                // Would send to analytics service
            }
            is PageEffect.LogMessage -> {
                // Would log
            }
        }
    }

    fun reset() {
        state = PageState.Idle
        pendingEffects.clear()
        executedEffects.clear()
    }
}

/**
 * FIXED: Effects returned as data, executed separately
 */
@Composable
fun EffectsInTransitionFixed(
    modifier: Modifier = Modifier
) {
    val stateMachine = remember { FixedStateMachine() }
    val scope = rememberCoroutineScope()
    var loadAttempts by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Effects as Data",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
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
                    text = "Pending effects:",
                    style = MaterialTheme.typography.labelMedium
                )
                stateMachine.pendingEffects.forEach { effect ->
                    Text("• $effect", style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    text = "Executed effects:",
                    style = MaterialTheme.typography.labelMedium
                )
                stateMachine.executedEffects.forEach { effect ->
                    Text("• $effect", style = MaterialTheme.typography.bodySmall, color = GoodColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    loadAttempts++
                    // 1. Process event (pure!)
                    val effects = stateMachine.processEvent(PageEvent.LoadRequested)

                    // 2. Execute effects separately
                    scope.launch {
                        effects.forEach { effect ->
                            stateMachine.executeEffect(effect) { resultEvent ->
                                // Process result events
                                stateMachine.processEvent(resultEvent)
                            }
                        }
                    }
                }) {
                    Text("Load Data")
                }

                Button(onClick = {
                    stateMachine.processEvent(PageEvent.Reset)
                    stateMachine.reset()
                }) {
                    Text("Reset")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.05f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "The Fix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoodColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """// GOOD: Pure transition function
fun transition(
    state: State,
    event: Event
): TransitionResult<State, Effect> {
    return TransitionResult(
        newState = Loading,
        effects = listOf(  // ✓ Effects as DATA
            Effect.FetchApi,
            Effect.TrackAnalytics
        )
    )
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
                    text = "Why is this better?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """transition() is PURE:
• No side effects
• Same input → same output
• Returns effects as DATA

Benefits:
• Easy to test (no mocking!)
• Can inspect effects before execution
• Can retry specific effects
• Clear separation of concerns""",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
