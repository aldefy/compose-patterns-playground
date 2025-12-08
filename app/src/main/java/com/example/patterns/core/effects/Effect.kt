package com.example.patterns.core.effects

import java.util.UUID

/**
 * Base Effect Interface
 *
 * WHY EFFECTS AS DATA?
 * ====================
 * Effects (side effects) like showing snackbars, navigation, analytics, etc.
 * should be OUTPUTS of state transitions, not inline code.
 *
 * BAD - Effect inside transition:
 * ```kotlin
 * fun onSubmit() {
 *     _state.value = Saving(form)
 *     repository.save(form)        // Side effect hidden here
 *     navigator.goTo(Success)      // Another hidden side effect
 * }
 * ```
 *
 * GOOD - Effects as data:
 * ```kotlin
 * fun ScreenState.onSubmit() = TransitionResult(
 *     newState = Saving(form),
 *     effects = listOf(
 *         Effect.SaveProfile(form),
 *         Effect.Navigate(Routes.Success)
 *     )
 * )
 * ```
 *
 * BENEFITS:
 * - TESTABLE: Assert that correct effects are emitted
 * - TRACEABLE: Log all effects for debugging
 * - REPLAYABLE: Reproduce bugs by replaying effect sequences
 * - DECOUPLED: State logic doesn't depend on effect implementations
 *
 * COMMON EFFECT TYPES:
 * - Navigation: Navigate to a route
 * - Snackbar: Show a message to the user
 * - Analytics: Track events
 * - Haptics: Provide tactile feedback
 * - Database: Save/load data
 * - Network: API calls
 */
interface Effect

/**
 * Common effects used across the app.
 * Each screen can define its own specific effects that extend this.
 */
sealed interface CommonEffect : Effect {

    /**
     * Show a snackbar message.
     *
     * WHY UUID?
     * =========
     * The unique ID ensures that the same message shown twice is treated
     * as two different events. This prevents the "snackbar shows twice after
     * rotation" bug where resubscribing to state re-triggers the snackbar.
     *
     * @param message The message to display
     * @param actionLabel Optional action button text
     * @param id Unique identifier for this specific snackbar instance
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val id: UUID = UUID.randomUUID()
    ) : CommonEffect

    /**
     * Navigate to a destination.
     *
     * @param route The navigation route/destination
     * @param popUpTo Optional route to pop up to before navigating
     * @param inclusive Whether to also pop the popUpTo destination
     */
    data class Navigate(
        val route: String,
        val popUpTo: String? = null,
        val inclusive: Boolean = false
    ) : CommonEffect

    /**
     * Navigate back to the previous screen.
     */
    data object NavigateBack : CommonEffect

    /**
     * Track an analytics event.
     *
     * @param event The event name
     * @param params Additional parameters for the event
     */
    data class TrackAnalytics(
        val event: String,
        val params: Map<String, Any> = emptyMap()
    ) : CommonEffect

    /**
     * Provide haptic feedback.
     */
    sealed interface Haptic : CommonEffect {
        data object Click : Haptic
        data object LongPress : Haptic
        data object Success : Haptic
        data object Error : Haptic
    }

    /**
     * Show a toast message (for debugging in workshop).
     */
    data class ShowToast(
        val message: String,
        val duration: Duration = Duration.Short
    ) : CommonEffect {
        enum class Duration { Short, Long }
    }
}
