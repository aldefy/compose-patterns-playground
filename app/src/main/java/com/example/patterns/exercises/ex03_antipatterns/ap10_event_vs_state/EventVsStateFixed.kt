package com.example.patterns.exercises.ex03_antipatterns.ap10_event_vs_state

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ============================================================================
// FIX: Use Channels for Events, State for UI
// ============================================================================
//
// SOLUTIONS:
// 1. Use SharedFlow/Channel for one-time events
// 2. Consume events in LaunchedEffect with flow collection
// 3. Events are automatically "consumed" when collected
// 4. No manual reset needed
// ============================================================================

/**
 * FIXED: Using Channel for one-time events
 */
@Composable
fun EventVsStateFixed(
    modifier: Modifier = Modifier
) {
    // GOOD: Channel for one-time events - each event consumed exactly once
    val eventChannel = remember { Channel<String>(Channel.BUFFERED) }
    var clickCount by remember { mutableIntStateOf(0) }
    var handledCount by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // GOOD: Collect from channel - each event processed exactly once
    LaunchedEffect(Unit) {
        eventChannel.receiveAsFlow().collect { message ->
            handledCount++
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Events via Channel",
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
                    Text("Click count: $clickCount")
                    Text("Events handled: $handledCount times")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        clickCount++
                        scope.launch {
                            eventChannel.send("Error #$clickCount occurred!")
                        }
                    }) {
                        Text("Trigger Error")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Benefits:\n" +
                                "• Each event handled exactly once\n" +
                                "• No manual reset needed\n" +
                                "• Survives recomposition safely",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoodColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Why Channels/Flows for Events?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
                            Channel = One-time delivery
                            • Event sent once
                            • Consumed once
                            • Gone after handling

                            vs State:
                            • Always present
                            • Visible every recomposition
                            • Needs manual clearing

                            Use State for: UI appearance
                            Use Events for: One-time actions (snackbar, navigation)
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Alternative pattern using a consumed event wrapper
 */
sealed interface UiEvent {
    data class ShowError(val message: String) : UiEvent
    data object NavigateBack : UiEvent
}

/**
 * In ViewModel, you would use:
 *
 * private val _events = Channel<UiEvent>(Channel.BUFFERED)
 * val events = _events.receiveAsFlow()
 *
 * fun onError() {
 *     viewModelScope.launch {
 *         _events.send(UiEvent.ShowError("Something went wrong"))
 *     }
 * }
 *
 * In Composable:
 *
 * LaunchedEffect(Unit) {
 *     viewModel.events.collect { event ->
 *         when (event) {
 *             is UiEvent.ShowError -> snackbar.showSnackbar(event.message)
 *             is UiEvent.NavigateBack -> navController.popBackStack()
 *         }
 *     }
 * }
 */
