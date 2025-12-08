package com.example.patterns.exercises.ex03_antipatterns.ap06_flow_collect_wrong

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.patterns.ui.components.ExerciseToggle

/**
 * Anti-Pattern 06: Wrong Flow Collection
 *
 * THE PROBLEM:
 * Collecting flows incorrectly in Compose causes:
 * - Flows restarting on recomposition
 * - Missing emissions
 * - Duplicate processing
 *
 * COMMON MISTAKES:
 * 1. Creating flow inside composable body
 * 2. Using changing value as LaunchedEffect key
 * 3. Not using collectAsState for simple cases
 *
 * SOLUTIONS:
 * - Create flows in ViewModel/Repository
 * - Use collectAsState() or collectAsStateWithLifecycle()
 * - Use remember { } if flow must be created in composable
 * - Use LaunchedEffect(Unit) for flows that shouldn't restart
 */
@Composable
fun FlowCollectWrongExercise(
    modifier: Modifier = Modifier
) {
    ExerciseToggle(
        title = "AP06: Flow Collection",
        badContent = { FlowCollectWrongBroken() },
        goodContent = { FlowCollectWrongFixed() },
        modifier = modifier
    )
}
