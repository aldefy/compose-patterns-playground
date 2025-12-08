package com.example.patterns.exercises.ex01_boolean_explosion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// ============================================================================
// ANTI-PATTERN: Boolean Explosion
// ============================================================================
//
// THE PROBLEM:
// Using multiple boolean flags to represent state leads to bugs where
// developers forget to reset flags, creating impossible states.
//
// This demo shows a REAL bug: Save fails, then Save succeeds, but
// isError was never cleared - now isError=true AND isSuccess=true!
// ============================================================================

/**
 * BAD: State represented with multiple boolean flags
 */
data class ProfileScreenStateBad(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "John Doe",
    val email: String = "john@example.com"
)

/**
 * BAD: ViewModel with a subtle bug - forgot to clear isError on success
 */
class BuggyViewModel {
    var state by mutableStateOf(ProfileScreenStateBad())
        private set

    private var saveAttempts = 0

    suspend fun save() {
        saveAttempts++

        state = state.copy(
            isSaving = true,
            // BUG: We clear isSuccess but what about isError?
            // A careful dev might remember... or might not!
            isSuccess = false
        )

        delay(1000) // Simulate network

        if (saveAttempts == 1) {
            // First attempt fails
            state = state.copy(
                isSaving = false,
                isError = true,
                errorMessage = "Network error! Tap Save again."
            )
        } else {
            // Second attempt succeeds
            // BUG: Forgot to set isError = false!
            state = state.copy(
                isSaving = false,
                isSuccess = true
                // isError is still true from before! 💥
            )
        }
    }

    fun reset() {
        saveAttempts = 0
        state = ProfileScreenStateBad()
    }
}

/**
 * Shows the boolean explosion bug through normal user interaction
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BooleanExplosionBadScreen(
    modifier: Modifier = Modifier
) {
    val viewModel = remember { BuggyViewModel() }
    val state = viewModel.state
    val scope = rememberCoroutineScope()

    // Check if we're in an impossible state
    val trueFlags = listOf(
        "isLoading" to state.isLoading,
        "isSaving" to state.isSaving,
        "isError" to state.isError,
        "isSuccess" to state.isSuccess
    ).filter { it.second }.map { it.first }

    val isInvalidState = trueFlags.size > 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Boolean Explosion Bug",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Watch how a real bug creates an impossible state",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // State flags visualization
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateChip(label = "isLoading", value = state.isLoading)
            StateChip(label = "isSaving", value = state.isSaving)
            StateChip(label = "isError", value = state.isError)
            StateChip(label = "isSuccess", value = state.isSuccess)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "2⁴ = 16 combinations possible · Only ~5 are valid",
            style = MaterialTheme.typography.labelSmall,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bug warning when in invalid state
        if (isInvalidState) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFCDD2)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🐛 BUG DETECTED!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${trueFlags.joinToString(" + ")} are ALL true!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB71C1C),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Developer forgot to clear isError when save succeeded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB71C1C),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main content card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isInvalidState)
                    Color(0xFFFFEBEE)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // UI based on state - notice the priority/ordering problem
                when {
                    state.isSaving -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Saving profile...")
                    }
                    state.isError && state.isSuccess -> {
                        // IMPOSSIBLE STATE - but which do we show?
                        Text(
                            text = "✓ Saved! ...but also ❌ Error?",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFB71C1C),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "UI doesn't know what to display!",
                            style = MaterialTheme.typography.bodySmall,
                            color = BadColor
                        )
                    }
                    state.isError -> {
                        Text(
                            text = "❌ ${state.errorMessage}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                    state.isSuccess -> {
                        Text(
                            text = "✓ Profile saved!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    else -> {
                        Text(
                            text = "Profile: ${state.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = state.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How to trigger the bug:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Tap 'Save Profile' → fails with error\n" +
                           "2. Tap 'Save Profile' again → succeeds\n" +
                           "3. Bug! isError was never cleared",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Button(
            onClick = { scope.launch { viewModel.save() } },
            enabled = !state.isSaving
        ) {
            Text("Save Profile")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.reset() }
        ) {
            Text("Reset Demo")
        }
    }
}

@Composable
private fun StateChip(
    label: String,
    value: Boolean
) {
    val backgroundColor = if (value) {
        Color(0xFF4CAF50) // Green for true
    } else {
        Color(0xFF757575) // Gray for false
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
