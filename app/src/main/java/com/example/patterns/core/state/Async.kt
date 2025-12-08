package com.example.patterns.core.state

/**
 * Async<T> - A sealed interface representing async operation states
 *
 * WHY THIS PATTERN?
 * =================
 * Instead of using separate boolean flags (isLoading, isError, hasData),
 * we use a sealed interface to represent ALL possible states of an async operation.
 *
 * PROBLEMS WITH BOOLEANS:
 * ```kotlin
 * // BAD: 2^3 = 8 combinations, only 4 are valid!
 * data class State(
 *     val isLoading: Boolean,
 *     val isError: Boolean,
 *     val data: User?
 * )
 * // What does isLoading=true AND isError=true mean? UNDEFINED!
 * ```
 *
 * BENEFITS OF ASYNC<T>:
 * - IMPOSSIBLE STATES ARE IMPOSSIBLE: Only 4 states, all valid
 * - EXHAUSTIVE WHEN: Compiler forces you to handle all cases
 * - TYPE-SAFE DATA: Data only exists in Success state
 * - CANCELLATION: Cancelled -> back to Idle, not stuck in Loading
 * - RETRY: Error state holds throwable for retry logic
 *
 * USAGE:
 * ```kotlin
 * data class ProfileScreenState(
 *     val profile: Async<UserProfile> = Async.Idle,
 *     val saveOperation: Async<Unit> = Async.Idle
 * )
 *
 * // In Composable
 * when (val profile = state.profile) {
 *     is Async.Idle -> IdleContent()
 *     is Async.Loading -> LoadingSpinner()
 *     is Async.Success -> ProfileCard(profile.data)
 *     is Async.Error -> ErrorMessage(profile.error)
 * }
 * ```
 */
sealed interface Async<out T> {

    /**
     * Initial state - no operation has been started yet
     */
    data object Idle : Async<Nothing>

    /**
     * Operation is in progress
     * Can optionally hold the previous data for optimistic UI updates
     */
    data class Loading<T>(val previousData: T? = null) : Async<T>

    /**
     * Operation completed successfully
     * @param data The result of the operation
     */
    data class Success<T>(val data: T) : Async<T>

    /**
     * Operation failed
     * @param error The exception that caused the failure
     * @param previousData The data before the operation failed (for retry scenarios)
     */
    data class Error<T>(
        val error: Throwable,
        val previousData: T? = null
    ) : Async<T>
}

// ============================================================================
// Extension functions for working with Async<T>
// ============================================================================

/**
 * Returns the data if this is Success, null otherwise
 */
fun <T> Async<T>.getOrNull(): T? = when (this) {
    is Async.Success -> data
    is Async.Loading -> previousData
    is Async.Error -> previousData
    is Async.Idle -> null
}

/**
 * Returns the data if this is Success, or the default value otherwise
 */
fun <T> Async<T>.getOrDefault(default: T): T = getOrNull() ?: default

/**
 * Returns true if this is Loading
 */
val Async<*>.isLoading: Boolean
    get() = this is Async.Loading

/**
 * Returns true if this is Success
 */
val Async<*>.isSuccess: Boolean
    get() = this is Async.Success

/**
 * Returns true if this is Error
 */
val Async<*>.isError: Boolean
    get() = this is Async.Error

/**
 * Returns true if this is Idle
 */
val Async<*>.isIdle: Boolean
    get() = this is Async.Idle

/**
 * Maps the data inside Success to a new type
 */
inline fun <T, R> Async<T>.map(transform: (T) -> R): Async<R> = when (this) {
    is Async.Idle -> Async.Idle
    is Async.Loading -> Async.Loading(previousData?.let(transform))
    is Async.Success -> Async.Success(transform(data))
    is Async.Error -> Async.Error(error, previousData?.let(transform))
}

/**
 * Executes the given block if this is Success
 */
inline fun <T> Async<T>.onSuccess(action: (T) -> Unit): Async<T> {
    if (this is Async.Success) action(data)
    return this
}

/**
 * Executes the given block if this is Error
 */
inline fun <T> Async<T>.onError(action: (Throwable) -> Unit): Async<T> {
    if (this is Async.Error) action(error)
    return this
}

/**
 * Executes the given block if this is Loading
 */
inline fun <T> Async<T>.onLoading(action: () -> Unit): Async<T> {
    if (this is Async.Loading) action()
    return this
}
