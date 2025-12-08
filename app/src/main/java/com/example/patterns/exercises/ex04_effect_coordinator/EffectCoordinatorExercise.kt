package com.example.patterns.exercises.ex04_effect_coordinator

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.patterns.core.effects.CommonEffect
import com.example.patterns.core.state.TransitionResult
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.launch

/**
 * Exercise 04: Effect Coordinator Pattern
 *
 * This exercise demonstrates how to coordinate effect execution
 * in a state machine architecture.
 *
 * KEY CONCEPTS:
 * 1. Effects are DATA returned from transitions
 * 2. Effect Coordinator EXECUTES those effects
 * 3. Results are fed back as events to the state machine
 * 4. UI only observes state, never executes effects directly
 *
 * ARCHITECTURE FLOW:
 * UI Event → ViewModel.onEvent() → transition() → [NewState, Effects]
 *                                       ↓
 *                              Coordinator.execute(effects)
 *                                       ↓
 *                              Results → Events → transition() → ...
 */

// State for the demo
sealed interface CoordinatorDemoState {
    data object Idle : CoordinatorDemoState
    data object Loading : CoordinatorDemoState
    data class Loaded(val data: String) : CoordinatorDemoState
    data class Saving(val data: String) : CoordinatorDemoState
    data class Saved(val message: String) : CoordinatorDemoState
    data class Error(val error: String) : CoordinatorDemoState
}

// Events for the demo
sealed interface CoordinatorDemoEvent {
    data object LoadClicked : CoordinatorDemoEvent
    data class SaveClicked(val data: String) : CoordinatorDemoEvent
    data class DataLoaded(val data: String) : CoordinatorDemoEvent
    data class DataSaved(val success: Boolean) : CoordinatorDemoEvent
    data class ErrorOccurred(val error: String) : CoordinatorDemoEvent
    data object Reset : CoordinatorDemoEvent
}

// Effects for the demo
sealed interface CoordinatorDemoEffect {
    data object LoadData : CoordinatorDemoEffect
    data class SaveData(val data: String) : CoordinatorDemoEffect
    data class ShowSnackbar(val message: String) : CoordinatorDemoEffect
    data class TrackAnalytics(val event: String) : CoordinatorDemoEffect
}

// Pure transition function
fun coordinatorDemoTransition(
    state: CoordinatorDemoState,
    event: CoordinatorDemoEvent
): TransitionResult<CoordinatorDemoState, CoordinatorDemoEffect> {
    return when (event) {
        is CoordinatorDemoEvent.LoadClicked -> TransitionResult(
            newState = CoordinatorDemoState.Loading,
            effects = listOf(
                CoordinatorDemoEffect.LoadData,
                CoordinatorDemoEffect.TrackAnalytics("load_started")
            )
        )

        is CoordinatorDemoEvent.SaveClicked -> TransitionResult(
            newState = CoordinatorDemoState.Saving(event.data),
            effects = listOf(
                CoordinatorDemoEffect.SaveData(event.data),
                CoordinatorDemoEffect.TrackAnalytics("save_started")
            )
        )

        is CoordinatorDemoEvent.DataLoaded -> TransitionResult(
            newState = CoordinatorDemoState.Loaded(event.data),
            effects = listOf(
                CoordinatorDemoEffect.TrackAnalytics("load_completed")
            )
        )

        is CoordinatorDemoEvent.DataSaved -> {
            if (event.success) {
                TransitionResult(
                    newState = CoordinatorDemoState.Saved("Data saved successfully!"),
                    effects = listOf(
                        CoordinatorDemoEffect.ShowSnackbar("Saved!"),
                        CoordinatorDemoEffect.TrackAnalytics("save_completed")
                    )
                )
            } else {
                TransitionResult(
                    newState = CoordinatorDemoState.Error("Failed to save"),
                    effects = listOf(
                        CoordinatorDemoEffect.TrackAnalytics("save_failed")
                    )
                )
            }
        }

        is CoordinatorDemoEvent.ErrorOccurred -> TransitionResult(
            newState = CoordinatorDemoState.Error(event.error)
        )

        is CoordinatorDemoEvent.Reset -> TransitionResult(
            newState = CoordinatorDemoState.Idle
        )
    }
}

