package com.example.patterns.exercises.ex03_antipatterns.ap04_state_in_loop

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 04: remember in a Loop
 *
 * THE PROBLEM:
 * Using remember { mutableStateOf() } inside a loop without a key
 * causes state to be associated with position, not the actual item.
 *
 * SYMPTOMS:
 * - State "jumps" between items when list changes
 * - Checkbox for item A shows item B's state
 * - Unpredictable behavior
 *
 * SOLUTIONS:
 * 1. Use key(item.id) { } around the content
 * 2. Use LazyColumn with key = { it.id }
 * 3. Hoist state to the data model (best practice)
 */
@Composable
fun StateInLoopExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP04: State in Loop",
        badContent = { StateInLoopBroken() },
        goodContent = { StateInLoopFixed() },
        modifier = modifier
    )
}
