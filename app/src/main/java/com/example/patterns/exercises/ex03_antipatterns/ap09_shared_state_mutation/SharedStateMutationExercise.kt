package com.example.patterns.exercises.ex03_antipatterns.ap09_shared_state_mutation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 09: Mutating Shared State Directly
 *
 * THE PROBLEM:
 * Mutating a mutable collection in state doesn't trigger recomposition.
 * Compose uses reference equality - same list = no change detected.
 *
 * SYMPTOMS:
 * - UI doesn't update after adding/removing items
 * - State seems stuck
 * - Items appear only after unrelated recomposition
 *
 * SOLUTIONS:
 * 1. Use immutable lists and create new instances: items = items + newItem
 * 2. Use mutableStateListOf() for Compose-aware mutations
 * 3. Use Kotlin immutable collections
 */
@Composable
fun SharedStateMutationExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP09: Shared State Mutation",
        badContent = { SharedStateMutationBroken() },
        goodContent = { SharedStateMutationFixed() },
        modifier = modifier
    )
}
