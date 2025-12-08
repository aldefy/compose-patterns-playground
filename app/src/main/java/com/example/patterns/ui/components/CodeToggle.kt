package com.example.patterns.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.BadColor
import com.example.patterns.ui.theme.GoodColor

/**
 * Toggle component for switching between "Bad" and "Good" code examples.
 *
 * WHY THIS COMPONENT?
 * ===================
 * Workshop exercises need to show both the anti-pattern (bad) and the
 * correct implementation (good). This toggle makes it easy to switch
 * between them and visually distinguishes which version is being shown.
 *
 * USAGE:
 * ```kotlin
 * CodeToggle(
 *     showingBad = showBadVersion,
 *     onToggle = { showBadVersion = it },
 *     badContent = { BrokenImplementation() },
 *     goodContent = { FixedImplementation() }
 * )
 * ```
 */
@Composable
fun CodeToggle(
    showingBad: Boolean,
    onToggle: (Boolean) -> Unit,
    badContent: @Composable () -> Unit,
    goodContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    badLabel: String = "Show Bug",
    goodLabel: String = "Show Fix"
) {
    Column(modifier = modifier) {
        // Toggle buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onToggle(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showingBad) BadColor else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (showingBad) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(badLabel)
            }

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Button(
                onClick = { onToggle(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showingBad) GoodColor else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!showingBad) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(goodLabel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status indicator
        val backgroundColor by animateColorAsState(
            targetValue = if (showingBad) BadColor.copy(alpha = 0.1f) else GoodColor.copy(alpha = 0.1f),
            label = "status_bg"
        )
        val textColor by animateColorAsState(
            targetValue = if (showingBad) BadColor else GoodColor,
            label = "status_text"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showingBad) "Showing: Anti-Pattern (Bad)" else "Showing: Correct Implementation (Good)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated content switch
        AnimatedContent(
            targetState = showingBad,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "code_toggle"
        ) { isBad ->
            if (isBad) {
                badContent()
            } else {
                goodContent()
            }
        }
    }
}

/**
 * Simple composable wrapper to run an exercise and control showing bad vs good versions.
 */
@Composable
fun ExerciseToggle(
    title: String,
    badContent: @Composable () -> Unit,
    goodContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var showingBad by remember { mutableStateOf(true) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        CodeToggle(
            showingBad = showingBad,
            onToggle = { showingBad = it },
            badContent = badContent,
            goodContent = goodContent
        )
    }
}
