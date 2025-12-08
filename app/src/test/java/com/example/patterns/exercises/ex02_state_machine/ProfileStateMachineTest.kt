package com.example.patterns.exercises.ex02_state_machine

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for the ProfileStateMachine
 *
 * These tests demonstrate the key benefit of pure state machines:
 * NO MOCKING REQUIRED!
 *
 * Each test simply:
 * 1. Creates a state
 * 2. Creates an event
 * 3. Calls the transition function
 * 4. Asserts the result
 *
 * That's it. No setup, no mocks, no async handling.
 */
class ProfileStateMachineTest {

    private val testProfile = Profile(
        id = "user-123",
        name = "John Doe",
        email = "john@example.com"
    )

    // ========================================================================
    // Loading State Tests
    // ========================================================================

    @Test
    fun `loading state transitions to viewing on profile loaded`() {
        val result = profileTransition(
            state = ProfileState.Loading,
            event = ProfileEvent.ProfileLoaded(testProfile)
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Viewing::class.java)
        val viewingState = result.newState as ProfileState.Viewing
        assertThat(viewingState.profile).isEqualTo(testProfile)
    }

    @Test
    fun `loading state transitions to error on load failed`() {
        val result = profileTransition(
            state = ProfileState.Loading,
            event = ProfileEvent.LoadFailed("Network error")
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Error::class.java)
        val errorState = result.newState as ProfileState.Error
        assertThat(errorState.message).isEqualTo("Network error")
    }

    @Test
    fun `loading state ignores edit clicked`() {
        val result = profileTransition(
            state = ProfileState.Loading,
            event = ProfileEvent.EditClicked
        )

        assertThat(result.newState).isEqualTo(ProfileState.Loading)
        assertThat(result.effects).isEmpty()
    }

    // ========================================================================
    // Viewing State Tests
    // ========================================================================

    @Test
    fun `viewing state transitions to editing on edit clicked`() {
        val viewingState = ProfileState.Viewing(testProfile)

        val result = profileTransition(
            state = viewingState,
            event = ProfileEvent.EditClicked
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Editing::class.java)
        val editingState = result.newState as ProfileState.Editing
        assertThat(editingState.originalProfile).isEqualTo(testProfile)
        assertThat(editingState.form.name).isEqualTo(testProfile.name)
        assertThat(editingState.form.email).isEqualTo(testProfile.email)
    }

    @Test
    fun `viewing state ignores save clicked`() {
        val viewingState = ProfileState.Viewing(testProfile)

        val result = profileTransition(
            state = viewingState,
            event = ProfileEvent.SaveClicked
        )

        assertThat(result.newState).isEqualTo(viewingState)
        assertThat(result.effects).isEmpty()
    }

    // ========================================================================
    // Editing State Tests
    // ========================================================================

    @Test
    fun `editing state updates name on name changed`() {
        val editingState = ProfileState.Editing(
            originalProfile = testProfile,
            form = ProfileFormData(name = "John", email = "john@example.com")
        )

        val result = profileTransition(
            state = editingState,
            event = ProfileEvent.NameChanged("Jane")
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Editing::class.java)
        val newState = result.newState as ProfileState.Editing
        assertThat(newState.form.name).isEqualTo("Jane")
    }

    @Test
    fun `editing state shows validation error for blank name`() {
        val editingState = ProfileState.Editing(
            originalProfile = testProfile,
            form = ProfileFormData(name = "John", email = "john@example.com")
        )

        val result = profileTransition(
            state = editingState,
            event = ProfileEvent.NameChanged("")
        )

        val newState = result.newState as ProfileState.Editing
        assertThat(newState.form.nameError).isNotNull()
        assertThat(newState.form.nameError).contains("required")
    }

