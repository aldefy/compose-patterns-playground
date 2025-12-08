package com.example.patterns.exercises.ex02_state_machine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Exercise 02: Full State Machine Pattern
 *
 * This exercise demonstrates the complete state machine architecture:
 *
 * COMPONENTS:
 * 1. ProfileState (sealed interface) - All valid states
 * 2. ProfileEvent (sealed interface) - All possible events
 * 3. ProfileEffect (sealed interface) - Side effects as data
 * 4. profileTransition() - Pure function: (State, Event) → (State, Effects)
 * 5. ProfileViewModel - Wires it all together
 * 6. ProfileScreen - Exhaustive UI based on state
 *
 * KEY INSIGHTS:
 *
 * 1. SEPARATION OF CONCERNS
 *    - State: What the screen IS (data)
 *    - Events: What HAPPENED (user actions, system responses)
 *    - Effects: What should happen NEXT (side effects)
 *    - Transition: How state changes (pure logic)
 *
 * 2. PURE TRANSITION FUNCTION
 *    - Same inputs always produce same outputs
 *    - No side effects during transition
 *    - Trivially testable without mocks
 *    - Easy to reason about
 *
 * 3. EFFECTS AS DATA
 *    - Side effects are returned, not executed
 *    - ViewModel handles effect execution separately
 *    - Makes the "what" separate from the "how"
 *
 * 4. EXHAUSTIVE HANDLING
 *    - 'when' expressions on sealed types are checked at compile time
 *    - Adding new states/events causes compile errors until handled
 *    - No forgotten edge cases!
 *
 * LEARNING OBJECTIVES:
 * - Understand the state machine architecture
 * - See how pure transitions enable testability
 * - Learn to model effects as data
 * - Practice exhaustive state handling in UI
 */
@Composable
fun StateMachineExercise(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Exercise 02: State Machine",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete state machine pattern with pure transitions, " +
                    "sealed states, and effects as data.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // The actual profile screen demonstrating the pattern
        ProfileScreen()
    }
}
