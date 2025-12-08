package com.example.patterns.exercises.ex03_antipatterns.ap07_state_read_too_high

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Defer State Reading to Where It's Needed
// ============================================================================
//
// SOLUTIONS:
// 1. Move state reads DOWN to the components that need them
// 2. Pass lambdas that read state (deferred read)
// 3. Use separate composable scopes for different state
// 4. Pass State<T> instead of T
//
// The goal is to minimize the recomposition scope.
// ============================================================================

/**
 * FIXED: Reading state only where needed
 *
 * The slider value is only read inside SliderSection,
 * so only that section recomposes when the slider moves.
 */
@Composable
fun StateReadTooHighFixed(
    modifier: Modifier = Modifier
) {
    // State is DECLARED here but not READ here
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    var counter by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Deferred State Reading",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // GOOD: State is read only inside this component
        // Only SliderSection recomposes when slider moves!
        SliderSection(
            value = sliderValue,
            onValueChange = { sliderValue = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // These DON'T recompose when slider moves!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "This card doesn't recompose!",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Slider changes don't affect this scope.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoodColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // This section only recomposes when counter changes
        CounterSection(
            counter = counter,
            onIncrement = { counter++ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Watch the recomposition counts!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        Move the slider: only SliderSection recomposes.
                        Click increment: only CounterSection recomposes.
                        Parent and Unrelated Card stay stable!

                        Key insight: Reading state defines the recomposition scope.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Isolated slider section - state is read here
 */
@Composable
private fun SliderSection(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        Text(
            text = "Slider Value: ${(value * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Isolated counter section
 */
@Composable
private fun CounterSection(
    counter: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Counter: $counter")
        }

        Button(onClick = onIncrement) {
            Text("Increment")
        }
    }
}

/**
 * Alternative: Using lambda for deferred read
 *
 * This is useful when you can't easily extract to a component.
 */
@Composable
fun DeferredReadExample() {
    var value by remember { mutableFloatStateOf(0.5f) }

    Column {
        // BAD: Reading here
        // Text("Value: $value")

        // GOOD: Deferred read with lambda
        DeferredText(valueProvider = { value })

        Slider(
            value = value,
            onValueChange = { value = it }
        )
    }
}

@Composable
private fun DeferredText(
    valueProvider: () -> Float
) {
    // State is read HERE, inside this scope
    Text("Value: ${valueProvider()}")
}
