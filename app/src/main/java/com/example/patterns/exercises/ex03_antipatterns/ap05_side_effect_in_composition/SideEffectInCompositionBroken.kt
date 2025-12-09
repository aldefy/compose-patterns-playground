package com.example.patterns.exercises.ex03_antipatterns.ap05_side_effect_in_composition

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
// ANTI-PATTERN 05: Side Effects During Composition
// ============================================================================
//
// THE BUG:
// Performing side effects (network calls, analytics, logging, DB writes)
// directly during composition. Composition can be called:
// - Multiple times
// - In any order
// - On any thread (not necessarily main)
// - And can be cancelled/restarted at any time
//
// SYMPTOMS:
// - Duplicate API calls
// - Analytics events counted multiple times
// - Race conditions
// - Inconsistent state
// ============================================================================

// Simulating analytics tracking - in real code this would be Firebase, etc.
object AnalyticsTracker {
    // Plain Int - not Compose state (like real Firebase SDK)
    var searchEventCount = 0
        private set

    fun trackSearch(query: String) {
        searchEventCount++
        Log.d("Analytics", "Search tracked: '$query' (total: $searchEventCount)")
    }

    fun reset() {
        searchEventCount = 0
    }
}

data class Product(val id: Int, val name: String, val price: String)

val sampleProducts = listOf(
    Product(1, "Wireless Headphones", "$99"),
    Product(2, "Smart Watch", "$299"),
    Product(3, "Laptop Stand", "$49"),
    Product(4, "USB-C Hub", "$79"),
    Product(5, "Mechanical Keyboard", "$149"),
    Product(6, "Gaming Mouse", "$69"),
    Product(7, "Monitor Light Bar", "$89"),
    Product(8, "Webcam HD", "$129")
)

/**
 * BROKEN: Performing side effects during composition
 */
@Composable
fun SideEffectInCompositionBroken(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // This state is used to display the counter - updated after each tracking
    var displayedCount by remember { mutableStateOf(0) }

    val filteredProducts = remember(searchQuery) {
        sampleProducts.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // BAD: Analytics tracked DURING composition!
    // This runs on EVERY recomposition, not just when searchQuery changes
    if (searchQuery.isNotEmpty()) {
        AnalyticsTracker.trackSearch(searchQuery)
        displayedCount = AnalyticsTracker.searchEventCount
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search field at TOP - stays visible with keyboard
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search products...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Analytics counter - visible while typing
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search events: $displayedCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BadColor
                )
                Text(
                    text = "Typed ${searchQuery.length} chars",
                    style = MaterialTheme.typography.bodySmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product list and other content in scrollable area
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Instructions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BadColor.copy(alpha = 0.15f)
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
                            text = "Type \"watch\" (5 letters) and watch the counter.\n" +
                                "It may go higher than 5 due to recompositions!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Products
            items(filteredProducts, key = { it.id }) { product ->
                ProductCard(product)
            }

            // The buggy code
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BadColor.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "The Bug",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BadColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = """// BAD: Runs during composition!
@Composable
fun SearchScreen() {
    if (query.isNotEmpty()) {
        Analytics.track(query) // ❌
    }
}""",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // Explanation at bottom
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "What's wrong?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Side effects during composition:\n" +
                                "• May run multiple times\n" +
                                "• No guarantee of execution order\n" +
                                "• Can be cancelled mid-way\n" +
                                "• Real analytics over-counted!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = product.price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
