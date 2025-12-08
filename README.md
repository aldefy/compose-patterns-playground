# Compose Patterns Playground

> Make impossible states impossible. Hands-on Compose patterns: state machines, anti-patterns & reactive architecture.

A workshop project for **"Compose Beyond the UI: Architecting Reactive State Machines at Scale"**

**Droidcon India 2025** | Adit Lal (GDE Android)

## Slides

📑 [Download Workshop Slides (PDF)](docs/slides.pdf)

## Overview

This project demonstrates patterns for building predictable, testable, and scalable UI state in Jetpack Compose. Through hands-on exercises, you'll learn to avoid common anti-patterns and build robust state machines.

## Key Concepts

### 1. Boolean Explosion Problem

```kotlin
// BAD: 2^4 = 16 combinations, only 4 are valid!
data class ProfileState(
    val isLoading: Boolean,
    val isSaving: Boolean,
    val isError: Boolean,
    val isSuccess: Boolean
)

// GOOD: Only valid states exist
sealed interface ProfileState {
    data object Loading : ProfileState
    data class Editing(val form: Form) : ProfileState
    data class Saving(val form: Form) : ProfileState
    data class Success(val message: String) : ProfileState
    data class Error(val reason: String) : ProfileState
}
```

### 2. TransitionResult Pattern

Pure state transitions that return effects as data:

```kotlin
data class TransitionResult<S, E>(
    val newState: S,
    val effects: List<E> = emptyList()
)

fun transition(state: State, event: Event): TransitionResult<State, Effect> {
    return when (event) {
        is Event.LoadClicked -> TransitionResult(
            newState = State.Loading,
            effects = listOf(Effect.LoadData)
        )
        // ...
    }
}
```

### 3. Effects as Data

Side effects are returned as data, not executed inline:

```kotlin
sealed interface Effect {
    data class LoadProfile(val id: String) : Effect
    data class SaveProfile(val profile: Profile) : Effect
    data class ShowSnackbar(val message: String) : Effect
}
```

## Project Structure

```
compose-patterns-playground/
├── README.md                           # Workshop guide
├── docs/
│   └── slides.pdf                      # Workshop slides (100 pages)
├── app/
│   └── src/
│       ├── main/java/com/example/patterns/
│       │   ├── MainActivity.kt         # Navigation hub to all exercises
│       │   ├── core/
│       │   │   ├── state/              # TransitionResult, Async
│       │   │   └── effects/            # Effect types and handler
│       │   ├── exercises/
│       │   │   ├── ex01_boolean_explosion/
│       │   │   │   ├── BooleanExplosionBad.kt
│       │   │   │   ├── BooleanExplosionGood.kt
│       │   │   │   └── BooleanExplosionExercise.kt
│       │   │   ├── ex02_state_machine/
│       │   │   │   ├── ProfileStateMachine.kt
│       │   │   │   ├── ProfileViewModel.kt
│       │   │   │   ├── ProfileScreen.kt
│       │   │   │   └── StateMachineExercise.kt
│       │   │   ├── ex03_antipatterns/
│       │   │   │   ├── ap01_launched_effect_trap/
│       │   │   │   │   ├── LaunchedEffectTrapBroken.kt
│       │   │   │   │   └── LaunchedEffectTrapFixed.kt
│       │   │   │   ├── ap02_derived_state_misuse/
│       │   │   │   ├── ap03_unstable_lambda/
│       │   │   │   ├── ap04_state_in_loop/
│       │   │   │   ├── ap05_side_effect_in_composition/
│       │   │   │   ├── ap06_flow_collect_wrong/
│       │   │   │   ├── ap07_state_read_too_high/
│       │   │   │   ├── ap08_remember_wrong_keys/
│       │   │   │   ├── ap09_shared_state_mutation/
│       │   │   │   ├── ap10_event_vs_state/
│       │   │   │   ├── ap11_viewmodel_in_composable/
│       │   │   │   └── ap12_effects_in_transition/
│       │   │   ├── ex04_effect_coordinator/
│       │   │   └── ex05_testing/
│       │   ├── navigation/
│       │   └── ui/
│       │       ├── components/         # RecompositionCounter, CodeToggle
│       │       └── theme/              # Material 3 theming
│       └── test/java/com/example/patterns/
│           └── exercises/ex02_state_machine/
│               └── ProfileStateMachineTest.kt
├── gradle/
└── build.gradle.kts
```

## Exercises

### Exercise 01: Boolean Explosion

Learn why multiple boolean flags create impossible states and how sealed interfaces solve this problem.

**Key Takeaway**: Use sealed interfaces to model mutually exclusive states.

### Exercise 02: State Machine

Build a complete state machine with:
- Sealed interface for states
- Sealed interface for events
- Pure transition function
- Effects as data

**Key Takeaway**: Transitions should be pure functions that return new state + effects.

### Exercise 03: Anti-patterns

Explore 12 common Compose mistakes with interactive Broken/Fixed demos. Each anti-pattern shows the bug in action and demonstrates the correct approach.

**Key Takeaway**: Understanding what NOT to do is as important as knowing the right patterns.

### Exercise 04: Effect Coordinator

Centralized effect handling that:
- Receives effects from state machine
- Executes each effect appropriately
- Returns results as events

### Exercise 05: Testing

Testing pure state machines is trivial:

```kotlin
@Test
fun `loading state transitions to viewing on profile loaded`() {
    val result = profileTransition(
        state = ProfileState.Loading,
        event = ProfileEvent.ProfileLoaded(testProfile)
    )

    assertThat(result.newState).isInstanceOf(ProfileState.Viewing::class.java)
}
```

**No mocking required!**

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Build & Run

```bash
# Clone the repository
git clone <repo-url>

# Open in Android Studio and sync
# Or build from command line:
./gradlew assembleDebug

# Run tests
./gradlew test
```

## Tech Stack

- **Kotlin** 2.0.21
- **Jetpack Compose** (BOM 2024.11.00)
- **Material 3**
- **Kotlin Coroutines** 1.9.0
- **Hilt** 2.52 for DI
- **Truth** 1.4.4 for testing

## Architecture Principles

1. **State as Sealed Interface**: Only valid states can exist
2. **Pure Transitions**: Same input → same output, no side effects
3. **Effects as Data**: Side effects returned, not executed
4. **Exhaustive Handling**: Compiler enforces all cases handled
5. **Single Source of Truth**: One state object per screen

## License

MIT License - Feel free to use for learning and workshops!

## Credits

Created by **Adit Lal** for Droidcon India 2025

GDE Android
