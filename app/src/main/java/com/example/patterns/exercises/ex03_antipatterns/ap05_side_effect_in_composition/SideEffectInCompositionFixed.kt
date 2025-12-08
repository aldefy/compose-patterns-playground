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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay

// ============================================================================
// FIX: Use proper effect handlers
// ============================================================================
//
// SOLUTIONS:
// 1. LaunchedEffect(key) - for suspend functions, runs when key changes
// 2. SideEffect - for non-suspend effects that must run after composition
// 3. DisposableEffect - for effects that need cleanup
// 4. rememberCoroutineScope - for effects triggered by user events
//
// Effects run AFTER composition succeeds, guaranteeing:
// - They only run once per successful composition
// - They can be properly cancelled
// - They have a predictable lifecycle
// ============================================================================

// Proper analytics tracker with debouncing support
object ProperAnalyticsTracker {
    var searchEventCount = 0
        private set
    var viewedProductIds = mutableSetOf<Int>()
        private set

    fun trackSearch(query: String) {
        searchEventCount++
        Log.d("Analytics", "Search tracked: '$query' (total: $searchEventCount)")
    }

    fun trackProductView(productId: Int) {
        if (productId !in viewedProductIds) {
            viewedProductIds.add(productId)
            Log.d("Analytics", "Product $productId viewed (unique: ${viewedProductIds.size})")
        }
    }

    fun reset() {
        searchEventCount = 0
        viewedProductIds.clear()
    }
}

/**
 * FIXED: Using proper effect handlers for analytics
 */
@Composable
fun SideEffectInCompositionFixed(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(searchQuery) {
        sampleProducts.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // GOOD: LaunchedEffect with debouncing for search analytics
    // Only tracks after user stops typing for 500ms
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(500) // Debounce - wait for user to stop typing
            ProperAnalyticsTracker.trackSearch(searchQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Product Search (Fixed)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Instructions card
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
                    text = "1. Type \"watch\" in the search box below\n" +
                        "2. Wait half a second after typing\n" +
                        "3. Only ONE search event tracked!\n\n" +
                        "LaunchedEffect + debounce = proper analytics.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Analytics display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Search events tracked: ${ProperAnalyticsTracker.searchEventCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoodColor
                )
                Text(
                    text = "1 event per search (debounced 500ms)",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoodColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unique products viewed: ${ProperAnalyticsTracker.viewedProductIds.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search products...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Product list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                // GOOD: Track in LaunchedEffect, only once per product
                LaunchedEffect(product.id) {
                    ProperAnalyticsTracker.trackProductView(product.id)
                }

                ProductCardFixed(product)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Explanation
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Why this works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "LaunchedEffect(searchQuery):\n" +
                        "• Runs AFTER composition succeeds\n" +
                        "• Cancelled if key changes (new keystroke)\n" +
                        "• Debounce waits for user to stop typing\n\n" +
                        "LaunchedEffect(product.id):\n" +
                        "• Tracks each product only once\n" +
                        "• Won't re-run on recomposition",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ProductCardFixed(product: Product) {
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
