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
 * A "ViewModel" that's not really a ViewModel
 */
class FakeViewModel {
    var counter: Int = 0
        private set

    fun increment() {
        counter++
    }

    fun reset() {
        counter = 0
    }
}

/**
 * BROKEN: Creating "ViewModel" inside composable
 *
 * This creates a new FakeViewModel every recomposition!
 * (Unless wrapped in remember, but even then it won't survive config changes)
 */
@Composable
fun ViewModelInComposableBroken(
    modifier: Modifier = Modifier
) {
    // BAD: Creates new instance on every recomposition!
    val viewModel = FakeViewModel()

    // Even with remember, it won't survive configuration changes
    val rememberedVm = remember { FakeViewModel() }

    var forceRecompose by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Fake ViewModel Anti-Pattern",
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
                    text = "Not Remembered VM:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Counter: ${viewModel.counter}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = { viewModel.increment() }) {
                    Text("Increment (Always 0!)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Remembered VM:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Counter: ${rememberedVm.counter}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = {
                    rememberedVm.increment()
                    forceRecompose++ // Force recompose to see change
                }) {
                    Text("Increment (Works, but...)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Try rotating device - remembered VM counter resets!",
                    style = MaterialTheme.typography.labelSmall,
                    color = BadColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Problems",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        1. Not remembered:
                           • New instance every recomposition
                           • Counter always shows 0
                           • State never persists

                        2. Remembered but not proper ViewModel:
                           • Works during composition
                           • Lost on configuration change
                           • Doesn't follow Compose state model
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
