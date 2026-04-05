package com.econoforge.simulator.state

import com.econoforge.simulator.simulation.DefaultEconomyState
import com.econoforge.simulator.simulation.DefaultPolicySettings
import com.econoforge.simulator.simulation.SimulationSpeed
import com.econoforge.simulator.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Test
    fun pauseStopsUpdatesAndSpeedChangesAffectCadence() {
        val viewModel = SimulatorViewModel(tickerDispatcher = testDispatcher)

        assertEquals(0, viewModel.uiState.value.economy.month)

        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.uiState.value.economy.month)

        viewModel.setSpeed(SimulationSpeed.PAUSED)
        testDispatcher.scheduler.advanceTimeBy(2_000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.uiState.value.economy.month)

        viewModel.setSpeed(SimulationSpeed.X4)
        testDispatcher.scheduler.advanceTimeBy(124)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.uiState.value.economy.month)

        testDispatcher.scheduler.advanceTimeBy(1)
        testDispatcher.scheduler.runCurrent()
        assertEquals(2, viewModel.uiState.value.economy.month)
    }

    @Test
    fun resetRestoresInitialStateAndSingleHistoryPoint() {
        val viewModel = SimulatorViewModel(tickerDispatcher = testDispatcher)

        viewModel.setTaxRate(12f)
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.uiState.value.economy.month)

        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals(DefaultEconomyState, state.economy)
        assertEquals(DefaultPolicySettings, state.policy)
        assertEquals(1, state.history.size)
        assertEquals(0, state.history.first().month)
    }
}
