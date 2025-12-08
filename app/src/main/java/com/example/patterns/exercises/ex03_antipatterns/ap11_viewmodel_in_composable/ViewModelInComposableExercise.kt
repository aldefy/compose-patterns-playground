package com.example.patterns.exercises.ex03_antipatterns.ap11_viewmodel_in_composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 11: Creating ViewModel-like Objects in Composables
 *
 * THE PROBLEM:
 * Creating state holder classes inside composables leads to:
 * - State lost on recomposition (if not remembered)
 * - State lost on configuration change (even if remembered)
 *
 * SOLUTIONS:
 * - Use proper ViewModel with viewModel() for screen-level state
 * - Use rememberSaveable for simple values that survive config changes
 * - Use remember only for ephemeral/UI-only state
 */
@Composable
fun ViewModelInComposableExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP11: Fake ViewModel",
        badContent = { ViewModelInComposableBroken() },
        goodContent = { ViewModelInComposableFixed() },
        modifier = modifier
    )
}
