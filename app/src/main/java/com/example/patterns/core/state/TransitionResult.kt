package com.example.patterns.core.state

/**
 * TransitionResult - The heart of pure state machine transitions
 *
 * WHY THIS PATTERN?
 * =================
 * Instead of executing side effects inside transition functions (which makes them impure
 * and hard to test), we RETURN the effects as data. The ViewModel then executes them.
 *
 * BENEFITS:
 * - Transitions become PURE FUNCTIONS: same input -> same output, always
 * - TESTABLE without mocks: just assert on the returned state and effects
 * - DEBUGGABLE: log every transition, replay any bug
 * - PREDICTABLE: effects are explicit, not hidden in callbacks
 *
 * USAGE:
 * ```kotlin
 * // Define your transition as an extension function
 * fun ProfileScreenState.onSubmit(): TransitionResult<ProfileScreenState, ProfileEffect> =
 *     when (this) {
 *         is Editing -> TransitionResult(
 *             newState = Saving(form),
 *             effects = listOf(ProfileEffect.SaveProfile(form))
 *         )
 *         else -> TransitionResult(this) // No-op for invalid transitions
 *     }
 *
 * // In ViewModel, apply the transition
 * fun onSubmit() {
 *     val result = _state.value.onSubmit()
 *     _state.value = result.newState
 *     result.effects.forEach { execute(it) }
 * }
 * ```
 *
 * @param S The state type (should be a sealed interface)
 * @param E The effect type (should be a sealed interface)
 * @param newState The state to transition to
 * @param effects List of side effects to execute (empty by default)
 */
data class TransitionResult<out S, out E>(
    val newState: S,
    val effects: List<E> = emptyList()
) {
    companion object {
        /**
         * Convenience factory for creating a TransitionResult with a single effect
         */
        fun <S, E> withEffect(newState: S, effect: E): TransitionResult<S, E> =
            TransitionResult(newState, listOf(effect))

        /**
         * Convenience factory for creating a no-op transition (state unchanged, no effects)
         */
        fun <S, E> noOp(currentState: S): TransitionResult<S, E> =
            TransitionResult(currentState, emptyList())
    }
}

/**
 * Extension function to add an effect to an existing TransitionResult
 */
fun <S, E> TransitionResult<S, E>.withAdditionalEffect(effect: E): TransitionResult<S, E> =
    copy(effects = effects + effect)

/**
 * Extension function to add multiple effects to an existing TransitionResult
 */
fun <S, E> TransitionResult<S, E>.withAdditionalEffects(newEffects: List<E>): TransitionResult<S, E> =
    copy(effects = effects + newEffects)
