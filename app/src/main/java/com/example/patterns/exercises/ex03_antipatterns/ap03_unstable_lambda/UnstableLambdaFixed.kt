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
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Stabilize lambda references
// ============================================================================
//
// SOLUTIONS:
// 1. Use remember {} to cache the lambda
// 2. Use a method reference (viewModel::onEvent)
// 3. Move the handler to a ViewModel (stable reference)
// 4. Use @Stable annotation for custom types
//
// The goal is to ensure the lambda reference doesn't change
// unless the behavior actually changes.
// ============================================================================

/**
 * A stable handler class that can be remembered
 */
class TodoHandler(
    private val onItemsChange: (List<TodoItem>) -> Unit
) {
    fun toggle(id: Int, items: List<TodoItem>) {
        val newItems = items.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted)
            else it
        }
        onItemsChange(newItems)
    }
}

/**
 * FIXED: Uses remembered lambda and stable references
 */
@Composable
fun UnstableLambdaFixed(
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

    // FIX 1: Remember a handler object
    val handler = remember {
        TodoHandler { newItems -> items = newItems }
    }

    // FIX 2: Remember the toggle function with correct dependencies
    val onToggle = remember<(Int) -> Unit> {
        { id ->
            items = items.map {
                if (it.id == id) it.copy(isCompleted = !it.isCompleted)
                else it
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Stable Lambda References",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Text(
            text = "Counter: $counter",
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = { counter++ }) {
            Text("Increment Counter")
        }

        Text(
            text = "Watch: Items DON'T recompose when counter changes!",
            style = MaterialTheme.typography.labelSmall,
            color = GoodColor
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                // GOOD: Using remembered lambda - same reference each time
                TodoItemCardFixed(
                    item = item,
                    onToggle = onToggle
                )
            }
        }
    }
}

/**
 * Child composable - now only recomposes when its data changes
 */
@Composable
private fun TodoItemCardFixed(
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

/**
 * Even better: Using ViewModel approach (recommended for production)
 *
 * When using a ViewModel:
 * - Methods are stable references by default
 * - viewModel::onToggle creates a stable reference
 * - No need for manual remember
 *
 * Example:
 * ```
 * class TodoViewModel : ViewModel() {
 *     var items by mutableStateOf<List<TodoItem>>(emptyList())
 *         private set
 *
 *     fun onToggle(id: Int) {
 *         items = items.map { ... }
 *     }
 * }
 *
 * // In Composable:
 * TodoItemCard(item, viewModel::onToggle) // Stable reference!
 * ```
 */
