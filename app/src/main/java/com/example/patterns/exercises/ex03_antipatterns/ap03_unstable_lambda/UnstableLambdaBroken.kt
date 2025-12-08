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
import androidx.compose.runtime.DontMemoize
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
// ANTI-PATTERN 03: Unstable Lambda Captures
// ============================================================================
//
// THE PROBLEM:
// Imagine a list of 1000 items, each with a click handler.
// If click handlers are recreated on every recomposition, Compose thinks
// EVERY item has changed (because each received a "new" click handler).
// All 1000 items recompose unnecessarily!
//
// THE PATTERN:
//   Parent recomposes → new lambda created → Child sees "new" parameter
//   → Child recomposes even though nothing actually changed
//
// Note: Strong Skipping Mode (Kotlin 2.0.20+) auto-fixes this by wrapping
// lambdas in remember. We use @DontMemoize to demonstrate the bug.
// ============================================================================

data class ListItem(
    val id: Int,
    val text: String
)

/**
 * BROKEN: Creates new lambda on every recomposition
 *
 * When you tap an item:
 * 1. selectedItem changes → Parent recomposes
 * 2. New onClick lambda is created for EVERY item
 * 3. Compose thinks every ItemCard has "changed"
 * 4. ALL items recompose unnecessarily!
 */
@Composable
fun UnstableLambdaBroken(
    modifier: Modifier = Modifier
) {
    val items = remember {
        (1..50).map { ListItem(it, "Item $it") }
    }
    var selectedItem by remember { mutableStateOf<ListItem?>(null) }

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
                    text = "1. Tap any item to select it\n" +
                        "2. Watch ALL items' recompose counts increase!\n\n" +
                        "Only 1 item changed, but ALL recompose because each gets a \"new\" lambda.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected item display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
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
                // BAD: New lambda created for EVERY item on EVERY recomposition!
                // @DontMemoize prevents Strong Skipping from auto-fixing this
                ItemCardBroken(
                    item = item,
                    isSelected = selectedItem?.id == item.id,
                    onClick = @DontMemoize {
                        selectedItem = item  // New lambda instance each time!
                    }
                )
            }
        }
    }
}

/**
 * Child composable that shows recomposition count
 */
@Composable
private fun ItemCardBroken(
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
                BadColor.copy(alpha = 0.3f)
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
                    containerColor = BadColor.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "recomposed: $currentCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
