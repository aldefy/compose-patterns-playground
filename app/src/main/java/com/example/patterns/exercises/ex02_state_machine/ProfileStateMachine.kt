package com.example.patterns.exercises.ex02_state_machine

import com.example.patterns.core.effects.CommonEffect
import com.example.patterns.core.state.TransitionResult

// ============================================================================
// EXERCISE 02: Full State Machine Pattern
// ============================================================================
//
// This exercise demonstrates the complete state machine pattern with:
// 1. Sealed interface for state (only valid states can exist)
// 2. Sealed interface for events (all possible inputs are known)
// 3. Pure transition function (state + event → new state + effects)
// 4. Effects as data (side effects returned, not executed inline)
//
// KEY INSIGHT: The transition function is PURE
// - Same input always produces same output
// - No side effects executed during transition
// - Easy to test (no mocking needed!)
// - Easy to reason about (deterministic)
// ============================================================================

/**
 * Domain model for a user profile
 */
data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)

/**
 * Form data for editing a profile
 */
data class ProfileFormData(
    val name: String = "",
    val email: String = "",
    val nameError: String? = null,
    val emailError: String? = null
) {
    val isValid: Boolean
        get() = nameError == null && emailError == null && name.isNotBlank() && email.isNotBlank()
}

// ============================================================================
// STATE: What can the screen BE?
// ============================================================================

/**
 * All possible states of the profile editor screen.
 *
 * DESIGN PRINCIPLES:
 * - Each state is a distinct type (no boolean combinations)
 * - Each state carries exactly the data it needs
 * - States are named for WHAT THE SCREEN IS DOING
 */
sealed interface ProfileState {
    /**
     * Initial state - loading profile from server
     */
    data object Loading : ProfileState

    /**
     * Profile loaded successfully, user is viewing
     */
    data class Viewing(val profile: Profile) : ProfileState

    /**
     * User is editing the profile
     */
    data class Editing(
        val originalProfile: Profile,
        val form: ProfileFormData
    ) : ProfileState

    /**
     * Saving changes to server
     */
    data class Saving(
        val originalProfile: Profile,
        val form: ProfileFormData
    ) : ProfileState

    /**
     * Error occurred (loading or saving failed)
     */
    data class Error(
        val message: String,
        val retryEvent: ProfileEvent? = null
    ) : ProfileState
}

// ============================================================================
// EVENTS: What can HAPPEN?
// ============================================================================

/**
 * All possible events that can occur in the profile editor.
 *
 * DESIGN PRINCIPLES:
 * - Events are things that HAPPENED (past tense naming often helps)
 * - Events carry any data needed to handle them
 * - Events do NOT dictate what the new state should be
 *   (that's the transition function's job)
 */
sealed interface ProfileEvent {
    // User actions
    data object EditClicked : ProfileEvent
    data object SaveClicked : ProfileEvent
    data object CancelClicked : ProfileEvent
    data object RetryClicked : ProfileEvent
    data class NameChanged(val name: String) : ProfileEvent
    data class EmailChanged(val email: String) : ProfileEvent

    // System events (from side effects)
    data class ProfileLoaded(val profile: Profile) : ProfileEvent
    data class ProfileSaved(val profile: Profile) : ProfileEvent
    data class LoadFailed(val error: String) : ProfileEvent
    data class SaveFailed(val error: String) : ProfileEvent
}

// ============================================================================
// EFFECTS: What should HAPPEN NEXT?
// ============================================================================

/**
 * Side effects that can be triggered by state transitions.
 *
 * DESIGN PRINCIPLES:
 * - Effects are DATA describing what should happen
 * - Effects are NOT executed during the transition
 * - Effects will be handled by an EffectHandler
 * - This makes the transition function pure and testable!
 */
sealed interface ProfileEffect {
    /**
     * Load profile from server
     */
    data class LoadProfile(val userId: String) : ProfileEffect

    /**
     * Save profile to server
     */
    data class SaveProfile(val profile: Profile) : ProfileEffect

    /**
     * Show a common effect (snackbar, navigation, etc.)
     */
    data class Common(val effect: CommonEffect) : ProfileEffect
}

// ============================================================================
// TRANSITION: Pure function (State, Event) → (NewState, Effects)
// ============================================================================

/**
 * Pure transition function for the profile state machine.
 *
 * THIS IS THE HEART OF THE PATTERN:
 * - Takes current state and an event
 * - Returns new state and any effects to execute
 * - NO SIDE EFFECTS happen during this function
 * - Completely deterministic and testable
 *
 * @param state Current state of the state machine
 * @param event Event that occurred
 * @return TransitionResult containing new state and effects to execute
 */
