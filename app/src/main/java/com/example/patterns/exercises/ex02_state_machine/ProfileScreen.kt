package com.example.patterns.exercises.ex02_state_machine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patterns.core.effects.CommonEffect
import com.example.patterns.ui.theme.GoodColor

// ============================================================================
// PROFILE SCREEN: State Machine-Driven UI
// ============================================================================
//
// This screen demonstrates the SOLUTION to boolean explosion:
// - ONE sealed state at a time (impossible to have conflicting states)
// - Exhaustive 'when' expression (compiler ensures all states handled)
// - Clear state transitions visible to the user
// ============================================================================

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.state
    val transitionHistory = viewModel.transitionHistory
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI effects (snackbars, etc.)
    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is CommonEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CommonEffect.Navigate -> {}
                is CommonEffect.NavigateBack -> {}
                is CommonEffect.TrackAnalytics -> {}
                is CommonEffect.Haptic -> {}
                is CommonEffect.ShowToast -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sealed State Machine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoodColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Only ONE state at a time - impossible to have conflicts!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current state chip - prominent display
            StateChip(state = state)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "5 possible states · All valid · No conflicts",
                style = MaterialTheme.typography.labelSmall,
                color = GoodColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // State transition history visualization
            TransitionHistoryCard(
                transitionHistory = transitionHistory,
                onClearHistory = { viewModel.clearHistory() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // EXHAUSTIVE STATE HANDLING
            // Compiler ensures all states are handled!
            when (state) {
                is ProfileState.Loading -> {
                    LoadingContent()
                }

                is ProfileState.Viewing -> {
                    ViewingContent(
                        profile = state.profile,
                        onEditClick = { viewModel.onEvent(ProfileEvent.EditClicked) }
                    )
                }

                is ProfileState.Editing -> {
                    EditingContent(
                        form = state.form,
                        onNameChange = { viewModel.onEvent(ProfileEvent.NameChanged(it)) },
                        onEmailChange = { viewModel.onEvent(ProfileEvent.EmailChanged(it)) },
                        onSaveClick = { viewModel.onEvent(ProfileEvent.SaveClicked) },
                        onCancelClick = { viewModel.onEvent(ProfileEvent.CancelClicked) }
                    )
                }

                is ProfileState.Saving -> {
                    SavingContent(form = state.form)
                }

                is ProfileState.Error -> {
                    ErrorContent(
                        message = state.message,
                        canRetry = state.retryEvent != null,
                        onRetryClick = { viewModel.onEvent(ProfileEvent.RetryClicked) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Explanation card
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
                        text = "Why this is better:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoodColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• State is a sealed interface\n" +
                               "• Can only be ONE type at a time\n" +
                               "• Loading + Error? IMPOSSIBLE!\n" +
                               "• Compiler enforces exhaustive handling",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StateChip(state: ProfileState) {
    val stateName = when (state) {
        is ProfileState.Loading -> "Loading"
        is ProfileState.Viewing -> "Viewing"
        is ProfileState.Editing -> "Editing"
        is ProfileState.Saving -> "Saving"
        is ProfileState.Error -> "Error"
    }

    Box(
        modifier = Modifier
            .background(
                color = GoodColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = stateName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LoadingContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading profile...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ViewingContent(
    profile: Profile,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit profile"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap the edit icon to change state → Editing",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditingContent(
    form: ProfileFormData,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                isError = form.nameError != null,
                supportingText = form.nameError?.let { { Text(it) } },
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
                isError = form.emailError != null,
                supportingText = form.emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (form.isValid) onSaveClick()
                    }
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(onClick = onCancelClick) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSaveClick,
                    enabled = form.isValid
                ) {
                    Text("Save")
                }
            }

            Text(
                text = "Save → Saving state · Cancel → Viewing state",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SavingContent(form: ProfileFormData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Saving profile...",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Updating ${form.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Will transition to Viewing or Error",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            if (canRetry) {
                Button(onClick = onRetryClick) {
                    Text("Retry")
                }
            }

            Text(
                text = "Retry → Loading or Saving state",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TransitionHistoryCard(
    transitionHistory: List<StateTransition>,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "State Transition Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (transitionHistory.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (transitionHistory.isEmpty()) {
                Text(
                    text = "Interact with the screen to see state transitions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Vertical list of transitions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    transitionHistory.takeLast(8).forEachIndexed { index, transition ->
                        TransitionRow(
                            transition = transition,
                            isLatest = index == transitionHistory.takeLast(8).lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionRow(
    transition: StateTransition,
    isLatest: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // From state
        TransitionStateChip(
            stateName = transition.fromState,
            isCurrentState = false
        )

        // Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        // Event name
        Text(
            text = transition.event,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        // To state
        TransitionStateChip(
            stateName = transition.toState,
            isCurrentState = isLatest
        )
    }
}

@Composable
private fun TransitionStateChip(
    stateName: String,
    isCurrentState: Boolean
) {
    val backgroundColor = if (isCurrentState) {
        GoodColor
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val textColor = if (isCurrentState) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = stateName,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isCurrentState) FontWeight.Bold else FontWeight.Normal
        )
    }
}

