package com.example.patterns.exercises.ex03_antipatterns.ap02_derived_state_misuse

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 02: derivedStateOf Misuse
 *
 * COMMON MISTAKES:
 * 1. Using derivedStateOf for simple computations that change every time
 * 2. Not using it when you SHOULD (expensive filtering, threshold checks)
 * 3. Confusing it with remember(key) {}
 *
 * WHEN TO USE derivedStateOf:
 * - Source changes frequently (e.g., scroll position, every keystroke)
 * - Computed value changes less frequently (e.g., threshold crossing)
 * - Expensive computation you want to avoid repeating
 *
 * WHEN NOT TO USE:
 * - Simple property access (just use the property)
 * - Computation changes as often as source (no benefit)
 * - One-time computation (use remember with keys)
 */
@Composable
fun DerivedStateMisuseExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP02: derivedStateOf Misuse",
        badContent = { DerivedStateMisuseBroken() },
        goodContent = { DerivedStateMisuseFixed() },
        modifier = modifier
    )
}
