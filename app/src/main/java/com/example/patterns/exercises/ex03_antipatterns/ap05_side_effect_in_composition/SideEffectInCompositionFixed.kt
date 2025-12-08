package com.example.patterns.exercises.ex03_antipatterns.ap05_side_effect_in_composition

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Use proper effect handlers
// ============================================================================
//
// SOLUTIONS:
// 1. LaunchedEffect(key) - for suspend functions, runs when key changes
// 2. SideEffect - for non-suspend effects that must run after composition
// 3. DisposableEffect - for effects that need cleanup
// 4. rememberCoroutineScope - for effects triggered by user events
//
// Effects run AFTER composition succeeds, guaranteeing:
// - They only run once per successful composition
// - They can be properly cancelled
// - They have a predictable lifecycle
// ============================================================================

object EffectCounter {
    var launchedEffectCount = 0
        private set
    var sideEffectCount = 0
        private set

    fun incrementLaunched() {
        launchedEffectCount++
        Log.d("SideEffectFixed", "LaunchedEffect ran! Count: $launchedEffectCount")
    }

    fun incrementSide() {
        sideEffectCount++
        Log.d("SideEffectFixed", "SideEffect ran! Count: $sideEffectCount")
    }

    fun reset() {
        launchedEffectCount = 0
        sideEffectCount = 0
    }
}

/**
 * FIXED: Using proper effect handlers
 */
@Composable
fun SideEffectInCompositionFixed(
    modifier: Modifier = Modifier
) {
    var recomposeCounter by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var triggerEffect by remember { mutableIntStateOf(0) }

    // GOOD: LaunchedEffect runs once when key changes
    // This runs only when triggerEffect changes, not on every recomposition
    LaunchedEffect(triggerEffect) {
        if (triggerEffect > 0) { // Skip initial
            EffectCounter.incrementLaunched()
        }
    }

    // GOOD: SideEffect runs AFTER each successful composition
    // Use this for fire-and-forget effects that don't need suspension
    SideEffect {
        EffectCounter.incrementSide()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Proper Effect Handlers",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LaunchedEffect: ${EffectCounter.launchedEffectCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoodColor
                )

                Text(
                    text = "SideEffect: ${EffectCounter.sideEffectCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { recomposeCounter++ }) {
                    Text("Trigger Recomposition ($recomposeCounter)")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(onClick = { triggerEffect++ }) {
                    Text("Trigger LaunchedEffect ($triggerEffect)")
                }

                Spacer(modifier = Modifier.height(4.dp))

                val focusManager = LocalFocusManager.current
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Type here") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(onClick = { EffectCounter.reset() }) {
                    Text("Reset Counters")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Effect Handlers Explained",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        LaunchedEffect(key):
                        • Runs when key changes
                        • Supports suspend functions
                        • Cancelled when leaving composition
                        • Use for: API calls, one-time effects

                        SideEffect:
                        • Runs after EVERY successful composition
                        • For non-suspend effects
                        • Use for: Logging, analytics (carefully!)

                        DisposableEffect:
                        • Has an onDispose callback
                        • Use for: Listeners, callbacks

                        rememberCoroutineScope:
                        • For effects triggered by events
                        • Use for: Button clicks, gestures
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
