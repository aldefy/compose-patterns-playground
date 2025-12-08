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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay

// ============================================================================
// FIX: Reset the flag AFTER all critical work is done
// ============================================================================
//
// THE SOLUTION:
// Move the flag reset to AFTER the suspension point and critical work.
// Or better: reset it elsewhere (on error, on clear, etc.) - not inside the effect.
//
// Pattern:
//   LaunchedEffect(wasAutoFilled) {
//       if (!wasAutoFilled) return
//
//       delay(300)           // Suspension - completes!
//       onVerifyOTP(otp)     // Critical work - executes!
//       wasAutoFilled = false // Reset AFTER (or don't reset here at all)
//   }
//
// ============================================================================

/**
 * FIXED: Reset the flag AFTER suspension and critical work
 */
@Composable
fun LaunchedEffectTrapFixed(
    modifier: Modifier = Modifier
) {
    var wasAutoFilled by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "LaunchedEffect Fixed",
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

        // FIX: Reset the flag AFTER delay and critical work
        item {
            LaunchedEffect(wasAutoFilled) {
                if (!wasAutoFilled) return@LaunchedEffect

                // Small delay to feel natural - COMPLETES!
                delay(300)

                // CRITICAL WORK - NOW IT EXECUTES!
                Log.d("OTP", "Calling onVerifyOTP - this prints!")
                verificationResult = "Success! OTP Verified"
                isVerifying = false

                // FIX: Reset AFTER critical work (or reset elsewhere)
                wasAutoFilled = false
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
                        text = "✓ Why does this work?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
1. SMS arrives: wasAutoFilled = true
2. LaunchedEffect(wasAutoFilled) starts
3. delay(300) completes normally
4. onVerifyOTP() EXECUTES!
5. isVerifying = false (spinner stops)
6. wasAutoFilled = false (reset AFTER work done)

The key doesn't change until AFTER all critical work is complete.
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
LaunchedEffect(wasAutoFilled) {
    if (!wasAutoFilled) return

    delay(300)  // ← COMPLETES!

    // NOW IT EXECUTES!
    onVerifyOTP(otp)
    isVerifying = false

    // Reset AFTER work done
    wasAutoFilled = false  // ← Safe here!
}
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Rule of thumb
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
                        text = "Rule of Thumb",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Never change the LaunchedEffect key BEFORE a suspension point.\n\nReset flags AFTER critical work, or reset them elsewhere (on error, on clear, etc.)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
