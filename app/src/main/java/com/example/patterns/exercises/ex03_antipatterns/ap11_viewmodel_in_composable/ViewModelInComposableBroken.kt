package com.example.patterns.exercises.ex03_antipatterns.ap11_viewmodel_in_composable

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 11: Creating ViewModel-like Objects in Composables
// ============================================================================
//
// THE BUG:
// Creating state holder classes directly inside composables:
// - Doesn't survive configuration changes
// - Creates new instance on recomposition if not remembered correctly
// - Loses state unexpectedly
//
// SYMPTOMS:
// - State lost on rotation
// - "My counter reset!"
// - Inconsistent behavior between screens
// ============================================================================

/**
 * Plain class used as state holder - NOT a real ViewModel!
 * This won't survive configuration changes (screen rotation).
 */
class PlainStateHolder {
    var counter: Int = 0
        private set

    fun increment() {
        counter++
    }
}

/**
 * BROKEN: Creating state holder class inside composable
 *
 * This creates a new PlainStateHolder every recomposition!
 * (Unless wrapped in remember, but even then it won't survive config changes)
 */
@Composable
fun ViewModelInComposableBroken(
    modifier: Modifier = Modifier
) {
    // BAD: Creates new instance on every recomposition!
    val notRemembered = PlainStateHolder()

    // Even with remember, it won't survive configuration changes
    val remembered = remember { PlainStateHolder() }

    var forceRecompose by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Plain Class as State Holder",
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
                    text = "Without remember:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Counter: ${notRemembered.counter}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = { notRemembered.increment() }) {
                    Text("Increment (Always 0!)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "With remember:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Counter: ${remembered.counter}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = {
                    remembered.increment()
                    forceRecompose++ // Force recompose to see change
                }) {
                    Text("Increment (Works...)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rotate device → remembered counter resets!",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.05f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "The Bug",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BadColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """// BAD: Plain class for state
class MyStateHolder {
    var count = 0
}

@Composable
fun Screen() {
    // ❌ New instance every recomposition!
    val holder = MyStateHolder()

    // ❌ Survives recomposition, but
    // dies on screen rotation!
    val holder2 = remember { MyStateHolder() }
}""",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why it fails",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """Without remember:
• New instance every recomposition
• Counter always shows 0

With remember:
• Survives recomposition ✓
• Lost on rotation ✗

Real ViewModel:
• Survives both ✓
• Scoped to Activity/Fragment lifecycle""",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
