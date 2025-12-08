package com.example.patterns.ui.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.RecomposeHighlight

/**
 * RecompositionCounter - A debugging composable that shows how many times it has recomposed.
 *
 * WHY THIS COMPONENT?
 * ===================
 * One of the most common performance issues in Compose is unnecessary recompositions.
 * This component helps visualize recomposition by:
 * 1. Displaying a count that increments each recomposition
 * 2. Briefly flashing yellow when recomposition occurs
 * 3. Logging to Logcat with a tag for easy filtering
 *
 * USAGE:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     RecompositionCounter(label = "MyScreen")
 *     // ... rest of screen
 * }
 * ```
 *
 * WORKSHOP TIP:
 * Place this component inside different parts of your UI to see which
 * parts are recomposing. Unexpected recompositions often indicate:
 * - Unstable lambda captures (use remember)
 * - Reading state too high in the tree
 * - Passing unstable objects as parameters
 *
 * @param label A label to identify this counter in logs
 * @param modifier Modifier for the counter container
 */
@Composable
fun RecompositionCounter(
    label: String,
    modifier: Modifier = Modifier
) {
    // This state survives recomposition but increments each time
    var recomposeCount by remember { mutableIntStateOf(0) }

    // SideEffect runs after every successful recomposition
    SideEffect {
        recomposeCount++
        Log.d("RecomposeCounter", "[$label] Recomposition #$recomposeCount")
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label: $recomposeCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A variant that only logs without UI.
 * Useful for tracking recomposition in production code without UI overhead.
 */
@Composable
fun LogRecomposition(label: String) {
    var recomposeCount by remember { mutableIntStateOf(0) }

    SideEffect {
        recomposeCount++
        Log.d("RecomposeCounter", "[$label] Recomposition #$recomposeCount")
    }
}

/**
 * Highlights a composable when it recomposes.
 * Wraps content and flashes a border color on recomposition.
 */
@Composable
fun RecompositionHighlight(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var recomposeCount by remember { mutableIntStateOf(0) }

    SideEffect {
        recomposeCount++
        Log.d("RecomposeHighlight", "[$label] Recomposition #$recomposeCount")
    }

    val borderColor by animateColorAsState(
        targetValue = if (recomposeCount % 2 == 0) Color.Transparent else RecomposeHighlight,
        label = "border_flash"
    )

    Box(
        modifier = modifier
            .background(borderColor.copy(alpha = 0.2f))
            .padding(2.dp)
    ) {
        content()
    }
}
