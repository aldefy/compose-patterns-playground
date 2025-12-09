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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ============================================================================
// ANTI-PATTERN 06: Wrong Way to Collect Flows
// ============================================================================
//
// THE BUG:
// Collecting flows incorrectly in Compose:
// 1. Collecting in LaunchedEffect with wrong key
// 2. Not using collectAsState for simple state mapping
// 3. Creating new flow on each recomposition
//
// SYMPTOMS:
// - Flow restarts on every recomposition
// - Missing emissions
// - Duplicate processing
// - Memory leaks
// ============================================================================

// A simple ticker flow for demonstration
fun createTickerFlow(): Flow<Int> = flow {
    var count = 0
    while (true) {
        emit(count++)
        delay(1000)
    }
}

/**
 * BROKEN: Creating flow inside composable and wrong LaunchedEffect usage
 */
@Composable
fun FlowCollectWrongBroken(
    modifier: Modifier = Modifier
) {
    var tickCount by remember { mutableStateOf(0) }
    var unrelatedCounter by remember { mutableIntStateOf(0) }

    // BAD: Creating flow inside the composable!
    // This creates a NEW flow on every recomposition
    val tickerFlow = flow {
        var count = 0
        while (true) {
            emit(count++)
            delay(1000)
        }
    }

    // BAD: Using the flow value as a key
    // This restarts collection every time tickCount changes!
    LaunchedEffect(tickCount) {
        tickerFlow.collect { value ->
            tickCount = value
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Wrong Flow Collection",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
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
                    text = "Notice: Counter keeps resetting!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BadColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { unrelatedCounter++ }) {
                    Text("Trigger Recomposition ($unrelatedCounter)")
                }

                Text(
                    text = "Clicking creates a NEW flow each time!",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor
                )
            }
        }

        // The buggy code
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
                    text = """// BAD: Flow created inside composable!
val tickerFlow = flow { // ❌ New each recomposition
    emit(count++)
}

// BAD: Wrong key causes restart loop!
LaunchedEffect(tickCount) { // ❌
    tickerFlow.collect { value ->
        tickCount = value // restarts effect!
    }
}""",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What's wrong?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Flow created INSIDE composable\n" +
                        "   → New instance each recomposition\n\n" +
                        "2. LaunchedEffect(tickCount)\n" +
                        "   → Restarts when tickCount changes\n" +
                        "   → Which changes tickCount... loop!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
