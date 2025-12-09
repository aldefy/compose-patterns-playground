package com.example.patterns.exercises.ex03_antipatterns.ap06_flow_collect_wrong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ============================================================================
// FIX: Proper Flow Collection in Compose
// ============================================================================
//
// SOLUTIONS:
// 1. Create flows OUTSIDE composables (in ViewModel/Repository)
// 2. Use collectAsState() for simple state mapping
// 3. Use remember { flow } if you must create in composable
// 4. Use LaunchedEffect(Unit) for flows that shouldn't restart
// ============================================================================

/**
 * FIXED: Proper flow collection
 */
@Composable
fun FlowCollectWrongFixed(
    modifier: Modifier = Modifier
) {
    var unrelatedCounter by remember { mutableIntStateOf(0) }

    // GOOD: Create flow once with remember
    // In real apps, this would come from ViewModel
    val tickerFlow: Flow<Int> = remember {
        flow {
            var count = 0
            while (true) {
                emit(count++)
                delay(1000)
            }
        }
    }

    // GOOD: Use collectAsState for simple state mapping
    // This handles lifecycle automatically and doesn't restart on recomposition
    val tickCount by tickerFlow.collectAsState(initial = 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Proper Flow Collection",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tick Count: $tickCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Counter keeps incrementing smoothly!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoodColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { unrelatedCounter++ }) {
                    Text("Trigger Recomposition ($unrelatedCounter)")
                }

                Text(
                    text = "Recomposition doesn't restart the flow!",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoodColor
                )
            }
        }

        // The fix code
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.05f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "The Fix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoodColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = """// GOOD: Create flow once with remember
val tickerFlow = remember { // ✓
    flow { emit(count++) }
}

// GOOD: Use collectAsState!
val tickCount by tickerFlow
    .collectAsState(initial = 0) // ✓""",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why this works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. remember { flow { } }\n" +
                        "   → Same instance across recompositions\n\n" +
                        "2. collectAsState()\n" +
                        "   → Handles lifecycle automatically\n" +
                        "   → No manual LaunchedEffect needed\n\n" +
                        "In production: use ViewModel + collectAsStateWithLifecycle()",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