fun profileTransition(
    state: ProfileState,
    event: ProfileEvent
): TransitionResult<ProfileState, ProfileEffect> {

    return when (state) {
        is ProfileState.Loading -> handleLoadingState(event)
        is ProfileState.Viewing -> handleViewingState(state, event)
        is ProfileState.Editing -> handleEditingState(state, event)
        is ProfileState.Saving -> handleSavingState(state, event)
        is ProfileState.Error -> handleErrorState(state, event)
    }
}

private fun handleLoadingState(
    event: ProfileEvent
): TransitionResult<ProfileState, ProfileEffect> {
    return when (event) {
        is ProfileEvent.ProfileLoaded -> TransitionResult(
            newState = ProfileState.Viewing(event.profile)
        )

        is ProfileEvent.LoadFailed -> TransitionResult(
            newState = ProfileState.Error(
                message = event.error,
                retryEvent = ProfileEvent.RetryClicked // Enable retry for load failures
            )
        )

        // Ignore other events while loading
        else -> TransitionResult(ProfileState.Loading)
    }
}

private fun handleViewingState(
    state: ProfileState.Viewing,
    event: ProfileEvent
): TransitionResult<ProfileState, ProfileEffect> {
    return when (event) {
        is ProfileEvent.EditClicked -> TransitionResult(
            newState = ProfileState.Editing(
                originalProfile = state.profile,
                form = ProfileFormData(
                    name = state.profile.name,
                    email = state.profile.email
                )
            )
        )

        // Ignore other events while viewing
        else -> TransitionResult(state)
    }
}

private fun handleEditingState(
    state: ProfileState.Editing,
    event: ProfileEvent
): TransitionResult<ProfileState, ProfileEffect> {
    return when (event) {
        is ProfileEvent.NameChanged -> {
            val nameError = validateName(event.name)
            TransitionResult(
                newState = state.copy(
                    form = state.form.copy(
                        name = event.name,
                        nameError = nameError
                    )
                )
            )
        }

        is ProfileEvent.EmailChanged -> {
            val emailError = validateEmail(event.email)
            TransitionResult(
                newState = state.copy(
                    form = state.form.copy(
                        email = event.email,
                        emailError = emailError
                    )
                )
            )
        }

        is ProfileEvent.SaveClicked -> {
            if (state.form.isValid) {
                val updatedProfile = state.originalProfile.copy(
                    name = state.form.name,
                    email = state.form.email
                )
                TransitionResult(
                    newState = ProfileState.Saving(
                        originalProfile = state.originalProfile,
                        form = state.form
                    ),
                    effects = listOf(ProfileEffect.SaveProfile(updatedProfile))
                )
            } else {
                // Show validation errors
                TransitionResult(
                    newState = state.copy(
                        form = state.form.copy(
                            nameError = validateName(state.form.name),
                            emailError = validateEmail(state.form.email)
                        )
                    ),
                    effects = listOf(
                        ProfileEffect.Common(
                            CommonEffect.ShowSnackbar("Please fix validation errors")
                        )
                    )
                )
            }
        }

        is ProfileEvent.CancelClicked -> TransitionResult(
            newState = ProfileState.Viewing(state.originalProfile)
        )

        // Ignore other events while editing
        else -> TransitionResult(state)
    }
}

private fun handleSavingState(
    state: ProfileState.Saving,
    event: ProfileEvent
): TransitionResult<ProfileState, ProfileEffect> {
    return when (event) {
        is ProfileEvent.ProfileSaved -> TransitionResult(
            newState = ProfileState.Viewing(event.profile),
            effects = listOf(
                ProfileEffect.Common(
                    CommonEffect.ShowSnackbar("Profile saved successfully!")
                )
            )
        )

        is ProfileEvent.SaveFailed -> TransitionResult(
            newState = ProfileState.Error(
                message = event.error,
                retryEvent = ProfileEvent.SaveClicked
            )
        )

        // Ignore other events while saving
        else -> TransitionResult(state)
    }
}

private fun handleErrorState(
    state: ProfileState.Error,
    event: ProfileEvent
): TransitionResult<ProfileState, ProfileEffect> {
    return when (event) {
        is ProfileEvent.RetryClicked -> {
            // If we have a retry event, use it
            if (state.retryEvent != null) {
                TransitionResult(
                    newState = ProfileState.Loading,
                    effects = listOf(ProfileEffect.LoadProfile("user-123"))
                )
            } else {
                TransitionResult(state)
            }
        }

        // Ignore other events in error state
        else -> TransitionResult(state)
    }
}

// ============================================================================
// VALIDATION: Pure helper functions
// ============================================================================

private fun validateName(name: String): String? {
    return when {
        name.isBlank() -> "Name is required"
        name.length < 2 -> "Name must be at least 2 characters"
        name.length > 50 -> "Name must be less than 50 characters"
        else -> null
    }
}

private fun validateEmail(email: String): String? {
    return when {
        email.isBlank() -> "Email is required"
        !email.contains("@") -> "Invalid email format"
        !email.contains(".") -> "Invalid email format"
        else -> null
    }
}
