package com.example.patterns.exercises.ex02_state_machine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.patterns.core.effects.CommonEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// ============================================================================
// VIEWMODEL: Wiring the State Machine to the UI
// ============================================================================
//
// The ViewModel's responsibilities:
// 1. Hold the current state
// 2. Receive events from the UI
// 3. Call the transition function to get new state + effects
// 4. Update state and execute effects
//
// Note: In a real app, you'd inject the EffectHandler and Repository.
// This example keeps things simple for workshop purposes.
// ============================================================================

/**
 * ViewModel that uses the ProfileStateMachine for state management.
 *
 * ARCHITECTURE PATTERN:
 * UI → [Event] → ViewModel.onEvent() → profileTransition() → [State, Effects]
 *                                              ↓
 *                                    Update state + Execute effects
 *                                              ↓
 *                                      UI observes state
 */
/**
 * Represents a single state transition for the history log
 */
data class StateTransition(
    val fromState: String,
    val event: String,
    val toState: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ProfileViewModel : ViewModel() {

    // Current state - UI observes this
    var state: ProfileState by mutableStateOf(ProfileState.Loading)
        private set

    // State transition history for visualization
    var transitionHistory: List<StateTransition> by mutableStateOf(emptyList())
        private set

    // Channel for UI effects (snackbars, navigation)
    private val _uiEffects = MutableSharedFlow<CommonEffect>()
    val uiEffects: SharedFlow<CommonEffect> = _uiEffects.asSharedFlow()

    init {
        // Start loading when ViewModel is created
        loadProfile()
    }

    /**
     * Entry point for all UI events.
     *
     * This is the ONLY way the UI should interact with state.
     * No direct state mutations!
     */
    fun onEvent(event: ProfileEvent) {
        val previousState = state

        // 1. Get the transition result from pure function
        val result = profileTransition(state, event)

        // 2. Update state
        state = result.newState

        // 3. Record state transition if state type actually changed
        // Skip NameChanged/EmailChanged as they don't change state type (stays in Editing)
        val shouldLogTransition = previousState::class != result.newState::class ||
            (event !is ProfileEvent.NameChanged && event !is ProfileEvent.EmailChanged)

        if (previousState != result.newState && shouldLogTransition) {
            transitionHistory = transitionHistory + StateTransition(
                fromState = getStateName(previousState),
                event = getEventName(event),
                toState = getStateName(result.newState)
            )
        }

        // 4. Execute effects
        result.effects.forEach { effect ->
            executeEffect(effect)
        }
    }

    private fun getStateName(state: ProfileState): String = when (state) {
        is ProfileState.Loading -> "Loading"
        is ProfileState.Viewing -> "Viewing"
        is ProfileState.Editing -> "Editing"
        is ProfileState.Saving -> "Saving"
        is ProfileState.Error -> "Error"
    }

    private fun getEventName(event: ProfileEvent): String = when (event) {
        is ProfileEvent.EditClicked -> "EditClicked"
        is ProfileEvent.SaveClicked -> "SaveClicked"
        is ProfileEvent.CancelClicked -> "CancelClicked"
        is ProfileEvent.RetryClicked -> "RetryClicked"
        is ProfileEvent.NameChanged -> "NameChanged"
        is ProfileEvent.EmailChanged -> "EmailChanged"
        is ProfileEvent.ProfileLoaded -> "ProfileLoaded"
        is ProfileEvent.ProfileSaved -> "ProfileSaved"
        is ProfileEvent.LoadFailed -> "LoadFailed"
        is ProfileEvent.SaveFailed -> "SaveFailed"
    }

    fun clearHistory() {
        transitionHistory = emptyList()
    }

    /**
     * Execute a side effect.
     *
     * Effects are handled here, separate from the pure transition.
     * This makes testing the transition function trivial!
     */
    private fun executeEffect(effect: ProfileEffect) {
        viewModelScope.launch {
            when (effect) {
                is ProfileEffect.LoadProfile -> {
                    loadProfileFromServer(effect.userId)
                }

                is ProfileEffect.SaveProfile -> {
                    saveProfileToServer(effect.profile)
                }

                is ProfileEffect.Common -> {
                    _uiEffects.emit(effect.effect)
                }
            }
        }
    }

    // ========================================================================
    // Side Effect Implementations (would be in Repository in real app)
    // ========================================================================

    private fun loadProfile() {
        viewModelScope.launch {
            loadProfileFromServer("user-123")
        }
    }

    private suspend fun loadProfileFromServer(userId: String) {
        // Simulate network delay
        delay(1000)

        // Simulate 90% success rate
        if (Math.random() > 0.1) {
            onEvent(
                ProfileEvent.ProfileLoaded(
                    Profile(
                        id = userId,
                        name = "John Doe",
                        email = "john.doe@example.com",
                        avatarUrl = null
                    )
                )
            )
        } else {
            onEvent(ProfileEvent.LoadFailed("Network error: Failed to load profile"))
        }
    }

    private suspend fun saveProfileToServer(profile: Profile) {
        // Simulate network delay
        delay(1500)

        // Simulate 80% success rate
        if (Math.random() > 0.2) {
            onEvent(ProfileEvent.ProfileSaved(profile))
        } else {
            onEvent(ProfileEvent.SaveFailed("Network error: Failed to save profile"))
        }
    }
}

// ============================================================================
// TESTING EXAMPLE (Preview of Exercise 05)
// ============================================================================
//
// Because profileTransition is pure, testing is trivial:
//
// @Test
// fun `editing state transitions to saving on valid save click`() {
//     val state = ProfileState.Editing(
//         originalProfile = Profile("1", "John", "john@example.com"),
//         form = ProfileFormData("Jane", "jane@example.com")
//     )
//
//     val result = profileTransition(state, ProfileEvent.SaveClicked)
//
//     assertThat(result.newState).isInstanceOf(ProfileState.Saving::class.java)
//     assertThat(result.effects).hasSize(1)
//     assertThat(result.effects[0]).isInstanceOf(ProfileEffect.SaveProfile::class.java)
// }
//
// No mocking required! Pure functions are a joy to test.
// ============================================================================
