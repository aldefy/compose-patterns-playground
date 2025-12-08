package com.example.patterns.exercises.ex03_antipatterns.ap04_state_in_loop

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Use key() to associate state with the right item
// ============================================================================
//
// SOLUTIONS:
// 1. Use key(item.id) { } to wrap iteration content
// 2. Use LazyColumn with key = { it.id } (preferred for lists)
// 3. Hoist state to parent (store checked state in the Task data class)
//
// The key tells Compose how to match up state across recompositions.
// ============================================================================

/**
 * FIXED: Using key() to properly associate state with items
 */
@Composable
fun StateInLoopFixed(
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
            .padding(16.dp)
    ) {

        Text(
            text = "Using key() for State",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

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

        Text(
            text = "Now state follows the correct item!",
            style = MaterialTheme.typography.labelSmall,
            color = GoodColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                tasks.forEach { task ->
                    // GOOD: key() tells Compose to associate state with task.id
                    key(task.id) {
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why does this work?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        key(task.id) tells Compose:
                        "This state belongs to THIS specific task"

                        Even when the list reorders:
                        - Task 1's state stays with Task 1
                        - New items get fresh state
                        - State is properly associated
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Even better: Hoist the checked state to the data model
 *
 * This is the recommended approach for most cases.
 */
data class TaskWithState(
    val id: Int,
    val name: String,
    val isCompleted: Boolean = false
)

@Composable
fun StateInLoopBetterFixed(
    modifier: Modifier = Modifier
) {
    var tasks by remember {
        mutableStateOf(
            listOf(
                TaskWithState(1, "Buy groceries"),
                TaskWithState(2, "Walk the dog"),
                TaskWithState(3, "Write code"),
                TaskWithState(4, "Review PRs")
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Best: State in Data Model",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        tasks.forEach { task ->
            // No local state needed - it's in the data model!
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { checked ->
                        // Update the list with the new state
                        tasks = tasks.map {
                            if (it.id == task.id) it.copy(isCompleted = checked)
                            else it
                        }
                    }
                )
                Text(
                    text = "${task.name} (id: ${task.id})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
