package com.example.patterns.exercises.ex03_antipatterns.ap08_remember_wrong_keys

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// FIX: Use Correct Keys with remember
// ============================================================================
//
// KEY RULES:
// 1. Include ALL values that affect the computation
// 2. Include ONLY values that affect the computation
// 3. Use the specific property, not the whole object when possible
// 4. For expensive computations, consider derivedStateOf
// ============================================================================

/**
 * FIXED: Correct keys in remember
 */
@Composable
fun RememberWrongKeysFixed(
    modifier: Modifier = Modifier
) {
    var user by remember {
        mutableStateOf(User(1, "Alice", "alice@example.com"))
    }

    // GOOD: Include all relevant keys
    val userSummary = remember(user.id, user.name, user.email) {
        "User #${user.id}: ${user.name} <${user.email}>"
    }

    // BETTER: Use the whole object since we need all fields anyway
    val userSummaryAlt = remember(user) {
        "User #${user.id}: ${user.name} <${user.email}>"
    }

    // GOOD: Use specific key when only using that property
    val greeting = remember(user.name) {
        "Hello, ${user.name}!"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Correct remember Keys",
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
                    text = "User Summary (updates!):",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = userSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoodColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Greeting (updates on name change):",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoodColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = user.name,
                    onValueChange = { user = user.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                OutlinedTextField(
                    value = user.email,
                    onValueChange = { user = user.copy(email = it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    user = User(
                        id = user.id + 1,
                        name = "User ${user.id + 1}",
                        email = "user${user.id + 1}@example.com"
                    )
                }) {
                    Text("Load New User")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Key Guidelines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        remember(key1, key2, ...) { computation }

                        RULES:
                        1. Keys should be ALL inputs to the computation
                        2. When key changes → recalculate
                        3. Same keys → use cached value

                        For greeting, we only use user.name, so:
                        • remember(user.name) - recalc only when name changes
                        • remember(user) - would recalc on email change too (wasteful)

                        For summary, we use all fields:
                        • remember(user) - correct, covers all
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
