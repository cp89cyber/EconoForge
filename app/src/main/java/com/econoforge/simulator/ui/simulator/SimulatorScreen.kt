package com.econoforge.simulator.ui.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.econoforge.simulator.simulation.EconomyStatus
import com.econoforge.simulator.simulation.SimulationSpeed
import com.econoforge.simulator.simulation.SimulatorUiState
import com.econoforge.simulator.state.SimulatorViewModel
import com.econoforge.simulator.ui.components.GdpChart
import com.econoforge.simulator.ui.components.KpiCard
import com.econoforge.simulator.ui.components.PolicySlider
import com.econoforge.simulator.ui.theme.Aqua
import com.econoforge.simulator.ui.theme.Danger
import com.econoforge.simulator.ui.theme.Gold
import com.econoforge.simulator.ui.theme.Success
import com.econoforge.simulator.ui.theme.Warning

@Composable
fun SimulatorRoute(viewModel: SimulatorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SimulatorScreen(
        uiState = uiState,
        onTaxChange = viewModel::setTaxRate,
        onInterestChange = viewModel::setInterestRate,
        onReserveChange = viewModel::setReserveRate,
        onSpeedChange = viewModel::setSpeed,
        onReset = viewModel::reset,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    uiState: SimulatorUiState,
    onTaxChange: (Float) -> Unit,
    onInterestChange: (Float) -> Unit,
    onReserveChange: (Float) -> Unit,
    onSpeedChange: (SimulationSpeed) -> Unit,
    onReset: () -> Unit,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val statusPresentation = statusPresentation(uiState.status)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "EconoForge",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "Policy sandbox",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showHelp = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Open help",
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatusBanner(
                    title = statusPresentation.title,
                    description = statusPresentation.description,
                    accentColor = statusPresentation.color,
                )
                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Live GDP",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Your economy advances one month at a time. Policy changes affect the next tick.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        GdpChart(
                            history = uiState.history,
                            accentColor = statusPresentation.color,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        KpiCard(
                            title = "GDP Index",
                            value = "%.1f".format(uiState.economy.gdpIndex),
                            supportingText = "Base year = 100",
                            accentColor = statusPresentation.color,
                            modifier = Modifier.weight(1f),
                        )
                        KpiCard(
                            title = "Monthly Growth",
                            value = "%+.2f%%".format(uiState.economy.monthlyGrowthPct),
                            supportingText = "Momentum per simulated month",
                            accentColor = if (uiState.economy.monthlyGrowthPct >= 0f) Success else Danger,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        KpiCard(
                            title = "Inflation",
                            value = "%.2f%%".format(uiState.economy.inflationPct),
                            supportingText = "Price pressure",
                            accentColor = if (uiState.economy.inflationPct > 6f) Warning else Aqua,
                            modifier = Modifier.weight(1f),
                        )
                        KpiCard(
                            title = "Unemployment",
                            value = "%.2f%%".format(uiState.economy.unemploymentPct),
                            supportingText = "Labor slack",
                            accentColor = if (uiState.economy.unemploymentPct > 8f) Warning else Gold,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    KpiCard(
                        title = "Bank Health",
                        value = "%.0f".format(uiState.economy.bankHealth),
                        supportingText = "0 to 100 resilience score",
                        accentColor = if (uiState.economy.bankHealth < 40f) Danger else Success,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Text(
                            text = "Policy Levers",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        PolicySlider(
                            label = "Tax Rate",
                            value = uiState.policy.taxRatePct,
                            valueRange = 0f..50f,
                            helperText = "Higher taxes cool demand, but can drag output and jobs.",
                            testTag = "taxSlider",
                            onValueChange = onTaxChange,
                        )
                        PolicySlider(
                            label = "Interest Rate",
                            value = uiState.policy.interestRatePct,
                            valueRange = 0f..20f,
                            helperText = "Higher rates reduce inflation pressure while slowing borrowing.",
                            testTag = "interestSlider",
                            onValueChange = onInterestChange,
                        )
                        PolicySlider(
                            label = "Bank Reserve Rate",
                            value = uiState.policy.reserveRatePct,
                            valueRange = 0f..30f,
                            helperText = "Higher reserves strengthen banks but restrict credit expansion.",
                            testTag = "reserveSlider",
                            onValueChange = onReserveChange,
                        )
                    }
                }

                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Time Controls",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SimulationSpeed.entries.forEach { speed ->
                                FilterChip(
                                    selected = uiState.speed == speed,
                                    onClick = { onSpeedChange(speed) },
                                    label = { Text(speed.label) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("speed_${speed.name}")
                                        .semantics {
                                            contentDescription = "Speed ${speed.label}"
                                            selected = uiState.speed == speed
                                        },
                                )
                            }
                        }
                        Button(
                            onClick = onReset,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Reset simulation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        if (showHelp) {
            HelpDialog(onDismiss = { showHelp = false })
        }
    }
}

@Composable
private fun StatusBanner(
    title: String,
    description: String,
    accentColor: Color,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = accentColor.copy(alpha = 0.16f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("How the simulator works") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This is an accessible educational macro model, not a real-world forecast.")
                Text("Tax rate: higher taxes cool demand and inflation, but can slow GDP and hiring.")
                Text("Interest rate: higher rates reduce borrowing and inflation pressure, but growth can weaken.")
                Text("Reserve rate: higher reserves strengthen banks, but less lending reaches the economy.")
                Text("GDP shows total output. Inflation tracks price pressure. Unemployment tracks labor slack. Bank health estimates system resilience.")
            }
        },
    )
}

private data class StatusPresentation(
    val title: String,
    val description: String,
    val color: Color,
)

private fun statusPresentation(status: EconomyStatus): StatusPresentation =
    when (status) {
        EconomyStatus.STABLE ->
            StatusPresentation(
                title = "Stable expansion",
                description = "Growth is positive and the system is staying inside a manageable policy range.",
                color = Success,
            )
        EconomyStatus.OVERHEATING ->
            StatusPresentation(
                title = "Economy overheating",
                description = "Output is running hot and inflation is climbing beyond the comfort zone.",
                color = Warning,
            )
        EconomyStatus.RECESSION ->
            StatusPresentation(
                title = "Recession risk",
                description = "GDP momentum has turned negative. Policy may need to stimulate demand.",
                color = Danger,
            )
        EconomyStatus.BANK_STRESS ->
            StatusPresentation(
                title = "Banking stress",
                description = "The financial system is getting fragile. Tighten risk controls or cool inflation.",
                color = Danger,
            )
    }
