package com.example.patterns.exercises.ex03_antipatterns.ap10_event_vs_state

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 10: Confusing Events with State
 *
 * THE PROBLEM:
 * Using state to represent one-time events leads to:
 * - Events processed multiple times
 * - Manual reset required
 * - Race conditions
 *
 * STATE is what the UI IS (persistent)
 * EVENTS are what HAPPENED (one-time)
 *
 * SOLUTIONS:
 * - Use Channel or SharedFlow for events
 * - Events consumed exactly once when collected
 * - No manual reset needed
 */
@Composable
fun EventVsStateExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP10: Events vs State",
        badContent = { EventVsStateBroken() },
        goodContent = { EventVsStateFixed() },
        modifier = modifier
    )
}
