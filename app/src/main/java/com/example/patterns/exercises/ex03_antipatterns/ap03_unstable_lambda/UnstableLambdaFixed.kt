package com.example.patterns.exercises.ex03_antipatterns.ap03_unstable_lambda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Stabilize lambda references
// ============================================================================
//
// SOLUTIONS:
// 1. Use remember {} to cache the lambda
// 2. Use a method reference (viewModel::onEvent)
// 3. Move the handler to a ViewModel (stable reference)
// 4. Strong Skipping Mode (Kotlin 2.0.20+) - auto-memoizes lambdas
//
// The goal is to ensure the lambda reference doesn't change
// unless the behavior actually changes.
// ============================================================================

/**
 * FIXED: Uses remembered lambda - same instance each recomposition
 *
 * When you tap an item:
 * 1. selectedItem changes → Parent recomposes
 * 2. But onClick lambda is REMEMBERED - same instance!
 * 3. Compose sees same lambda reference → skips recomposition
 * 4. Only the tapped item recomposes (because isSelected changed)
 */
@Composable
fun UnstableLambdaFixed(
    modifier: Modifier = Modifier
) {
    val items = remember {
        (1..50).map { ListItem(it, "Item $it") }
    }
    var selectedItem by remember { mutableStateOf<ListItem?>(null) }

    // FIX: Remember the lambda with a callback that takes the item
    // This creates ONE stable function that can handle any item
    val onItemClick: (ListItem) -> Unit = remember {
        { item: ListItem ->
            selectedItem = item
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

        Spacer(modifier = Modifier.height(8.dp))

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.15f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Try This",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoodColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Tap any item to select it\n" +
                        "2. Only the selected item's count increases!\n\n" +
                        "The lambda is remembered - same instance each time.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "What is Memoization?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Memoization = caching a value so it's not recreated.\n\n" +
                                "Without remember:\n" +
                                "  onClick = { select(item) } → NEW lambda each time\n\n" +
                                "With remember:\n" +
                                "  val onClick = remember { { select(item) } } → SAME instance\n\n" +
                                "Strong Skipping Mode (Kotlin 2.0.20+) auto-wraps lambdas in remember!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected item display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = "Selected: ${selectedItem?.text ?: "None"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items, key = { it.id }) { item ->
                // GOOD: Pass the remembered lambda with the item
                // Same function reference, just different argument
                ItemCardFixed(
                    item = item,
                    isSelected = selectedItem?.id == item.id,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * Child composable - only recomposes when its parameters actually change
 */
@Composable
private fun ItemCardFixed(
    item: ListItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track recompositions
    val recomposeCounter = remember { intArrayOf(0) }
    recomposeCounter[0]++
    val currentCount = recomposeCounter[0]

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                GoodColor.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GoodColor.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "recomposed: $currentCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoodColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
