package com.example.patterns.exercises.ex03_antipatterns.ap03_unstable_lambda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 03: Unstable Lambda Captures
// ============================================================================
//
// THE BUG:
// Passing a new lambda instance on every recomposition causes child
// composables to recompose unnecessarily.
//
// WHY IT HAPPENS:
// In Kotlin, a lambda that captures external variables creates a new
// object each time. Compose can't know if the lambda's behavior changed,
// so it must recompose.
//
// SYMPTOMS:
// - List items recompose when parent state changes
// - Performance degradation with large lists
// - Compose compiler reports warn about unstable parameters
// ============================================================================

data class TodoItem(
    val id: Int,
    val text: String,
    val isCompleted: Boolean = false
)

/**
 * BROKEN: Creates new lambda on every recomposition
 *
 * When 'counter' changes, this whole composable recomposes.
 * The onItemClick lambda is recreated, causing ALL list items to recompose!
 */
@Composable
fun UnstableLambdaBroken(
    modifier: Modifier = Modifier
) {
    var counter by remember { mutableIntStateOf(0) }
    var items by remember {
        mutableStateOf(
            listOf(
                TodoItem(1, "Learn Compose"),
                TodoItem(2, "Understand State"),
                TodoItem(3, "Master Recomposition"),
                TodoItem(4, "Avoid Anti-patterns"),
                TodoItem(5, "Build Great Apps")
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Unstable Lambda Captures",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Text(
            text = "Counter: $counter",
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = { counter++ }) {
            Text("Increment Counter")
        }

        Text(
            text = "Watch: All items recompose when counter changes!",
            style = MaterialTheme.typography.labelSmall,
            color = BadColor
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                // BAD: This lambda is recreated every recomposition
                // because it captures 'items' which is a new object each time
                TodoItemCardBroken(
                    item = item,
                    onToggle = { id ->
                        // This lambda captures 'items' - a new object each recomposition!
                        items = items.map {
                            if (it.id == id) it.copy(isCompleted = !it.isCompleted)
                            else it
                        }
                    }
                )
            }
        }
    }
}

/**
 * Child composable that shows recomposition count
 *
 * Even though this item didn't change, it recomposes because
 * the onToggle lambda is a new instance every time.
 */
@Composable
private fun TodoItemCardBroken(
    item: TodoItem,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onToggle(item.id) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.isCompleted) FontWeight.Light else FontWeight.Normal
            )
        }
    }
}
