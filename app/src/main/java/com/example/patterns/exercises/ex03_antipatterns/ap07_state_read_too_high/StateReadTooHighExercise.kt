package com.example.patterns.exercises.ex03_antipatterns.ap07_state_read_too_high

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 07: Reading State Too High in the Tree
 *
 * THE PROBLEM:
 * Reading state at a parent level causes the entire subtree to recompose,
 * even if only a small part of the UI uses that state.
 *
 * SOLUTIONS:
 * 1. Move state reads DOWN to components that need them
 * 2. Pass lambdas that defer state reading
 * 3. Extract components to create isolated recomposition scopes
 * 4. Use State<T> instead of T when passing state
 *
 * RULE: Reading state defines the recomposition scope.
 */
@Composable
fun StateReadTooHighExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP07: State Read Too High",
        badContent = { StateReadTooHighBroken() },
        goodContent = { StateReadTooHighFixed() },
        modifier = modifier
    )
}
