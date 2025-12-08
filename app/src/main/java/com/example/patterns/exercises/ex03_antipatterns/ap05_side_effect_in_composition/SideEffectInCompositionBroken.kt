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
    var searchEventCount = 0
        private set
    var viewedProductIds = mutableListOf<Int>()
        private set

    fun trackSearch(query: String) {
        searchEventCount++
        Log.d("Analytics", "Search tracked: '$query' (total: $searchEventCount)")
    }

    fun trackProductView(productId: Int) {
        viewedProductIds.add(productId)
        Log.d("Analytics", "Product $productId viewed (total views: ${viewedProductIds.size})")
    }

    fun reset() {
        searchEventCount = 0
        viewedProductIds.clear()
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
    val filteredProducts = remember(searchQuery) {
        sampleProducts.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // BAD: Analytics tracked DURING composition!
    // Every keystroke causes recomposition, which tracks another search event
    if (searchQuery.isNotEmpty()) {
        AnalyticsTracker.trackSearch(searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Product Search",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Instructions card
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
                    text = "1. Type \"watch\" in the search box below\n" +
                        "2. Look at the \"Search events tracked\" counter\n" +
                        "3. You typed 5 letters, but way more events tracked!\n\n" +
                        "Each keystroke triggers recomposition → analytics runs again.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Analytics display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Search events tracked: ${AnalyticsTracker.searchEventCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BadColor
                )
                Text(
                    text = "Expected: 1 per search, Actual: 1 per keystroke!",
                    style = MaterialTheme.typography.bodySmall,
                    color = BadColor
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
                // BAD: Product view tracked during composition!
                AnalyticsTracker.trackProductView(product.id)

                ProductCard(product)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Explanation
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "What's wrong?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Analytics code runs DURING composition:\n" +
                        "• Runs on every recomposition\n" +
                        "• 'watch' = 5 keystrokes = 5+ search events\n" +
                        "• Product views tracked repeatedly\n\n" +
                        "Your analytics dashboard shows 10x real usage!",
                    style = MaterialTheme.typography.bodySmall
                )
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
