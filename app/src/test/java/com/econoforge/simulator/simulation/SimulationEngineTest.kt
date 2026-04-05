package com.econoforge.simulator.simulation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationEngineTest {
    private val engine = SimulationEngine()

    @Test
    fun neutralPolicyRemainsFiniteAndPositive() {
        var current = DefaultEconomyState

        repeat(24) {
            current = engine.tick(current, DefaultPolicySettings)
            assertTrue(current.gdpIndex > 0f)
            assertFalse(current.gdpIndex.isNaN())
            assertFalse(current.gdpIndex.isInfinite())
            assertTrue(current.bankHealth in 0f..100f)
            assertTrue(current.inflationPct in -1f..15f)
            assertTrue(current.unemploymentPct in 2f..20f)
        }
    }

    @Test
    fun expansionaryPolicyGrowsFasterThanBaseline() {
        val baseline = runPolicy(DefaultPolicySettings, months = 12)
        val expansionary = runPolicy(
            PolicySettings(taxRatePct = 10f, interestRatePct = 1f, reserveRatePct = 4f),
            months = 12,
        )

        assertTrue(expansionary.gdpIndex > baseline.gdpIndex)
        assertTrue(expansionary.inflationPct >= baseline.inflationPct)
    }

    @Test
    fun restrictivePolicySlowsGdpAndRaisesUnemployment() {
        val baseline = runPolicy(DefaultPolicySettings, months = 12)
        val restrictive = runPolicy(
            PolicySettings(taxRatePct = 35f, interestRatePct = 12f, reserveRatePct = 20f),
            months = 12,
        )

        assertTrue(restrictive.gdpIndex < baseline.gdpIndex)
        assertTrue(restrictive.unemploymentPct > baseline.unemploymentPct)
    }

    @Test
    fun extremePoliciesRemainClampedAndBounded() {
        var current = DefaultEconomyState
        val extreme = PolicySettings(taxRatePct = 0f, interestRatePct = 20f, reserveRatePct = 30f)

        repeat(36) {
            current = engine.tick(current, extreme)
            assertTrue(current.monthlyGrowthPct in -2.5f..2.0f)
            assertTrue(current.inflationPct in -1f..15f)
            assertTrue(current.unemploymentPct in 2f..20f)
            assertTrue(current.bankHealth in 0f..100f)
            assertTrue(current.consumerConfidence in 0f..100f)
        }
    }

    private fun runPolicy(policy: PolicySettings, months: Int): EconomyState {
        var current = DefaultEconomyState
        repeat(months) {
            current = engine.tick(current, policy)
        }
        return current
    }
}
