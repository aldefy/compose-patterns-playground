package com.example.patterns

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.patterns.exercises.ex01_boolean_explosion.BooleanExplosionExercise
import com.example.patterns.exercises.ex02_state_machine.StateMachineExercise
import com.example.patterns.exercises.ex03_antipatterns.AntiPatternsExercise
import com.example.patterns.exercises.ex03_antipatterns.ap01_launched_effect_trap.LaunchedEffectTrapExercise
import com.example.patterns.exercises.ex03_antipatterns.ap02_derived_state_misuse.DerivedStateMisuseExercise
import com.example.patterns.exercises.ex03_antipatterns.ap03_unstable_lambda.UnstableLambdaExercise
import com.example.patterns.exercises.ex03_antipatterns.ap04_state_in_loop.StateInLoopExercise
import com.example.patterns.exercises.ex03_antipatterns.ap05_side_effect_in_composition.SideEffectInCompositionExercise
import com.example.patterns.exercises.ex03_antipatterns.ap06_flow_collect_wrong.FlowCollectWrongExercise
import com.example.patterns.exercises.ex03_antipatterns.ap07_state_read_too_high.StateReadTooHighExercise
import com.example.patterns.exercises.ex03_antipatterns.ap08_remember_wrong_keys.RememberWrongKeysExercise
import com.example.patterns.exercises.ex03_antipatterns.ap09_shared_state_mutation.SharedStateMutationExercise
import com.example.patterns.exercises.ex03_antipatterns.ap10_event_vs_state.EventVsStateExercise
import com.example.patterns.exercises.ex03_antipatterns.ap11_viewmodel_in_composable.ViewModelInComposableExercise
import com.example.patterns.exercises.ex03_antipatterns.ap12_effects_in_transition.EffectsInTransitionExercise
import com.example.patterns.exercises.ex04_effect_coordinator.EffectCoordinatorExercise
import com.example.patterns.exercises.ex05_testing.TestingExercise
import com.example.patterns.navigation.Screen
import com.example.patterns.ui.components.Difficulty
import com.example.patterns.ui.components.ExerciseCard
import com.example.patterns.ui.theme.ComposePatternsPlaygroundTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposePatternsPlaygroundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Compose Patterns Workshop",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            ) { padding ->
                HomeScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        // Exercise 01: Boolean Explosion
        composable(Screen.BooleanExplosion.route) {
            ExerciseScaffold(
                title = "Boolean Explosion",
                onBack = { navController.popBackStack() }
            ) {
                BooleanExplosionExercise()
            }
        }

        // Exercise 02: State Machine
        composable(Screen.StateMachine.route) {
            ExerciseScaffold(
                title = "State Machine",
                onBack = { navController.popBackStack() }
            ) {
                StateMachineExercise()
            }
        }

        // Exercise 03: Anti-patterns
        composable(Screen.AntiPatterns.route) {
            ExerciseScaffold(
                title = "Anti-patterns",
                onBack = { navController.popBackStack() }
            ) {
                AntiPatternsExercise(
                    onNavigateToAntiPattern = { id ->
                        val route = when (id) {
                            "ap01" -> Screen.AP01.route
                            "ap02" -> Screen.AP02.route
                            "ap03" -> Screen.AP03.route
                            "ap04" -> Screen.AP04.route
                            "ap05" -> Screen.AP05.route
                            "ap06" -> Screen.AP06.route
                            "ap07" -> Screen.AP07.route
                            "ap08" -> Screen.AP08.route
                            "ap09" -> Screen.AP09.route
                            "ap10" -> Screen.AP10.route
                            "ap11" -> Screen.AP11.route
                            "ap12" -> Screen.AP12.route
                            else -> return@AntiPatternsExercise
                        }
                        navController.navigate(route)
                    }
                )
            }
        }

        // Exercise 04: Effect Coordinator
        composable(Screen.EffectCoordinator.route) {
            ExerciseScaffold(
                title = "Effect Coordinator",
                onBack = { navController.popBackStack() }
            ) {
                EffectCoordinatorExercise()
            }
        }

        // Exercise 05: Testing
        composable(Screen.Testing.route) {
            ExerciseScaffold(
                title = "Testing",
                onBack = { navController.popBackStack() }
            ) {
                TestingExercise()
            }
        }

        // Anti-pattern screens
        composable(Screen.AP01.route) {
            ExerciseScaffold(title = "LaunchedEffect Trap", onBack = { navController.popBackStack() }) {
                LaunchedEffectTrapExercise()
            }
        }
        composable(Screen.AP02.route) {
            ExerciseScaffold(title = "derivedStateOf Misuse", onBack = { navController.popBackStack() }) {
                DerivedStateMisuseExercise()
            }
        }
        composable(Screen.AP03.route) {
            ExerciseScaffold(title = "Unstable Lambda", onBack = { navController.popBackStack() }) {
                UnstableLambdaExercise()
            }
        }
        composable(Screen.AP04.route) {
            ExerciseScaffold(title = "State in Loop", onBack = { navController.popBackStack() }) {
                StateInLoopExercise()
            }
        }
        composable(Screen.AP05.route) {
            ExerciseScaffold(title = "Side Effect in Composition", onBack = { navController.popBackStack() }) {
                SideEffectInCompositionExercise()
            }
        }
        composable(Screen.AP06.route) {
            ExerciseScaffold(title = "Flow Collection", onBack = { navController.popBackStack() }) {
                FlowCollectWrongExercise()
            }
        }
        composable(Screen.AP07.route) {
            ExerciseScaffold(title = "State Read Too High", onBack = { navController.popBackStack() }) {
                StateReadTooHighExercise()
            }
        }
        composable(Screen.AP08.route) {
            ExerciseScaffold(title = "Wrong remember Keys", onBack = { navController.popBackStack() }) {
                RememberWrongKeysExercise()
            }
        }
        composable(Screen.AP09.route) {
            ExerciseScaffold(title = "Shared State Mutation", onBack = { navController.popBackStack() }) {
                SharedStateMutationExercise()
            }
        }
        composable(Screen.AP10.route) {
            ExerciseScaffold(title = "Events vs State", onBack = { navController.popBackStack() }) {
                EventVsStateExercise()
            }
        }
        composable(Screen.AP11.route) {
            ExerciseScaffold(title = "Fake ViewModel", onBack = { navController.popBackStack() }) {
                ViewModelInComposableExercise()
            }
        }
        composable(Screen.AP12.route) {
            ExerciseScaffold(title = "Effects in Transitions", onBack = { navController.popBackStack() }) {
                EffectsInTransitionExercise()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Compose Beyond the UI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Architecting Reactive State Machines at Scale",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Exercises",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        ExerciseCard(
            title = "Boolean Explosion",
            description = "Learn why multiple booleans create impossible states",
            difficulty = Difficulty.BEGINNER,
            icon = Icons.Filled.BugReport,
            onClick = { onNavigate(Screen.BooleanExplosion.route) }
        )

        ExerciseCard(
            title = "State Machine",
            description = "Complete state machine with pure transitions",
            difficulty = Difficulty.INTERMEDIATE,
            icon = Icons.Filled.Settings,
            onClick = { onNavigate(Screen.StateMachine.route) }
        )

        ExerciseCard(
            title = "Anti-patterns",
            description = "12 common pitfalls and how to avoid them",
            difficulty = Difficulty.INTERMEDIATE,
            icon = Icons.Filled.Code,
            onClick = { onNavigate(Screen.AntiPatterns.route) }
        )

        ExerciseCard(
            title = "Effect Coordinator",
            description = "Centralized side effect handling",
            difficulty = Difficulty.ADVANCED,
            icon = Icons.Filled.PlayArrow,
            onClick = { onNavigate(Screen.EffectCoordinator.route) }
        )

        ExerciseCard(
            title = "Testing",
            description = "Testing pure state machines",
            difficulty = Difficulty.BEGINNER,
            icon = Icons.Filled.Check,
            onClick = { onNavigate(Screen.Testing.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "by Adit Lal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "GDE Android | Droidcon India 2025",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
