package com.example.patterns.exercises.ex03_antipatterns.ap03_unstable_lambda

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 03: Unstable Lambda Captures
 *
 * THE PROBLEM:
 * Lambda expressions that capture external state create new instances
 * on every recomposition. This causes child composables to recompose
 * even when their actual inputs haven't changed.
 *
 * SOLUTIONS:
 * 1. Use remember {} to cache lambdas
 * 2. Use method references (viewModel::onEvent)
 * 3. Hoist state to a ViewModel (methods are stable by default)
 * 4. Use @Stable annotation for custom types
 *
 * RULE OF THUMB:
 * If a lambda is passed to a child composable, ensure the reference
 * is stable. Use remember or method references.
 */
@Composable
fun UnstableLambdaExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP03: Unstable Lambda",
        badContent = { UnstableLambdaBroken() },
        goodContent = { UnstableLambdaFixed() },
        modifier = modifier
    )
}
