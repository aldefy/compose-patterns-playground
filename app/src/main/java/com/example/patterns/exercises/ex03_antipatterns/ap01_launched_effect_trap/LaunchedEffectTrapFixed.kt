package com.example.patterns.exercises.ex03_antipatterns.ap01_launched_effect_trap

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter

// ============================================================================
// FIX: Use snapshotFlow to observe state changes WITHOUT self-cancellation
// ============================================================================
//
// THE PROPER SOLUTION:
// Use snapshotFlow { state } inside LaunchedEffect(Unit).
// snapshotFlow converts Compose state reads into a Flow.
// When the state changes, it emits - but does NOT cancel the coroutine!
//
// Pattern:
//   LaunchedEffect(Unit) {
//       snapshotFlow { wasAutoFilled }
//           .filter { it }
//           .collect {
//               wasAutoFilled = false  // Safe! Doesn't cancel this coroutine
//               delay(300)             // Completes!
//               onVerifyOTP(otp)       // Executes!
//           }
//   }
//
// WHY THIS WORKS:
// - LaunchedEffect(Unit) never restarts (key never changes)
// - snapshotFlow observes state reactively
// - Changing wasAutoFilled just emits a new value, doesn't cancel collect
//
// ============================================================================

/**
 * FIXED: Use snapshotFlow to avoid self-cancellation
 *
 * snapshotFlow converts Compose state into a Flow that doesn't
 * cancel when the observed value changes.
 */
@Composable
fun LaunchedEffectTrapFixed(
    modifier: Modifier = Modifier
) {
    var wasAutoFilled by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }

    // FIX: Use snapshotFlow inside LaunchedEffect(Unit)
    LaunchedEffect(Unit) {
        snapshotFlow { wasAutoFilled }
            .filter { it }  // Only proceed when true
            .collect {
                // Safe to reset immediately - this is a Flow, not a keyed effect!
                wasAutoFilled = false

                // Small delay to feel natural - COMPLETES!
                delay(300)

                // CRITICAL WORK - NOW IT EXECUTES!
                Log.d("OTP", "Calling onVerifyOTP - this prints!")
                verificationResult = "Success! OTP Verified"
                isVerifying = false
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "snapshotFlow Fixed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoodColor
            )
        }

        // Demo Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GoodColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "OTP Auto-Submit Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Status display
                    when {
                        verificationResult != null -> {
                            Text(
                                text = "✓ $verificationResult",
                                style = MaterialTheme.typography.headlineSmall,
                                color = GoodColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        isVerifying -> {
                            CircularProgressIndicator()
                            Text(
                                text = "Verifying OTP...",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Text(
                                text = "Simulate SMS auto-fill",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Text(
                        text = "wasAutoFilled: $wasAutoFilled",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            // Simulate SMS auto-filling the OTP
                            verificationResult = null
                            isVerifying = true
                            wasAutoFilled = true
                        },
                        enabled = !isVerifying
                    ) {
                        Text("Simulate SMS Auto-Fill")
                    }

                    Button(
                        onClick = {
                            wasAutoFilled = false
                            isVerifying = false
                            verificationResult = null
                        }
                    ) {
                        Text("Reset")
                    }
                }
            }
        }

        // Fix Explanation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GoodColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "✓ Why snapshotFlow works",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
1. LaunchedEffect(Unit) starts ONCE
2. snapshotFlow observes wasAutoFilled
3. When wasAutoFilled = true, flow emits
4. wasAutoFilled = false (just emits again, no cancellation!)
5. delay(300) completes normally
6. onVerifyOTP() EXECUTES!

The coroutine never restarts because the key is Unit.
snapshotFlow just observes - it doesn't control lifecycle.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Code snippet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Fixed Code:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
LaunchedEffect(Unit) {  // Key is Unit - never restarts!
    snapshotFlow { wasAutoFilled }
        .filter { it }
        .collect {
            // Safe! This is a Flow, not a keyed effect
            wasAutoFilled = false

            delay(300)        // ← COMPLETES!
            onVerifyOTP(otp)  // ← EXECUTES!
        }
}
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Key insight
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Key Insight",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
LaunchedEffect(key) restarts when key changes.

snapshotFlow { state } inside LaunchedEffect(Unit) observes state changes WITHOUT restarting the coroutine.

Use snapshotFlow when you need to:
• React to state changes
• But NOT cancel ongoing work
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Comparison
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Broken vs Fixed",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
// 💀 BROKEN - key change cancels effect
LaunchedEffect(wasAutoFilled) {
    wasAutoFilled = false  // CANCELS!
    delay(300)             // Never runs
}

// ✅ FIXED - snapshotFlow observes safely
LaunchedEffect(Unit) {
    snapshotFlow { wasAutoFilled }
        .filter { it }
        .collect {
            wasAutoFilled = false  // Just emits
            delay(300)             // Runs!
        }
}
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
