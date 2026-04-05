package com.econoforge.simulator.ui.simulator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.econoforge.simulator.simulation.SimulationSpeed
import com.econoforge.simulator.simulation.initialSimulatorUiState
import com.econoforge.simulator.ui.theme.EconoForgeTheme
import org.junit.Rule
import org.junit.Test

class SimulatorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sliderLabelsUpdateImmediately() {
        composeRule.setContent {
            var uiState by remember { mutableStateOf(initialSimulatorUiState(speed = SimulationSpeed.PAUSED)) }
            EconoForgeTheme {
                SimulatorScreen(
                    uiState = uiState,
                    onTaxChange = { uiState = uiState.copy(policy = uiState.policy.copy(taxRatePct = it)) },
                    onInterestChange = { uiState = uiState.copy(policy = uiState.policy.copy(interestRatePct = it)) },
                    onReserveChange = { uiState = uiState.copy(policy = uiState.policy.copy(reserveRatePct = it)) },
                    onSpeedChange = { uiState = uiState.copy(speed = it) },
                    onReset = { uiState = initialSimulatorUiState(speed = uiState.speed) },
                )
            }
        }

        composeRule.onNodeWithTag("taxSlider")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(40f) }

        composeRule.onNodeWithTag("taxSlider").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "40 percent"),
        )
    }

    @Test
    fun controlsExposeSemanticsAndSpeedSelectionUpdates() {
        composeRule.setContent {
            var uiState by remember { mutableStateOf(initialSimulatorUiState(speed = SimulationSpeed.PAUSED)) }
            EconoForgeTheme {
                SimulatorScreen(
                    uiState = uiState,
                    onTaxChange = { uiState = uiState.copy(policy = uiState.policy.copy(taxRatePct = it)) },
                    onInterestChange = { uiState = uiState.copy(policy = uiState.policy.copy(interestRatePct = it)) },
                    onReserveChange = { uiState = uiState.copy(policy = uiState.policy.copy(reserveRatePct = it)) },
                    onSpeedChange = { uiState = uiState.copy(speed = it) },
                    onReset = { uiState = initialSimulatorUiState(speed = uiState.speed) },
                )
            }
        }

        composeRule.onNodeWithTag("gdpChart").assertIsDisplayed()
        composeRule.onNodeWithTag("interestSlider").assert(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription),
        )
        composeRule.onNodeWithTag("reserveSlider").assert(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription),
        )
        composeRule.onNodeWithTag("speed_X4").performClick()
        composeRule.onNodeWithTag("speed_X4").assertIsSelected()
    }
}