@Composable
fun EffectCoordinatorExercise(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State machine
    var state: CoordinatorDemoState by remember { mutableStateOf(CoordinatorDemoState.Idle) }
    var inputData by remember { mutableStateOf("") }
    var executedEffects by remember { mutableStateOf(listOf<String>()) }

    // Effect coordinator (in real app, would be injected)
    val coordinator = remember {
        object {
            suspend fun execute(
                effect: CoordinatorDemoEffect,
                onEvent: (CoordinatorDemoEvent) -> Unit
            ) {
                when (effect) {
                    is CoordinatorDemoEffect.LoadData -> {
                        kotlinx.coroutines.delay(1000)
                        onEvent(CoordinatorDemoEvent.DataLoaded("Hello from server!"))
                    }

                    is CoordinatorDemoEffect.SaveData -> {
                        kotlinx.coroutines.delay(1000)
                        onEvent(CoordinatorDemoEvent.DataSaved(success = true))
                    }

                    is CoordinatorDemoEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }

                    is CoordinatorDemoEffect.TrackAnalytics -> {
                        // Would track analytics here
                    }
                }
            }
        }
    }

    // Event handler
    fun onEvent(event: CoordinatorDemoEvent) {
        val result = coordinatorDemoTransition(state, event)
        state = result.newState

        // Execute effects
        result.effects.forEach { effect ->
            executedEffects = executedEffects + effect::class.simpleName.orEmpty()
            scope.launch {
                coordinator.execute(effect, ::onEvent)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Text(
                text = "Exercise 04: Effect Coordinator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Demonstrates centralized effect execution with " +
                        "results fed back as events.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "Current State: ${state::class.simpleName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (val currentState = state) {
                        is CoordinatorDemoState.Idle -> {
                            Button(onClick = { onEvent(CoordinatorDemoEvent.LoadClicked) }) {
                                Text("Load Data")
                            }
                        }

                        is CoordinatorDemoState.Loading -> {
                            CircularProgressIndicator()
                            Text("Loading...")
                        }

                        is CoordinatorDemoState.Loaded -> {
                            val focusManager = LocalFocusManager.current
                            Text("Loaded: ${currentState.data}")

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputData,
                                onValueChange = { inputData = it },
                                label = { Text("Data to save") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (inputData.isNotBlank()) {
                                            onEvent(CoordinatorDemoEvent.SaveClicked(inputData))
                                        }
                                    }
                                )
                            )

                            Button(
                                onClick = { onEvent(CoordinatorDemoEvent.SaveClicked(inputData)) },
                                enabled = inputData.isNotBlank()
                            ) {
                                Text("Save Data")
                            }
                        }

                        is CoordinatorDemoState.Saving -> {
                            CircularProgressIndicator()
                            Text("Saving: ${currentState.data}")
                        }

                        is CoordinatorDemoState.Saved -> {
                            Text(
                                text = currentState.message,
                                color = GoodColor,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { onEvent(CoordinatorDemoEvent.Reset) }) {
                                Text("Reset")
                            }
                        }

                        is CoordinatorDemoState.Error -> {
                            Text(
                                text = "Error: ${currentState.error}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { onEvent(CoordinatorDemoEvent.Reset) }) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Effects Executed:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    executedEffects.forEachIndexed { index, effect ->
                        Text("${index + 1}. $effect")
                    }

                    if (executedEffects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { executedEffects = emptyList() }) {
                            Text("Clear Log")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Effect Coordinator Pattern",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
                            Flow:
                            1. User clicks → Event dispatched
                            2. Transition returns [NewState, Effects]
                            3. State updated immediately
                            4. Coordinator executes effects
                            5. Results become new events
                            6. Cycle continues...

                            Benefits:
                            • Pure transitions (testable!)
                            • Centralized effect handling
                            • Effects as inspectable data
                            • Easy to mock for tests
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
