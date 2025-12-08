package com.example.patterns.exercises.ex03_antipatterns.ap08_remember_wrong_keys

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.patterns.ui.theme.BadColor

// ============================================================================
// ANTI-PATTERN 08: Wrong Keys in remember
// ============================================================================
//
// THE BUG:
// Using wrong keys (or missing keys) with remember causes:
// - Stale computations that don't update when they should
// - Or excessive recalculations that happen too often
//
// COMMON MISTAKES:
// 1. Missing key → stale data
// 2. Too many keys → unnecessary recalculation
// 3. Wrong key type → always recalculating
// ============================================================================

data class User(val id: Int, val name: String, val email: String)

/**
 * BROKEN: Missing key causes stale computation
 */
@Composable
fun RememberWrongKeysBroken(
    modifier: Modifier = Modifier
) {
    var userId by remember { mutableIntStateOf(1) }
    var user by remember {
        mutableStateOf(User(1, "Alice", "alice@example.com"))
    }

    // BAD: No key! This only computes once and becomes stale
    val userSummary = remember {
        "User #${user.id}: ${user.name} <${user.email}>"
    }

    // BAD: Using the whole object as key when only part changes
    // This recalculates even when email changes but we only need name
    val greeting = remember(user) {
        "Hello, ${user.name}!"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Wrong remember Keys",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BadColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BadColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "User Summary (stale!):",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = userSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BadColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Actual User:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "User #${user.id}: ${user.name} <${user.email}>",
                    style = MaterialTheme.typography.bodyLarge
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
                    text = "Notice the problem?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        The "User Summary" never updates!

                        remember {} with no keys only computes once.
                        The value becomes stale as user data changes.

                        This is a VERY common bug:
                        • Formatted strings that never update
                        • Computed values stuck on initial data
                        • "But I changed the data, why doesn't it show?"
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
