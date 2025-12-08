package com.example.patterns.exercises.ex03_antipatterns.ap07_state_read_too_high

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 07: Reading State Too High in the Tree
// ============================================================================
//
// THE BUG:
// Reading state at a parent level causes the entire subtree to recompose,
// even if only a small part of the UI uses that state.
//
// WHY IT MATTERS:
// Compose recomposes the smallest scope that reads a changed state.
// If you read state at the parent, the parent and ALL children recompose.
//
// SYMPTOMS:
// - Large parts of the UI recompose on every state change
// - Poor performance with complex UIs
// - Choppy animations or scrolling
// ============================================================================

/**
 * BROKEN: Reading state at the parent level
 *
 * The slider value is read here, causing ALL children to recompose
 * on every slider movement (60+ times per second!).
 */
@Composable
fun StateReadTooHighBroken(
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    var counter by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "State Read Too High",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BAD: Reading sliderValue here causes whole tree to recompose
        Text(
            text = "Slider Value: ${(sliderValue * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // These children have NOTHING to do with the slider
        // But they recompose on every slider change!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "This card doesn't use the slider!",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "But it recomposes every time you move it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Counter: $counter")
            }

            Button(onClick = { counter++ }) {
                Text("Increment")
            }
        }

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
                        Move the slider and watch ALL components recompose.

                        The "Unrelated Card" and "Counter Section" have
                        nothing to do with the slider, but they still
                        recompose because sliderValue is read at the parent.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
