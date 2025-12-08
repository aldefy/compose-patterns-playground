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
import com.example.patterns.ui.theme.BadColor
import kotlinx.coroutines.delay

// ============================================================================
// ANTI-PATTERN 01: LaunchedEffect Self-Cancellation Trap
// ============================================================================
//
// THE BUG:
// When you change a LaunchedEffect's key INSIDE the effect BEFORE a
// suspension point, the effect cancels - code after never runs!
//
// Real-world example: OTP auto-submit
//   LaunchedEffect(wasAutoFilled) {
//       wasAutoFilled = false   // Reset flag - CHANGES KEY!
//       delay(300)              // Suspension - CANCELLED HERE!
//       onVerifyOTP(otp)        // NEVER EXECUTES!
//   }
//
// ============================================================================

/**
 * BROKEN: LaunchedEffect that cancels itself
 *
 * This OTP auto-submit demo shows the bug:
 * - SMS fills OTP → wasAutoFilled = true
 * - Effect starts, resets wasAutoFilled = false BEFORE delay
 * - Effect cancels, verification never happens!
 */
@Composable
fun LaunchedEffectTrapBroken(
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
                text = "LaunchedEffect Self-Cancellation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BadColor
            )
        }

        // Demo Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = BadColor.copy(alpha = 0.1f)
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
                                text = verificationResult!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (verificationResult!!.contains("Success"))
                                    MaterialTheme.colorScheme.primary
                                else
                                    BadColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        isVerifying -> {
                            CircularProgressIndicator()
                            Text(
                                text = "Verifying OTP... (stuck forever!)",
                                color = BadColor,
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

        // THE BUG: Resetting the key BEFORE the suspension point
        item {
            LaunchedEffect(wasAutoFilled) {
                if (!wasAutoFilled) return@LaunchedEffect

                // BUG: Reset flag BEFORE delay - CHANGES KEY!
                wasAutoFilled = false

                // Small delay to feel natural - CANCELLED HERE!
                delay(300)

                // CRITICAL WORK - NEVER EXECUTES!
                Log.d("OTP", "Calling onVerifyOTP - this never prints!")
                verificationResult = "Success! OTP Verified"
                isVerifying = false
            }
        }

        // Bug Explanation
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
                        text = "🐛 Why is it stuck?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BadColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
1. SMS arrives: wasAutoFilled = true
2. LaunchedEffect(wasAutoFilled) starts
3. wasAutoFilled = false → KEY CHANGES!
4. Effect cancels before delay(300) completes
5. onVerifyOTP() NEVER CALLED!
6. Spinner stuck forever (isVerifying = false never runs)

The developer wanted to reset the flag for next time, but placing it BEFORE the delay cancels the effect.
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
                        text = "Broken Code:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BadColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
LaunchedEffect(wasAutoFilled) {
    if (!wasAutoFilled) return

    // BUG: Reset BEFORE delay!
    wasAutoFilled = false  // ← KEY CHANGES!

    delay(300)  // ← CANCELLED!

    // NEVER EXECUTES!
    onVerifyOTP(otp)
    isVerifying = false
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
