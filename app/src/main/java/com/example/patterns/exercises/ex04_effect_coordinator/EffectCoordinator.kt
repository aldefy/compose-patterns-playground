package com.example.patterns.exercises.ex04_effect_coordinator

import com.example.patterns.core.effects.CommonEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// ============================================================================
// EXERCISE 04: Effect Coordinator
// ============================================================================
//
// The Effect Coordinator is the component that EXECUTES effects.
// Remember: State machine transitions return effects as DATA.
// The coordinator is responsible for actually executing them.
//
// RESPONSIBILITIES:
// 1. Receive effects from the state machine
// 2. Execute each effect appropriately
// 3. Dispatch result events back to the state machine
// 4. Coordinate between different effect types
//
// BENEFITS:
// - Centralized effect handling
// - Easy to test (mock the coordinator)
// - Separation of concerns
// - Can handle effect ordering, cancellation, etc.
// ============================================================================

/**
 * A generic effect coordinator interface
 */
interface EffectCoordinator<E, R> {
    /**
     * Execute an effect and return results via callback
     */
    suspend fun execute(effect: E, onResult: (R) -> Unit)
}

/**
 * Sample effects for the coordinator demo
 */
sealed interface DemoEffect {
    data class LoadData(val id: String) : DemoEffect
    data class SaveData(val data: String) : DemoEffect
    data class ShowMessage(val message: String) : DemoEffect
    data class Navigate(val route: String) : DemoEffect
    data class TrackEvent(val name: String, val params: Map<String, String>) : DemoEffect
}

/**
 * Results from executing effects
 */
sealed interface DemoResult {
    data class DataLoaded(val data: String) : DemoResult
    data class DataSaved(val success: Boolean) : DemoResult
    data class MessageShown(val dismissed: Boolean) : DemoResult
    data class NavigationCompleted(val route: String) : DemoResult
    data class EventTracked(val name: String) : DemoResult
}

/**
 * Example Effect Coordinator implementation
 *
 * This coordinator handles all side effects for our demo.
 * In a real app, you'd inject repositories, analytics, navigation, etc.
 */
class DemoEffectCoordinator(
    private val scope: CoroutineScope,
    private val dataRepository: DataRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val messageHandler: MessageHandler
) : EffectCoordinator<DemoEffect, DemoResult> {

    // Events that UI can observe (snackbar, navigation)
    private val _uiEvents = MutableSharedFlow<CommonEffect>()
    val uiEvents: SharedFlow<CommonEffect> = _uiEvents.asSharedFlow()

    override suspend fun execute(effect: DemoEffect, onResult: (DemoResult) -> Unit) {
        when (effect) {
            is DemoEffect.LoadData -> {
                val data = dataRepository.load(effect.id)
                onResult(DemoResult.DataLoaded(data))
            }

            is DemoEffect.SaveData -> {
                val success = dataRepository.save(effect.data)
                onResult(DemoResult.DataSaved(success))

                // Also show a message
                if (success) {
                    _uiEvents.emit(CommonEffect.ShowSnackbar("Saved successfully!"))
                }
            }

            is DemoEffect.ShowMessage -> {
                messageHandler.show(effect.message)
                _uiEvents.emit(CommonEffect.ShowSnackbar(effect.message))
                onResult(DemoResult.MessageShown(dismissed = true))
            }

            is DemoEffect.Navigate -> {
                _uiEvents.emit(CommonEffect.Navigate(effect.route))
                onResult(DemoResult.NavigationCompleted(effect.route))
            }

            is DemoEffect.TrackEvent -> {
                analyticsTracker.track(effect.name, effect.params)
                onResult(DemoResult.EventTracked(effect.name))
            }
        }
    }

    /**
     * Fire-and-forget execution for effects that don't need results
     */
    fun executeAsync(effect: DemoEffect) {
        scope.launch {
            execute(effect) { /* Ignore result */ }
        }
    }

    /**
     * Execute multiple effects in sequence
     */
    suspend fun executeSequence(
        effects: List<DemoEffect>,
        onResult: (DemoResult) -> Unit
    ) {
        effects.forEach { effect ->
            execute(effect, onResult)
        }
    }

    /**
     * Execute multiple effects in parallel
     */
    fun executeParallel(
        effects: List<DemoEffect>,
        onResult: (DemoResult) -> Unit
    ) {
        effects.forEach { effect ->
            scope.launch {
                execute(effect, onResult)
            }
        }
    }
}

// ============================================================================
// MOCK DEPENDENCIES (for demo purposes)
// ============================================================================

interface DataRepository {
    suspend fun load(id: String): String
    suspend fun save(data: String): Boolean
}

interface AnalyticsTracker {
    fun track(name: String, params: Map<String, String>)
}

interface MessageHandler {
    fun show(message: String)
}

/**
 * Simple in-memory implementation for demo
 */
class InMemoryDataRepository : DataRepository {
    private val storage = mutableMapOf<String, String>()

    override suspend fun load(id: String): String {
        kotlinx.coroutines.delay(500) // Simulate network
        return storage[id] ?: "Default data for $id"
    }

    override suspend fun save(data: String): Boolean {
        kotlinx.coroutines.delay(500) // Simulate network
        storage["latest"] = data
        return true
    }
}

class ConsoleAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<Pair<String, Map<String, String>>>()

    override fun track(name: String, params: Map<String, String>) {
        events.add(name to params)
        android.util.Log.d("Analytics", "Track: $name, params: $params")
    }
}

class SimpleMessageHandler : MessageHandler {
    var lastMessage: String? = null
        private set

    override fun show(message: String) {
        lastMessage = message
        android.util.Log.d("Message", message)
    }
}
