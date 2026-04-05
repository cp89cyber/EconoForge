# EconoForge

EconoForge is an Android economic policy sandbox built with Jetpack Compose. It simulates an economy one month at a time and lets you adjust tax rate, interest rate, and bank reserve rate while tracking GDP, inflation, unemployment, bank health, and consumer confidence.

> Repository status: this snapshot includes a debug APK plus build and test artifacts. The Android source tree and top-level Gradle project files required for a verified source build are not present in the current checkout.

## Features

- Live macroeconomic simulation with month-by-month updates
- Policy sliders for tax rate, interest rate, and bank reserve rate
- KPI outputs for GDP, inflation, unemployment, bank health, and consumer confidence
- GDP history and chart visualization
- Simulation speed control, including pause
- Reset control
- Economy status states for stable, overheating, recession, and bank stress

## Technical Snapshot

- Platform: Android
- UI stack inferred from compiled classes: Jetpack Compose
- Application ID: `com.econoforge.simulator`
- Version: `1.0` (version code `1`)
- `minSdk`: `26`
- `targetSdk`: `36`
- Main activity launches in portrait orientation

## Architecture at a Glance

- `simulation`: `EconomyState`, `PolicySettings`, `SimulationEngine`, `SimulationSpeed`, `EconomyStatus`, `GdpPoint`, `SimulatorUiState`
- `state`: `SimulatorViewModel`
- `ui/components`: GDP chart, KPI card, and policy slider components
- `ui/simulator`: main simulator screen, status banner, and help dialog
- `ui/theme`: app theme primitives

## Running / Inspecting the Current Snapshot

The current snapshot includes a debug APK at [app-debug.apk](/home/culpen0/EconoForge/app/build/outputs/apk/debug/app-debug.apk).

This checkout does not currently contain enough checked-in source and build configuration to document a verified rebuild flow, so this README intentionally omits speculative `./gradlew` or Android Studio setup steps.

## Testing

Verified unit test reports are available in [SimulationEngineTest.xml](/home/culpen0/EconoForge/app/build/test-results/testDebugUnitTest/TEST-com.econoforge.simulator.simulation.SimulationEngineTest.xml) and [SimulatorViewModelTest.xml](/home/culpen0/EconoForge/app/build/test-results/testDebugUnitTest/TEST-com.econoforge.simulator.state.SimulatorViewModelTest.xml).

Covered scenarios include:

- neutral policy remains finite and positive
- expansionary policy grows faster than baseline
- restrictive policy slows GDP and raises unemployment
- extreme policy values remain clamped and bounded
- pause affects simulation cadence
- reset restores initial state and a single history point

Compiled UI test classes are present in the build outputs, but this snapshot does not include an instrumented test report to verify their execution.

## License

This project is licensed under the GNU AGPL v3 / AGPL-3.0. See [LICENSE](/home/culpen0/EconoForge/LICENSE).
