package com.example.patterns.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")

    // Exercises
    data object BooleanExplosion : Screen("ex01_boolean_explosion")
    data object StateMachine : Screen("ex02_state_machine")
    data object AntiPatterns : Screen("ex03_antipatterns")
    data object EffectCoordinator : Screen("ex04_effect_coordinator")
    data object Testing : Screen("ex05_testing")

    // Anti-patterns
    data object AP01 : Screen("ap01_launched_effect")
    data object AP02 : Screen("ap02_derived_state")
    data object AP03 : Screen("ap03_unstable_lambda")
    data object AP04 : Screen("ap04_state_in_loop")
    data object AP05 : Screen("ap05_side_effect")
    data object AP06 : Screen("ap06_flow_collect")
    data object AP07 : Screen("ap07_state_read_high")
    data object AP08 : Screen("ap08_remember_keys")
    data object AP09 : Screen("ap09_shared_mutation")
    data object AP10 : Screen("ap10_event_vs_state")
    data object AP11 : Screen("ap11_viewmodel")
    data object AP12 : Screen("ap12_effects_transition")
}
