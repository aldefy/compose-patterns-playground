package com.example.patterns.exercises.ex05_testing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patterns.ui.theme.GoodColor

/**
 * Exercise 05: Testing State Machines
 *
 * This exercise demonstrates why pure state machines are a joy to test.
 *
 * KEY INSIGHT:
 * Because our transition functions are PURE (same input → same output),
 * testing is trivial - no mocking required!
 *
 * WHAT WE TEST:
 * 1. State transitions: Given state + event → expected new state
 * 2. Effect outputs: Given state + event → expected effects
 * 3. Edge cases: Invalid transitions, error handling
 * 4. State invariants: Certain states should never coexist
 *
 * NO MOCKING NEEDED:
 * - No repository mocks
 * - No network mocks
 * - No ViewModel mocks
 * - Just pure function calls!
 */
@Composable
fun TestingExercise(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Exercise 05: Testing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pure state machines are trivially testable. " +
                    "See the test files for examples.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        TestExampleCard(
            title = "Testing State Transitions",
            code = """
                @Test
                fun `loading state shows loading UI`() {
                    val result = profileTransition(
                        state = ProfileState.Loading,
                        event = ProfileEvent.ProfileLoaded(
                            Profile("1", "John", "john@test.com")
                        )
                    )

                    assertThat(result.newState)
                        .isInstanceOf(ProfileState.Viewing::class.java)
                }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TestExampleCard(
            title = "Testing Effect Outputs",
            code = """
                @Test
                fun `save click emits save effect`() {
                    val state = ProfileState.Editing(
                        originalProfile = Profile("1", "John", "john@test.com"),
                        form = ProfileFormData("Jane", "jane@test.com")
                    )

                    val result = profileTransition(
                        state = state,
                        event = ProfileEvent.SaveClicked
                    )

                    assertThat(result.effects)
                        .hasSize(1)
                    assertThat(result.effects[0])
                        .isInstanceOf(ProfileEffect.SaveProfile::class.java)
                }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TestExampleCard(
            title = "Testing Invalid Transitions",
            code = """
                @Test
                fun `save click in loading state is ignored`() {
                    val state = ProfileState.Loading

                    val result = profileTransition(
                        state = state,
                        event = ProfileEvent.SaveClicked
                    )

                    // State unchanged, no effects
                    assertThat(result.newState).isEqualTo(state)
                    assertThat(result.effects).isEmpty()
                }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TestExampleCard(
            title = "Testing Validation",
            code = """
                @Test
                fun `invalid form shows validation errors`() {
                    val state = ProfileState.Editing(
                        originalProfile = testProfile,
                        form = ProfileFormData(name = "", email = "")
                    )

                    val result = profileTransition(
                        state = state,
                        event = ProfileEvent.SaveClicked
                    )

                    // Still editing, with errors
                    val newState = result.newState as ProfileState.Editing
                    assertThat(newState.form.nameError).isNotNull()
                    assertThat(newState.form.emailError).isNotNull()
                }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = GoodColor.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why Testing is Easy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        Pure Functions = Easy Tests

                        1. NO MOCKING REQUIRED
                           • Just call the transition function
                           • Check the result
                           • Done!

                        2. DETERMINISTIC
                           • Same input = Same output
                           • No flaky tests
                           • No race conditions

                        3. FAST
                           • No async setup
                           • No waiting
                           • Thousands of tests in seconds

                        4. COMPREHENSIVE
                           • Easy to test edge cases
                           • Easy to test error paths
                           • Easy to test all transitions
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Test File Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = """
                        Check out the actual test files:

                        app/src/test/java/com/example/patterns/
                        └── exercises/
                            └── ex02_state_machine/
                                └── ProfileStateMachineTest.kt

                        Run tests:
                        ./gradlew test
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TestExampleCard(
    title: String,
    code: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
