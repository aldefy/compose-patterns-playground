package com.example.patterns.exercises.ex03_antipatterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor

/**
 * Exercise 03: Anti-Patterns Catalog
 *
 * This exercise presents 12 common anti-patterns in Compose state management.
 * Each anti-pattern has:
 * - A "Broken" implementation showing the bug
 * - A "Fixed" implementation showing the solution
 * - Explanation of why the bug occurs and how to avoid it
 *
 * ANTI-PATTERNS COVERED:
 *
 * AP01: LaunchedEffect Self-Cancellation Trap
 * AP02: derivedStateOf Misuse
 * AP03: Unstable Lambda Captures
 * AP04: remember in a Loop (wrong key)
 * AP05: Side Effects During Composition
 * AP06: Wrong Flow Collection
 * AP07: Reading State Too High in the Tree
 * AP08: Wrong Keys in remember
 * AP09: Mutating Shared State Directly
 * AP10: Confusing Events with State
 * AP11: Creating ViewModel in Composables
 * AP12: Side Effects in State Transitions
 */
@Composable
fun AntiPatternsExercise(
    onNavigateToAntiPattern: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Exercise 03: Anti-Patterns",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "12 common pitfalls in Compose state management. " +
                    "Each shows the bug and the fix.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        val antiPatterns = listOf(
            AntiPatternInfo(
                id = "ap01",
                title = "LaunchedEffect Trap",
                description = "Self-cancellation when changing key inside effect"
            ),
            AntiPatternInfo(
                id = "ap02",
                title = "derivedStateOf Misuse",
                description = "Using it when not needed, or missing it when needed"
            ),
            AntiPatternInfo(
                id = "ap03",
                title = "Unstable Lambda",
                description = "Lambdas causing unnecessary recomposition"
            ),
            AntiPatternInfo(
                id = "ap04",
                title = "State in Loop",
                description = "remember without key in iterations"
            ),
            AntiPatternInfo(
                id = "ap05",
                title = "Side Effects in Composition",
                description = "Executing effects during composition"
            ),
            AntiPatternInfo(
                id = "ap06",
                title = "Flow Collection",
                description = "Wrong way to collect flows in Compose"
            ),
            AntiPatternInfo(
                id = "ap07",
                title = "State Read Too High",
                description = "Reading state too high causes excess recomposition"
            ),
            AntiPatternInfo(
                id = "ap08",
                title = "Wrong remember Keys",
                description = "Missing or incorrect keys in remember"
            ),
            AntiPatternInfo(
                id = "ap09",
                title = "Shared State Mutation",
                description = "Mutating lists without triggering recomposition"
            ),
            AntiPatternInfo(
                id = "ap10",
                title = "Events vs State",
                description = "Using state for one-time events"
            ),
            AntiPatternInfo(
                id = "ap11",
                title = "Fake ViewModel",
                description = "Creating ViewModel-like objects in composables"
            ),
            AntiPatternInfo(
                id = "ap12",
                title = "Effects in Transitions",
                description = "Executing side effects in state transitions"
            )
        )

        antiPatterns.forEachIndexed { index, ap ->
            AntiPatternCard(
                number = index + 1,
                info = ap,
                onClick = { onNavigateToAntiPattern(ap.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class AntiPatternInfo(
    val id: String,
    val title: String,
    val description: String
)

@Composable
private fun AntiPatternCard(
    number: Int,
    info: AntiPatternInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BugReport,
                contentDescription = null,
                tint = BadColor
            )

            Text(
                text = "AP${number.toString().padStart(2, '0')}: ${info.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = info.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
