package com.example.patterns.exercises.ex03_antipatterns.ap01_launched_effect_trap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 01: LaunchedEffect Self-Cancellation Trap
 *
 * THE PROBLEM:
 * Using a value as a LaunchedEffect key that you modify inside the effect.
 * This causes the effect to cancel itself at each suspension point.
 *
 * SYMPTOMS:
 * - Operations that never complete
 * - Loading states that get "stuck"
 * - Infinite restart loops
 *
 * THE FIX:
 * Use keys that represent WHEN you want the effect to restart,
 * not the data you're modifying inside the effect.
 *
 * RULE OF THUMB:
 * - Key changes → Effect restarts
 * - Don't change the key inside the effect
 * - For loops: use while(isActive) inside
 * - For one-shots: use a trigger counter
 */
@Composable
fun LaunchedEffectTrapExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP01: LaunchedEffect Trap",
        badContent = { LaunchedEffectTrapBroken() },
        goodContent = { LaunchedEffectTrapFixed() },
        modifier = modifier
    )
}
