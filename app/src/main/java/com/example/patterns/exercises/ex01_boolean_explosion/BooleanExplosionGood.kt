package com.example.patterns.exercises.ex01_boolean_explosion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// ============================================================================
// SOLUTION: Sealed Interface State
// ============================================================================
//
// THE FIX:
// Use a sealed interface to model ONLY valid states:
// 1. EXHAUSTIVE: Compiler forces handling of all cases
// 2. NO IMPOSSIBLE STATES: Each state is a distinct type
// 3. EASY TO REASON: State name tells you exactly what's happening
// 4. ASSOCIATED DATA: Each state carries only the data it needs
//
// Instead of 2^n boolean combinations, we have exactly N valid states!
// ============================================================================

/**
 * Form data for the profile editor
 */
data class ProfileForm(
    val name: String = "",
    val email: String = ""
)

/**
 * GOOD: Sealed interface representing only valid states
 *
 * Benefits:
 * - IMPOSSIBLE states are literally impossible to create
 * - Each state carries exactly the data it needs
 * - when() expression requires handling all cases
 * - Adding a new state forces updating all handling code
 */
sealed interface ProfileScreenState {
    /**
     * Initial loading state when fetching profile from server
     */
    data object Loading : ProfileScreenState

    /**
     * User is editing the profile form
     */
    data class Editing(val form: ProfileForm) : ProfileScreenState

    /**
     * Profile is being saved to server
     * Keeps the form data so we can show what's being saved
     */
    data class Saving(val form: ProfileForm) : ProfileScreenState

    /**
     * Profile saved successfully
     */
    data class Success(val message: String) : ProfileScreenState

    /**
     * An error occurred
     * Optionally keeps form data so user can retry
     */
    data class Error(
        val reason: String,
        val form: ProfileForm? = null
    ) : ProfileScreenState
}

/**
 * GOOD: ViewModel using sealed interface state
 *
 * Notice how each transition is simple and safe:
 * - No boolean flags to manage
 * - Each state transition is explicit and clear
 * - Impossible to create invalid states
 */
class BooleanExplosionViewModelGood {
    // Start in Editing state so user can immediately interact
    var state: ProfileScreenState by mutableStateOf(
        ProfileScreenState.Editing(ProfileForm(name = "John Doe", email = "john@example.com"))
    )
        private set

    fun updateForm(form: ProfileForm) {
        // Only allow editing when in Editing state
        val currentState = state
        if (currentState is ProfileScreenState.Editing) {
            state = ProfileScreenState.Editing(form)
        }
    }

    fun updateName(name: String) {
        val currentState = state
        if (currentState is ProfileScreenState.Editing) {
            state = ProfileScreenState.Editing(
                currentState.form.copy(name = name)
            )
        }
    }

    fun updateEmail(email: String) {
        val currentState = state
        if (currentState is ProfileScreenState.Editing) {
            state = ProfileScreenState.Editing(
                currentState.form.copy(email = email)
            )
        }
    }

    // GOOD: Clean state transition - just set the new state
    suspend fun load() {
        state = ProfileScreenState.Loading

        delay(1000) // Simulate network

        // Transition to editing with loaded data
        state = ProfileScreenState.Editing(
            ProfileForm(
                name = "John Doe",
                email = "john@example.com"
            )
        )
    }

    // GOOD: Each transition is clean and atomic
    suspend fun save() {
        val currentState = state

        // Can only save from Editing state
        if (currentState !is ProfileScreenState.Editing) return

        // Transition to saving (keeping form data)
        state = ProfileScreenState.Saving(currentState.form)

        delay(1500) // Simulate save

        // 50% chance of error for demo
        state = if (Math.random() > 0.5) {
            ProfileScreenState.Error(
                reason = "Failed to save profile",
                form = currentState.form // Keep form so user can retry
            )
        } else {
            ProfileScreenState.Success("Profile saved successfully!")
        }
    }

    fun retry() {
        val currentState = state
        if (currentState is ProfileScreenState.Error && currentState.form != null) {
            state = ProfileScreenState.Editing(currentState.form)
        }
    }

    fun dismiss() {
        val currentState = state
        when (currentState) {
            is ProfileScreenState.Success -> {
                // Go back to editing with empty form
                state = ProfileScreenState.Editing(ProfileForm())
            }
            is ProfileScreenState.Error -> {
                if (currentState.form != null) {
                    state = ProfileScreenState.Editing(currentState.form)
                }
            }
            else -> { /* Do nothing for other states */ }
        }
    }
}

/**
 * GOOD: UI using exhaustive when expression
 *
 * Benefits:
 * - Compiler ensures ALL states are handled
 * - Each branch is clear about what state it handles
 * - No ambiguity about priority (no overlapping conditions)
 * - Adding a new state in the sealed interface will cause a compile error
 *   until this when expression is updated
 */
@Composable
fun BooleanExplosionGoodScreen(
    modifier: Modifier = Modifier
) {
    val viewModel = remember { BooleanExplosionViewModelGood() }
    val state = viewModel.state
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sealed Interface Solution",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoodColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show current state type for debugging
        StateTypeInfo(state)

        Spacer(modifier = Modifier.height(16.dp))

        // GOOD: Exhaustive when expression
        // If we add a new state to the sealed interface,
        // the compiler will force us to handle it here!
        when (state) {
            is ProfileScreenState.Loading -> {
                CircularProgressIndicator()
                Text("Loading profile...")
            }

            is ProfileScreenState.Editing -> {
                ProfileFormGood(
                    form = state.form,
                    onNameChange = viewModel::updateName,
                    onEmailChange = viewModel::updateEmail,
                    onSave = { scope.launch { viewModel.save() } }
                )
            }

            is ProfileScreenState.Saving -> {
                CircularProgressIndicator()
                Text("Saving ${state.form.name}...")
            }

            is ProfileScreenState.Success -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.dismiss() }) {
                    Text("Continue")
                }
            }

            is ProfileScreenState.Error -> {
                Text(
                    text = state.reason,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.form != null) {
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                    Button(onClick = { viewModel.dismiss() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun StateTypeInfo(state: ProfileScreenState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Current State:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val stateTypeName = when (state) {
            is ProfileScreenState.Loading -> "Loading"
            is ProfileScreenState.Editing -> "Editing"
            is ProfileScreenState.Saving -> "Saving"
            is ProfileScreenState.Success -> "Success"
            is ProfileScreenState.Error -> "Error"
        }

        // Single state chip - always green because there's only ONE valid state
        Box(
            modifier = Modifier
                .background(
                    color = GoodColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = stateTypeName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Only 5 valid states exist",
            style = MaterialTheme.typography.titleSmall,
            color = GoodColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Impossible to be in 2 states at once!",
            style = MaterialTheme.typography.bodyMedium,
            color = GoodColor
        )
    }
}

@Composable
private fun ProfileFormGood(
    form: ProfileForm,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        OutlinedTextField(
            value = form.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSave()
                }
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onSave) {
                Text("Save Profile")
            }
        }
    }
}
