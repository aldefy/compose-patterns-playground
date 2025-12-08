package com.example.patterns.exercises.ex01_boolean_explosion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.patterns.ui.components.CodeToggle

/**
 * Exercise 01: Boolean Explosion
 *
 * This exercise demonstrates one of the most common anti-patterns in UI state management:
 * using multiple boolean flags to represent state.
 *
 * THE PROBLEM (Bad Version):
 * - Uses 4 booleans: isLoading, isSaving, isError, isSuccess
 * - Creates 2^4 = 16 possible combinations
 * - Only ~4 combinations are actually valid states
 * - Easy to forget to reset flags, leading to bugs
 * - No compiler help to ensure all states are handled
 *
 * THE SOLUTION (Good Version):
 * - Uses a sealed interface with 5 distinct states
 * - Each state is a separate type with its own data
 * - Impossible to be in multiple states at once
 * - Compiler forces handling of all cases with 'when'
 * - Adding new states causes compile errors until handled
 *
 * LEARNING OBJECTIVES:
 * 1. Recognize the boolean explosion anti-pattern
 * 2. Model state as exclusive types using sealed interfaces
 * 3. Leverage Kotlin's exhaustive 'when' for compile-time safety
 * 4. Associate relevant data with each state type
 */
@Composable
fun BooleanExplosionExercise(
    modifier: Modifier = Modifier
) {
    var showingBad by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Exercise 01: Boolean Explosion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Learn why multiple boolean flags create impossible states " +
                    "and how sealed interfaces solve this problem.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        CodeToggle(
            showingBad = showingBad,
            onToggle = { showingBad = it },
            badContent = {
                BooleanExplosionBadScreen()
            },
            goodContent = {
                BooleanExplosionGoodScreen()
            },
            badLabel = "Boolean Flags",
            goodLabel = "Sealed Interface"
        )
    }
}
