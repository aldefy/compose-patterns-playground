package com.example.patterns.exercises.ex03_antipatterns.ap08_remember_wrong_keys

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 08: Wrong Keys in remember
 *
 * THE PROBLEM:
 * Using wrong keys with remember causes stale data or excessive recalculation.
 *
 * COMMON MISTAKES:
 * 1. Missing keys → stale computation, never updates
 * 2. Too many keys → recalculates more than necessary
 * 3. Using whole object when only part is needed
 *
 * RULES:
 * - Include ALL inputs to the computation as keys
 * - Include ONLY inputs that affect the result
 * - Use specific properties over whole objects when appropriate
 */
@Composable
fun RememberWrongKeysExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP08: Wrong remember Keys",
        badContent = { RememberWrongKeysBroken() },
        goodContent = { RememberWrongKeysFixed() },
        modifier = modifier
    )
}
