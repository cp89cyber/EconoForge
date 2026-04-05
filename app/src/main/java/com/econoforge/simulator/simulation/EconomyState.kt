package com.econoforge.simulator.simulation

const val MAX_HISTORY_POINTS = 120

data class PolicySettings(
    val taxRatePct: Float,
    val interestRatePct: Float,
    val reserveRatePct: Float,
)

data class EconomyState(
    val month: Int,
    val gdpIndex: Float,
    val monthlyGrowthPct: Float,
    val inflationPct: Float,
    val unemploymentPct: Float,
    val bankHealth: Float,
    val consumerConfidence: Float,
)

data class GdpPoint(
    val month: Int,
    val gdpIndex: Float,
)

enum class SimulationSpeed(val label: String, val tickDurationMillis: Long) {
    PAUSED("Pause", Long.MAX_VALUE),
    X1("1x", 500L),
    X2("2x", 250L),
    X4("4x", 125L),
}

enum class EconomyStatus {
    STABLE,
    OVERHEATING,
    RECESSION,
    BANK_STRESS,
}

data class SimulatorUiState(
    val economy: EconomyState,
    val policy: PolicySettings,
    val speed: SimulationSpeed,
    val history: List<GdpPoint>,
    val status: EconomyStatus,
)

val DefaultPolicySettings = PolicySettings(
    taxRatePct = 25f,
    interestRatePct = 3f,
    reserveRatePct = 10f,
)

val DefaultEconomyState = EconomyState(
    month = 0,
    gdpIndex = 100f,
    monthlyGrowthPct = 0.25f,
    inflationPct = 2f,
    unemploymentPct = 5f,
    bankHealth = 75f,
    consumerConfidence = 60f,
)

fun EconomyState.toGdpPoint(): GdpPoint = GdpPoint(month = month, gdpIndex = gdpIndex)

fun initialSimulatorUiState(speed: SimulationSpeed = SimulationSpeed.X1): SimulatorUiState =
    SimulatorUiState(
        economy = DefaultEconomyState,
        policy = DefaultPolicySettings,
        speed = speed,
        history = listOf(DefaultEconomyState.toGdpPoint()),
        status = EconomyStatus.STABLE,
    )
