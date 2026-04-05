package com.econoforge.simulator.simulation

class SimulationEngine {
    fun tick(current: EconomyState, policy: PolicySettings): EconomyState {
        val demandScore =
            ((25f - policy.taxRatePct) * 0.04f) +
                ((3f - policy.interestRatePct) * 0.12f) +
                ((10f - policy.reserveRatePct) * 0.08f)
        val confidenceBoost = (current.consumerConfidence - 50f) * 0.02f
        val inflationDrag = maxOf(0f, current.inflationPct - 3f) * 0.18f
        val bankDrag = maxOf(0f, 60f - current.bankHealth) * 0.03f

        val monthlyGrowthPct =
            (0.25f + demandScore + confidenceBoost - inflationDrag - bankDrag)
                .coerceIn(-2.5f, 2.0f)
        val gdpIndex = (current.gdpIndex * (1f + monthlyGrowthPct / 100f)).coerceAtLeast(1f)

        val inflationChange =
            ((monthlyGrowthPct - 0.25f) * 0.35f) +
                ((10f - policy.reserveRatePct) * 0.03f) -
                ((policy.interestRatePct - 3f) * 0.07f) -
                ((policy.taxRatePct - 25f) * 0.015f)
        val inflationPct = (current.inflationPct + inflationChange).coerceIn(-1f, 15f)

        val unemploymentChange =
            0.10f -
                (monthlyGrowthPct * 0.35f) +
                ((policy.interestRatePct - 3f) * 0.04f) +
                ((policy.taxRatePct - 25f) * 0.01f)
        val unemploymentPct = (current.unemploymentPct + unemploymentChange).coerceIn(2f, 20f)

        val bankHealthChange =
            ((policy.reserveRatePct - 10f) * 0.35f) -
                (maxOf(0f, inflationPct - 5f) * 0.25f) -
                (maxOf(0f, monthlyGrowthPct - 1.2f) * 0.8f)
        val bankHealth = (current.bankHealth + bankHealthChange).coerceIn(0f, 100f)

        val confidenceChange =
            (monthlyGrowthPct * 2.5f) -
                (maxOf(0f, inflationPct - 4f) * 1.2f) -
                (maxOf(0f, unemploymentPct - 6f) * 1f)
        val consumerConfidence = (current.consumerConfidence + confidenceChange).coerceIn(0f, 100f)

        return current.copy(
            month = current.month + 1,
            gdpIndex = gdpIndex,
            monthlyGrowthPct = monthlyGrowthPct,
            inflationPct = inflationPct,
            unemploymentPct = unemploymentPct,
            bankHealth = bankHealth,
            consumerConfidence = consumerConfidence,
        )
    }

    fun statusFor(state: EconomyState): EconomyStatus =
        when {
            state.bankHealth < 40f -> EconomyStatus.BANK_STRESS
            state.inflationPct > 6f -> EconomyStatus.OVERHEATING
            state.monthlyGrowthPct < 0f -> EconomyStatus.RECESSION
            else -> EconomyStatus.STABLE
        }
}
