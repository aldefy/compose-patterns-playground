package com.example.patterns.exercises.ex03_antipatterns.ap04_state_in_loop

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 04: remember in a Loop
// ============================================================================
//
// THE BUG:
// Using remember { mutableStateOf() } inside a loop or iteration
// without a proper key causes state to be mixed up or lost.
//
// WHY IT HAPPENS:
// Compose tracks remember calls by position in the call tree.
// Inside a loop, if items are added/removed/reordered, the position
// changes and remember returns the wrong cached value.
//
// SYMPTOMS:
// - Checkbox for item A shows item B's state
// - State "jumps" to different items when list changes
// - Seemingly random behavior
// ============================================================================

data class Task(
    val id: Int,
    val name: String
)

/**
 * BROKEN: remember without proper key in iteration
 *
 * Try adding/removing items and watch the checkbox states get mixed up!
 */
@Composable
fun StateInLoopBroken(
    modifier: Modifier = Modifier
) {
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Buy groceries"),
                Task(2, "Walk the dog"),
                Task(3, "Write code"),
                Task(4, "Review PRs")
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "remember in Loop",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Try This",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BadColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Check the first 2 checkboxes\n" +
                        "2. Tap \"Add at Start\" to add a new task\n" +
                        "3. Watch the checkmarks \"jump\" to wrong items!\n\n" +
                        "The state stayed at position 0 & 1, but the items shifted.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                tasks = listOf(Task(newId, "New Task $newId")) + tasks
            }) {
                Text("Add at Start")
            }

            Button(onClick = {
                tasks = tasks.drop(1)
            }) {
                Text("Remove First")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                tasks.forEach { task ->
                    // BAD: remember is keyed by POSITION, not by task.id
                    // When tasks reorder, states get mixed up!
                    var isChecked by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it }
                        )
                        Text(
                            text = "${task.name} (id: ${task.id})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What's happening?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        remember uses POSITION as the key.

                        Position 0's state stays at position 0,
                        even if a NEW task is now at position 0!

                        Result: State belongs to wrong items.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
