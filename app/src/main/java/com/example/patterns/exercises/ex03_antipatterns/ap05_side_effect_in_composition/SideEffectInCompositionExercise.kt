package com.example.patterns.exercises.ex03_antipatterns.ap05_side_effect_in_composition

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 05: Side Effects During Composition
 *
 * THE PROBLEM:
 * Performing side effects directly in the composable body.
 * Composition can happen multiple times, in any order, on any thread.
 *
 * SYMPTOMS:
 * - Duplicate API calls
 * - Analytics counted multiple times
 * - Race conditions
 * - Unpredictable behavior
 *
 * SOLUTIONS:
 * - LaunchedEffect(key) for suspend effects
 * - SideEffect for post-composition effects
 * - DisposableEffect for cleanup-needed effects
 * - rememberCoroutineScope for event-triggered effects
 */
@Composable
fun SideEffectInCompositionExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP05: Side Effects in Composition",
        badContent = { SideEffectInCompositionBroken() },
        goodContent = { SideEffectInCompositionFixed() },
        modifier = modifier
    )
}
