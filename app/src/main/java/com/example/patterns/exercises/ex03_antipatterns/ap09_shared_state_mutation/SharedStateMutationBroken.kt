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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 09: Mutating Shared State Directly
// ============================================================================
//
// THE BUG:
// Mutating a mutable collection in state doesn't trigger recomposition.
// Compose uses reference equality for collections - mutating the same
// list object doesn't create a new reference.
//
// SYMPTOMS:
// - UI doesn't update after adding/removing items
// - "I added an item but it doesn't show!"
// - State seems stuck
// ============================================================================

/**
 * BROKEN: Mutating a mutable list doesn't trigger recomposition
 */
@Composable
fun SharedStateMutationBroken(
    modifier: Modifier = Modifier
) {
    // BAD: Using MutableList - mutations won't trigger recomposition!
    var items by remember { mutableStateOf(mutableListOf("Item 1", "Item 2", "Item 3")) }
    var counter by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Mutable List Mutation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
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
                        // BAD: Mutating the same list object
                        items.add("Item ${items.size + 1}")
                        // UI won't update because 'items' reference hasn't changed!
                    }) {
                        Text("Add Item (Broken)")
                    }

                    Button(onClick = {
                        if (items.isNotEmpty()) {
                            items.removeAt(items.lastIndex)
                            // Same problem - UI won't update
                        }
                    }) {
                        Text("Remove (Broken)")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // This button forces recomposition
                Button(onClick = { counter++ }) {
                    Text("Force Recomposition ($counter)")
                }

                Text(
                    text = "Click Add, then Force Recomposition to see items!",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why doesn't it update?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        Compose checks: Did the STATE REFERENCE change?

                        items.add() mutates the SAME list object.
                        The reference doesn't change, so Compose thinks
                        nothing changed and skips recomposition.

                        The items ARE being added internally,
                        but the UI never recomposes to show them!

                        "Force Recomposition" reveals the hidden items.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