    @Test
    fun `editing state shows validation error for invalid email`() {
        val editingState = ProfileState.Editing(
            originalProfile = testProfile,
            form = ProfileFormData(name = "John", email = "john@example.com")
        )

        val result = profileTransition(
            state = editingState,
            event = ProfileEvent.EmailChanged("invalid-email")
        )

        val newState = result.newState as ProfileState.Editing
        assertThat(newState.form.emailError).isNotNull()
        assertThat(newState.form.emailError).contains("Invalid")
    }

    @Test
    fun `editing state transitions to saving on valid save click`() {
        val editingState = ProfileState.Editing(
            originalProfile = testProfile,
            form = ProfileFormData(name = "Jane Doe", email = "jane@example.com")
        )

        val result = profileTransition(
            state = editingState,
            event = ProfileEvent.SaveClicked
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Saving::class.java)
        assertThat(result.effects).hasSize(1)
        assertThat(result.effects[0]).isInstanceOf(ProfileEffect.SaveProfile::class.java)
    }

    @Test
    fun `editing state stays in editing with validation errors on invalid save click`() {
        val editingState = ProfileState.Editing(
            originalProfile = testProfile,
            form = ProfileFormData(name = "", email = "")
        )

        val result = profileTransition(
            state = editingState,
            event = ProfileEvent.SaveClicked
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Editing::class.java)
        val newState = result.newState as ProfileState.Editing
        assertThat(newState.form.nameError).isNotNull()
        assertThat(newState.form.emailError).isNotNull()
    }

    @Test
    fun `editing state transitions back to viewing on cancel`() {
        val editingState = ProfileState.Editing(
            originalProfile = testProfile,
            form = ProfileFormData(name = "Changed Name", email = "changed@example.com")
        )

        val result = profileTransition(
            state = editingState,
            event = ProfileEvent.CancelClicked
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Viewing::class.java)
        val viewingState = result.newState as ProfileState.Viewing
        // Original profile should be restored
        assertThat(viewingState.profile).isEqualTo(testProfile)
    }

    // ========================================================================
    // Saving State Tests
    // ========================================================================

    @Test
    fun `saving state transitions to viewing on profile saved`() {
        val savingState = ProfileState.Saving(
            originalProfile = testProfile,
            form = ProfileFormData(name = "Jane", email = "jane@example.com")
        )
        val savedProfile = testProfile.copy(name = "Jane", email = "jane@example.com")

        val result = profileTransition(
            state = savingState,
            event = ProfileEvent.ProfileSaved(savedProfile)
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Viewing::class.java)
        val viewingState = result.newState as ProfileState.Viewing
        assertThat(viewingState.profile).isEqualTo(savedProfile)
        // Should emit snackbar effect
        assertThat(result.effects).isNotEmpty()
    }

    @Test
    fun `saving state transitions to error on save failed`() {
        val savingState = ProfileState.Saving(
            originalProfile = testProfile,
            form = ProfileFormData(name = "Jane", email = "jane@example.com")
        )

        val result = profileTransition(
            state = savingState,
            event = ProfileEvent.SaveFailed("Network error")
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Error::class.java)
        val errorState = result.newState as ProfileState.Error
        assertThat(errorState.message).isEqualTo("Network error")
        assertThat(errorState.retryEvent).isEqualTo(ProfileEvent.SaveClicked)
    }

    // ========================================================================
    // Error State Tests
    // ========================================================================

    @Test
    fun `error state with retry event transitions to loading on retry`() {
        val errorState = ProfileState.Error(
            message = "Network error",
            retryEvent = ProfileEvent.SaveClicked
        )

        val result = profileTransition(
            state = errorState,
            event = ProfileEvent.RetryClicked
        )

        assertThat(result.newState).isInstanceOf(ProfileState.Loading::class.java)
        assertThat(result.effects).isNotEmpty()
    }

    @Test
    fun `error state without retry event stays in error on retry`() {
        val errorState = ProfileState.Error(
            message = "Network error",
            retryEvent = null
        )

        val result = profileTransition(
            state = errorState,
            event = ProfileEvent.RetryClicked
        )

        assertThat(result.newState).isEqualTo(errorState)
    }
}
