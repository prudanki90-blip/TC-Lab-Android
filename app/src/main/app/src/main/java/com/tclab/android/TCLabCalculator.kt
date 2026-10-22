```kotlin
package com.tclab.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.pow

private val calculatorTools = listOf(
    "Stock Solution Dilution",
    "Molarity to Mass",
    "Percentage Solution (w/v)",
    "Media Recipe Scaling",
    "Agar Calculation",
    "Serial Dilution",
    "Culture Vessel Planning",
    "Batch Cost Estimate"
)

@Composable
fun TCLabCalculatorScreen() {
    var selectedTool by rememberSaveable {
        mutableStateOf(calculatorTools[0])
    }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var result by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }

    // Stock solution dilution
    var stockConcentration by rememberSaveable { mutableStateOf("") }
    var targetConcentration by rememberSaveable { mutableStateOf("") }
    var finalVolume by rememberSaveable { mutableStateOf("") }

    // Molarity to mass
    var molarity by rememberSaveable { mutableStateOf("") }
    var molecularWeight by rememberSaveable { mutableStateOf("") }
    var molarVolume by rememberSaveable { mutableStateOf("") }

    // Percentage solution
    var percentage by rememberSaveable { mutableStateOf("") }
    var percentageVolume by rememberSaveable { mutableStateOf("") }

    // Media scaling
    var baseVolume by rememberSaveable { mutableStateOf("") }
    var targetMediaVolume by rememberSaveable { mutableStateOf("") }
    var ingredientAmount by rememberSaveable { mutableStateOf("") }
    var ingredientUnit by rememberSaveable { mutableStateOf("g") }

    // Agar
    var agarRate by rememberSaveable { mutableStateOf("") }
    var agarVolume by rememberSaveable { mutableStateOf("") }

    // Serial dilution
    var initialConcentration by rememberSaveable { mutableStateOf("") }
    var dilutionFactor by rememberSaveable { mutableStateOf("10") }
    var dilutionSteps by rememberSaveable { mutableStateOf("1") }

    // Culture vessel planning
    var desiredVessels by rememberSaveable { mutableStateOf("") }
    var lossPercent by rememberSaveable { mutableStateOf("10") }

    // Cost estimate
    var baseCost by rememberSaveable { mutableStateOf("") }
    var wastePercent by rememberSaveable { mutableStateOf("10") }

    fun calculate() {
        error = false
        result = ""

        try {
            when (selectedTool) {
                "Stock Solution Dilution" -> {
                    val c1 = stockConcentration.toDoubleOrNull()
                    val c2 = targetConcentration.toDoubleOrNull()
                    val v2 = finalVolume.toDoubleOrNull()

                    if (c1 == null || c2 == null || v2 == null ||
                        c1 <= 0.0 || c2 < 0.0 || v2 <= 0.0
                    ) {
                        throw IllegalArgumentException(
                            "Enter a positive stock concentration and final volume, and a non-negative target concentration."
                        )
                    }

                    if (c2 > c1) {
                        throw IllegalArgumentException(
                            "The target concentration exceeds the stock concentration. Dilution alone cannot achieve it."
                        )
                    }

                    val v1 = c2 * v2 / c1
                    val approximateDiluent = v2 - v1

                    result = """
                        Stock solution required: ${fmt(v1)} volume units
                        Diluent estimate: ${fmt(approximateDiluent)} volume units
                        Final volume: ${fmt(v2)} volume units

                        Formula:
                        C₁V₁ = C₂V₂
                        V₁ = C₂ × V₂ ÷ C₁

                        Use the same concentration units for C₁ and C₂, and the same volume units for V₁ and V₂.

                        Practical note: add the stock, then bring the mixture to the specified final volume. The diluent estimate assumes volumes are additive.
                    """.trimIndent()
                }

                "Molarity to Mass" -> {
                    val m = molarity.toDoubleOrNull()
                    val mw = molecularWeight.toDoubleOrNull()
                    val v = molarVolume.toDoubleOrNull()

                    if (m == null || mw == null || v == null ||
                        m < 0.0 || mw <= 0.0 || v <= 0.0
                    ) {
                        throw IllegalArgumentException(
                            "Enter a non-negative molarity and positive molecular weight and final volume."
                        )
                    }

                    val grams = m * mw * (v / 1000.0)
                    val moles = m * (v / 1000.0)

                    result = """
                        Required mass: ${fmt(grams)} g
                        Amount of substance: ${fmt(moles)} mol
                        Final solution volume: ${fmt(v)} mL

                        Formula:
                        mass (g) = molarity (mol/L) × molecular weight (g/mol) × volume (L)

                        The entered volume is the final solution volume, not necessarily the amount of water to add.
                    """.trimIndent()
                }

                "Percentage Solution (w/v)" -> {
                    val p = percentage.toDoubleOrNull()
                    val v = percentageVolume.toDoubleOrNull()

                    if (p == null || v == null || p < 0.0 || v <= 0.0) {
                        throw IllegalArgumentException(
                            "Enter a non-negative percentage and a positive final volume."
                        )
                    }

                    val grams = p * v / 100.0

                    result = """
                        Solute required: ${fmt(grams)} g
                        Final solution volume: ${fmt(v)} mL

                        Formula:
                        % (w/v) = grams of solute per 100 mL final solution
                        grams = percentage × final volume ÷ 100

                        Dissolve the solute and bring the solution to the specified final volume.
                    """.trimIndent()
                }

                "Media Recipe Scaling" -> {
                    val base = baseVolume.toDoubleOrNull()
                    val target = targetMediaVolume.toDoubleOrNull()
                    val amount = ingredientAmount.toDoubleOrNull()

                    if (base == null || target == null || amount == null ||
                        base <= 0.0 || target <= 0.0 || amount < 0.0
                    ) {
                        throw IllegalArgumentException(
                            "Enter positive base and target volumes and a non-negative ingredient amount."
                        )
                    }

                    val factor = target / base
                    val scaled = amount * factor

                    result = """
                        Scaling factor: ${fmt(factor)}×

                        Original ingredient amount: ${fmt(amount)} $ingredientUnit
                        Scaled ingredient amount: ${fmt(scaled)} $ingredientUnit

                        Original recipe volume: ${fmt(base)} mL
                        Target recipe volume: ${fmt(target)} mL

                        Formula:
                        scaling factor = target volume ÷ original volume
                        new amount = original amount × scaling factor

                        This scales one ingredient mathematically. Repeat for every ingredient in the recipe. Confirm that the original recipe and target volumes use the same units.
                    """.trimIndent()
                }

                "Agar Calculation" -> {
                    val rate = agarRate.toDoubleOrNull()
                    val volume = agarVolume.toDoubleOrNull()

                    if (rate == null || volume == null ||
                        rate < 0.0 || volume <= 0.0
                    ) {
                        throw IllegalArgumentException(
                            "Enter a non-negative agar rate in g/L and a positive final medium volume."
                        )
                    }

                    val grams = rate * volume / 1000.0

                    result = """
                        Agar required: ${fmt(grams)} g
                        Agar rate entered: ${fmt(rate)} g/L
                        Final medium volume: ${fmt(volume)} mL

                        Formula:
                        agar mass (g) = agar rate (g/L) × volume (L)

                        This is a quantity calculation only. It does not establish that the selected agar concentration is appropriate for a particular culture.
                    """.trimIndent()
                }

                "Serial Dilution" -> {
                    val initial = initialConcentration.toDoubleOrNull()
                    val factor = dilutionFactor.toDoubleOrNull()
                    val steps = dilutionSteps.toIntOrNull()

                    if (initial == null || factor == null || steps == null ||
                        initial < 0.0 || factor <= 1.0 || steps < 1 || steps > 100
                    ) {
                        throw IllegalArgumentException(
                            "Enter a non-negative initial concentration, a dilution factor greater than 1, and 1–100 steps."
                        )
                    }

                    val final = initial / factor.pow(steps)

                    result = """
                        Initial concentration: ${fmt(initial)}
                        Dilution factor per step: 1:${fmt(factor)}
                        Number of steps: $steps
                        Final theoretical concentration: ${fmt(final)}

                        Formula:
                        final concentration = initial concentration ÷ factorⁿ

                        The final concentration uses the same units as the initial concentration. This assumes each step is performed accurately and mixed thoroughly.
                    """.trimIndent()
                }

                "Culture Vessel Planning" -> {
                    val desired = desiredVessels.toIntOrNull()
                    val loss = lossPercent.toDoubleOrNull()

                    if (desired == null || loss == null ||
                        desired < 1 || loss < 0.0 || loss >= 100.0
                    ) {
                        throw IllegalArgumentException(
                            "Enter at least one usable vessel and a loss allowance from 0% up to, but not including, 100%."
                        )
                    }

                    val required = ceil(
                        desired / (1.0 - loss / 100.0)
                    ).toInt()

                    val buffer = required - desired

                    result = """
                        Desired usable vessels: $desired
                        Loss allowance: ${fmt(loss)}%
                        Vessels to prepare: $required
                        Additional vessels in allowance: $buffer

                        Formula:
                        vessels to prepare = round up(
                            desired usable vessels ÷ (1 − loss fraction)
                        )

                        This is a planning estimate, not a prediction of biological contamination or survival.
                    """.trimIndent()
                }

                "Batch Cost Estimate" -> {
                    val cost = baseCost.toDoubleOrNull()
                    val waste = wastePercent.toDoubleOrNull()

                    if (cost == null || waste == null ||
                        cost < 0.0 || waste < 0.0
                    ) {
                        throw IllegalArgumentException(
                            "Enter a non-negative base cost and a non-negative waste allowance."
                        )
                    }

                    val allowance = cost * waste / 100.0
                    val total = cost + allowance

                    result = """
                        Base cost: ${fmt(cost)}
                        Waste allowance: ${fmt(allowance)}
                        Estimated batch cost: ${fmt(total)}

                        Formula:
                        allowance = base cost × waste percentage ÷ 100
                        estimated cost = base cost + allowance

                        Enter all costs in the same currency. Include only costs represented in your base estimate.
                    """.trimIndent()
                }
            }
        } catch (e: IllegalArgumentException) {
            error = true
            result = e.message ?: "Please check the values entered."
        } catch (e: Exception) {
            error = true
            result = "Unable to calculate. Check the values and try again."
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TC Lab Calculator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Scientific calculations and laboratory planning",
                style = MaterialTheme.typography.bodyMedium
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tool: $selectedTool")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    calculatorTools.forEach { tool ->
                        DropdownMenuItem(
                            text = { Text(tool) },
                            onClick = {
                                selectedTool = tool
                                menuExpanded = false
                                result = ""
                                error = false
                            }
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTool) {
                        "Stock Solution Dilution" -> {
                            Text(
                                "Dilution calculator",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Concentration units must match. Volume units must match."
                            )
                            NumberField(
                                "Stock concentration (C₁)",
                                stockConcentration
                            ) { stockConcentration = it }
                            NumberField(
                                "Target concentration (C₂)",
                                targetConcentration
                            ) { targetConcentration = it }
                            NumberField(
                                "Desired final volume (V₂)",
                                finalVolume
                            ) { finalVolume = it }
                        }

                        "Molarity to Mass" -> {
                            Text(
                                "Molarity to mass",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField("Target molarity (mol/L)", molarity) {
                                molarity = it
                            }
                            NumberField(
                                "Molecular weight (g/mol)",
                                molecularWeight
                            ) { molecularWeight = it }
                            NumberField("Final volume (mL)", molarVolume) {
                                molarVolume = it
                            }
                        }

                        "Percentage Solution (w/v)" -> {
                            Text(
                                "Percentage solution (weight/volume)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField("Target concentration (% w/v)", percentage) {
                                percentage = it
                            }
                            NumberField("Final solution volume (mL)", percentageVolume) {
                                percentageVolume = it
                            }
                        }

                        "Media Recipe Scaling" -> {
                            Text(
                                "Scale an ingredient in a recipe",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField("Original recipe volume (mL)", baseVolume) {
                                baseVolume = it
                            }
                            NumberField("Target recipe volume (mL)", targetMediaVolume) {
                                targetMediaVolume = it
                            }
                            NumberField("Original ingredient amount", ingredientAmount) {
                                ingredientAmount = it
                            }
                            Text("Ingredient unit")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("g", "mg", "mL", "µL").forEach { unit ->
                                    OutlinedButton(
                                        onClick = { ingredientUnit = unit }
                                    ) {
                                        Text(
                                            if (ingredientUnit == unit) "✓ $unit"
                                            else unit
                                        )
                                    }
                                }
                            }
                        }

                        "Agar Calculation" -> {
                            Text(
                                "Agar quantity",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField("Agar rate (g/L)", agarRate) {
                                agarRate = it
                            }
                            NumberField("Final medium volume (mL)", agarVolume) {
                                agarVolume = it
                            }
                        }

                        "Serial Dilution" -> {
                            Text(
                                "Serial dilution estimate",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField(
                                "Initial concentration",
                                initialConcentration
                            ) { initialConcentration = it }
                            NumberField("Dilution factor per step", dilutionFactor) {
                                dilutionFactor = it
                            }
                            NumberField("Number of steps (whole number)", dilutionSteps) {
                                dilutionSteps = it
                            }
                        }

                        "Culture Vessel Planning" -> {
                            Text(
                                "Plan vessel quantities",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField(
                                "Desired usable vessels",
                                desiredVessels,
                                integer = true
                            ) { desiredVessels = it }
                            NumberField("Loss allowance (%)", lossPercent) {
                                lossPercent = it
                            }
                        }

                        "Batch Cost Estimate" -> {
                            Text(
                                "Batch cost estimate",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NumberField("Base batch cost", baseCost) {
                                baseCost = it
                            }
                            NumberField("Waste allowance (%)", wastePercent) {
                                wastePercent = it
                            }
                            Text(
                                "Use one currency consistently. This estimates an allowance; it does not determine the actual amount of waste."
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { calculate() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate")
                    }
                }
            }

            if (result.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (error) "Check your inputs" else "Result",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (error) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Text(
                text = "Educational calculator. Verify units, reagent specifications, source protocols, and laboratory procedures before practical use. Calculations alone do not validate a biological protocol.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    integer: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (integer) {
                KeyboardType.Number
            } else {
                KeyboardType.Decimal
            }
        )
    )
}

private fun fmt(value: Double): String {
    if (!value.isFinite()) return "Out of range"
    return String.format(Locale.US, "%.4f", value)
        .trimEnd('0')
        .trimEnd('.')
}
```
