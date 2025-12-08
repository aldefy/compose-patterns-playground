package com.example.patterns.core.effects

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EffectHandler - Coordinates execution of side effects
 *
 * WHY CENTRALIZED EFFECT HANDLING?
 * ================================
 * Instead of scattering effect execution logic throughout ViewModels,
 * we centralize common effect handling here. This provides:
 *
 * 1. CONSISTENCY: Same effect type is handled the same way everywhere
 * 2. TESTABILITY: Easy to mock/fake for testing
 * 3. SINGLE RESPONSIBILITY: ViewModels don't need to know about Toast, Vibrator, etc.
 * 4. CONFIGURATION: Easy to change behavior app-wide (e.g., disable haptics)
 *
 * USAGE IN VIEWMODEL:
 * ```kotlin
 * class MyViewModel @Inject constructor(
 *     private val effectHandler: EffectHandler
 * ) : ViewModel() {
 *
 *     fun processTransition(result: TransitionResult<State, Effect>) {
 *         _state.value = result.newState
 *         result.effects.forEach { effect ->
 *             when (effect) {
 *                 is CommonEffect -> effectHandler.handle(effect)
 *                 is MyScreenEffect -> handleScreenEffect(effect)
 *             }
 *         }
 *     }
 * }
 * ```
 */
@Singleton
class EffectHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Channel for navigation effects - UI collects these
    private val _navigationEffects = Channel<CommonEffect.Navigate>(Channel.BUFFERED)
    val navigationEffects: Flow<CommonEffect.Navigate> = _navigationEffects.receiveAsFlow()

    // Channel for snackbar effects - UI collects these
    private val _snackbarEffects = Channel<CommonEffect.ShowSnackbar>(Channel.BUFFERED)
    val snackbarEffects: Flow<CommonEffect.ShowSnackbar> = _snackbarEffects.receiveAsFlow()

    // Channel for back navigation
    private val _navigateBackEffects = Channel<Unit>(Channel.BUFFERED)
    val navigateBackEffects: Flow<Unit> = _navigateBackEffects.receiveAsFlow()

    /**
     * Handle a common effect.
     * Call this from your ViewModel when processing TransitionResult effects.
     */
    suspend fun handle(effect: CommonEffect) {
        when (effect) {
            is CommonEffect.ShowSnackbar -> {
                _snackbarEffects.send(effect)
            }

            is CommonEffect.Navigate -> {
                _navigationEffects.send(effect)
            }

            is CommonEffect.NavigateBack -> {
                _navigateBackEffects.send(Unit)
            }

            is CommonEffect.TrackAnalytics -> {
                // In a real app, you'd send this to your analytics service
                android.util.Log.d("Analytics", "Event: ${effect.event}, Params: ${effect.params}")
            }

            is CommonEffect.Haptic -> {
                performHaptic(effect)
            }

            is CommonEffect.ShowToast -> {
                showToast(effect)
            }
        }
    }

    private fun performHaptic(haptic: CommonEffect.Haptic) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService<Vibrator>()
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (haptic) {
                CommonEffect.Haptic.Click -> VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                CommonEffect.Haptic.LongPress -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                CommonEffect.Haptic.Success -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                CommonEffect.Haptic.Error -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private fun showToast(toast: CommonEffect.ShowToast) {
        val duration = when (toast.duration) {
            CommonEffect.ShowToast.Duration.Short -> Toast.LENGTH_SHORT
            CommonEffect.ShowToast.Duration.Long -> Toast.LENGTH_LONG
        }
        Toast.makeText(context, toast.message, duration).show()
    }
}
