package com.example.patterns.exercises.ex03_antipatterns.ap09_shared_state_mutation

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Use Immutable Collections or Compose State Lists
// ============================================================================
//
// SOLUTIONS:
// 1. Create a NEW list on each modification (immutable pattern)
// 2. Use mutableStateListOf() which notifies Compose of mutations
// 3. Use Kotlin immutable collections library
//
// The goal is to ensure Compose detects the change.
// ============================================================================

/**
 * FIXED: Creating new list on modification
 */
@Composable
fun SharedStateMutationFixed(
    modifier: Modifier = Modifier
) {
    // GOOD: Use an immutable list and create new instances on change
    var items by remember { mutableStateOf(listOf("Item 1", "Item 2", "Item 3")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Immutable List Pattern",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Items (${items.size}):",
                    style = MaterialTheme.typography.titleMedium
                )

                items.forEach { item ->
                    Text(text = "• $item")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        // GOOD: Create a NEW list with the added item
                        items = items + "Item ${items.size + 1}"
                    }) {
                        Text("Add Item")
                    }

                    Button(onClick = {
                        if (items.isNotEmpty()) {
                            // GOOD: Create a NEW list without the last item
                            items = items.dropLast(1)
                        }
                    }) {
                        Text("Remove")
                    }
                }

                Text(
                    text = "Updates immediately!",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoodColor
                )
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
                        items = items + newItem

                        This creates a NEW list object!
                        Compose sees the reference changed and recomposes.

                        Alternative: mutableStateListOf()
                        • Compose-aware mutable list
                        • Mutations automatically trigger recomposition
                        • Better for frequent updates
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Alternative: Using mutableStateListOf
 */
@Composable
fun SharedStateMutationWithStateList(
    modifier: Modifier = Modifier
) {
    // GOOD: mutableStateListOf is Compose-aware
    val items = remember { listOf("Item 1", "Item 2", "Item 3").toMutableStateList() }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Using mutableStateListOf",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        items.forEach { item ->
            Text(text = "• $item")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                // This mutation IS detected by Compose!
                items.add("Item ${items.size + 1}")
            }) {
                Text("Add")
            }

            Button(onClick = {
                if (items.isNotEmpty()) {
                    items.removeAt(items.lastIndex)
                }
            }) {
                Text("Remove")
            }
        }
    }
}
