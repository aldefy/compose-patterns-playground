package com.example.patterns.exercises.ex03_antipatterns.ap12_effects_in_transition

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 12: Side Effects in State Transitions
 *
 * THE PROBLEM:
 * Executing side effects directly during state transitions:
 * - Makes transitions impure and untestable
 * - Side effects can't be inspected
 * - Race conditions and ordering issues
 * - Retry/rollback becomes impossible
 *
 * THE SOLUTION:
 * TransitionResult pattern - return effects as DATA:
 * - Transitions are pure functions
 * - Return new state + list of effects
 * - Execute effects separately
 *
 * This is the core insight of the state machine pattern!
 */
@Composable
fun EffectsInTransitionExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP12: Effects in Transitions",
        badContent = { EffectsInTransitionBroken() },
        goodContent = { EffectsInTransitionFixed() },
        modifier = modifier
    )
}
