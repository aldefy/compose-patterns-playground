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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Use Proper ViewModel or rememberSaveable
// ============================================================================
//
// SOLUTIONS:
// 1. Use actual ViewModel with viewModel() - survives config changes
// 2. Use rememberSaveable for simple values - saved to Bundle
// 3. Keep state in proper state holders
// ============================================================================

/**
 * Proper ViewModel using AndroidX ViewModel
 */
class CounterViewModel : ViewModel() {
    var counter by mutableIntStateOf(0)
        private set

    fun increment() {
        counter++
    }

    fun reset() {
        counter = 0
    }
}

/**
 * FIXED: Using proper ViewModel
 */
@Composable
fun ViewModelInComposableFixed(
    modifier: Modifier = Modifier
) {
    // GOOD: Proper ViewModel - survives configuration changes
    val viewModel: CounterViewModel = viewModel()

    // GOOD: rememberSaveable for simple values that need to survive config changes
    var savedCounter by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Proper State Management",
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
                    text = "Proper ViewModel:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Counter: ${viewModel.counter}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = { viewModel.increment() }) {
                    Text("Increment")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "rememberSaveable:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Counter: $savedCounter",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = { savedCounter++ }) {
                    Text("Increment")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Both survive rotation!",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoodColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "When to Use What",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        ViewModel:
                        • Complex state with business logic
                        • State that needs to survive config changes
                        • When you need coroutine scope (viewModelScope)
                        • Shared state across composables

                        rememberSaveable:
                        • Simple UI state (scroll position, text input)
                        • Primitive values or Parcelable objects
                        • When you don't need business logic

                        remember:
                        • Ephemeral state that can be lost
                        • Animation state
                        • UI-only concerns
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
