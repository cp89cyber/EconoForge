package com.econoforge.simulator.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.econoforge.simulator.simulation.EconomyState
import com.econoforge.simulator.simulation.MAX_HISTORY_POINTS
import com.econoforge.simulator.simulation.PolicySettings
import com.econoforge.simulator.simulation.SimulationEngine
import com.econoforge.simulator.simulation.SimulationSpeed
import com.econoforge.simulator.simulation.SimulatorUiState
import com.econoforge.simulator.simulation.initialSimulatorUiState
import com.econoforge.simulator.simulation.toGdpPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SimulatorViewModel(
    private val engine: SimulationEngine = SimulationEngine(),
    private val tickerDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialSimulatorUiState())
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        syncTicker()
    }

    fun setTaxRate(value: Float) {
        updatePolicy { copy(taxRatePct = value.coerceIn(0f, 50f)) }
    }

    fun setInterestRate(value: Float) {
        updatePolicy { copy(interestRatePct = value.coerceIn(0f, 20f)) }
    }

    fun setReserveRate(value: Float) {
        updatePolicy { copy(reserveRatePct = value.coerceIn(0f, 30f)) }
    }

    fun setSpeed(speed: SimulationSpeed) {
        _uiState.update { it.copy(speed = speed) }
        syncTicker()
    }

    fun reset() {
        val currentSpeed = _uiState.value.speed
        _uiState.value = initialSimulatorUiState(speed = currentSpeed)
        syncTicker()
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }

    private fun syncTicker() {
        tickerJob?.cancel()
        val speed = _uiState.value.speed
        if (speed == SimulationSpeed.PAUSED) {
            return
        }
        tickerJob = viewModelScope.launch(tickerDispatcher) {
            while (isActive) {
                delay(_uiState.value.speed.tickDurationMillis)
                advanceOneMonth()
            }
        }
    }

    private fun advanceOneMonth() {
        _uiState.update { state ->
            val nextEconomy = engine.tick(state.economy, state.policy)
            state.copy(
                economy = nextEconomy,
                history = (state.history + nextEconomy.toGdpPoint()).takeLast(MAX_HISTORY_POINTS),
                status = engine.statusFor(nextEconomy),
            )
        }
    }

    private fun updatePolicy(update: PolicySettings.() -> PolicySettings) {
        _uiState.update { state -> state.copy(policy = state.policy.update()) }
    }
}
