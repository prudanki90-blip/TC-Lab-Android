
package com.tclab.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF9AD6B0),
                    secondary = Color(0xFFB8DCC8),
                    background = Color(0xFF101613),
                    surface = Color(0xFF19231D)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TCLabApp()
                }
            }
        }
    }
}

@Composable
fun TCLabApp() {
    var currentScreen by remember { mutableStateOf("Home") }

    when (currentScreen) {
        "Home" -> HomeScreen(
            onNavigate = { currentScreen = it }
        )

        "Calculators" -> CalculatorScreen(
            onBack = { currentScreen = "Home" }
        )

        else -> PlaceholderScreen(
            title = currentScreen,
            onBack = { currentScreen = "Home" }
        )
    }
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "TC LAB",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Tissue Culture Laboratory",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "INTERACTIVE STARTER VERSION",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Text("Plan experiments. Calculate accurately. Learn from evidence.")

        SectionCard(
            title = "Plant Library",
            description = "Explore apple rootstocks, pomegranate, breadfruit, banana, pineapple and passion fruit."
        ) {
            onNavigate("Plant Library")
        }

        SectionCard(
            title = "Culture Protocols",
            description = "Organize research sources, protocol steps and revisions."
        ) {
            onNavigate("Culture Protocols")
        }

        SectionCard(
            title = "Media & Chemicals",
            description = "Plan media formulations and review laboratory chemical information."
        ) {
            onNavigate("Media & Chemicals")
        }

        SectionCard(
            title = "Calculators",
            description = "Calculate molarity, dilutions, g/L concentrations and survival percentages."
        ) {
            onNavigate("Calculators")
        }

        SectionCard(
            title = "Experiments",
            description = "Prepare a workspace for batches, observations and culture history."
        ) {
            onNavigate("Experiments")
        }

        SectionCard(
            title = "Equipment",
            description = "Organize equipment specifications, safety notes and costs."
        ) {
            onNavigate("Equipment")
        }

        SectionCard(
            title = "Tasks & Reminders",
            description = "Plan future culture checks and laboratory tasks."
        ) {
            onNavigate("Tasks & Reminders")
        }

        SectionCard(
            title = "Research Notebook",
            description = "Separate published evidence, your measurements and calculated results."
        ) {
            onNavigate("Research Notebook")
        }

        Text(
            text = "Important: Verify scientific methods and safety requirements against appropriate sources. This starter version does not save laboratory records.",
            fontSize = 13.sp
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap to open →",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Home")
        }

        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "This section is part of the TC Lab development plan."
        )

        Text(
            text = "The navigation is working, but this section does not yet contain connected catalogue data, saved records or reminders."
        )

        Text(
            text = "We will implement each section in stages and test it before adding the next feature."
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return Home")
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf("Molarity") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Home")
        }

        Text(
            text = "Lab Calculators",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text("Choose a calculator.")

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Molarity", "Dilution", "g/L", "Survival").forEach { option ->
                if (selected == option) {
                    Button(onClick = { selected = option }) {
                        Text(option)
                    }
                } else {
                    TextButton(onClick = { selected = option }) {
                        Text(option)
                    }
                }
            }
        }

        when (selected) {
            "Molarity" -> MolarityCalculator()
            "Dilution" -> DilutionCalculator()
            "g/L" -> ConcentrationCalculator()
            "Survival" -> SurvivalCalculator()
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Check input units, source methods and chemical safety before using a calculated result in laboratory work.",
            fontSize = 13.sp
        )
    }
}

@Composable
fun MolarityCalculator() {
    var mass by remember { mutableStateOf("") }
    var molecularWeight by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Text(
        text = "Molarity",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Text("Calculate moles per litre from mass, molecular weight and final solution volume.")

    NumberField("Chemical mass (g)", mass) {
        mass = it
        result = ""
    }

    NumberField("Molecular weight (g/mol)", molecularWeight) {
        molecularWeight = it
        result = ""
    }

    NumberField("Final solution volume (mL)", volume) {
        volume = it
        result = ""
    }

    Button(
        onClick = {
            val m = mass.toDoubleOrNull()
            val mw = molecularWeight.toDoubleOrNull()
            val v = volume.toDoubleOrNull()

            result = if (
                m != null && mw != null && v != null &&
                m >= 0 && mw > 0 && v > 0
            ) {
                val molarity = (m / mw) / (v / 1000.0)
                "Result: %.6f mol/L".format(molarity)
            } else {
                "Enter valid numbers. Molecular weight and volume must be greater than zero."
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Calculate Molarity")
    }

    if (result.isNotEmpty()) Text(result)
}

@Composable
fun DilutionCalculator() {
    var stock by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var finalVolume by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Text(
        text = "Dilution Calculator",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Text("Uses C1 × V1 = C2 × V2. Concentrations must use the same units.")

    NumberField("Stock concentration (C1)", stock) {
        stock = it
        result = ""
    }

    NumberField("Target concentration (C2)", target) {
        target = it
        result = ""
    }

    NumberField("Desired final volume (mL)", finalVolume) {
        finalVolume = it
        result = ""
    }

    Button(
        onClick = {
            val c1 = stock.toDoubleOrNull()
            val c2 = target.toDoubleOrNull()
            val v2 = finalVolume.toDoubleOrNull()

            result = if (
                c1 != null && c2 != null && v2 != null &&
                c1 > 0 && c2 >= 0 && c2 <= c1 && v2 > 0
            ) {
                val v1 = (c2 * v2) / c1
                val solvent = v2 - v1

                "Stock solution required: %.4f mL".format(v1) +
                    "\nSolvent to add approximately: %.4f mL".format(solvent) +
                    "\nBring the mixture to the final volume of %.4f mL.".format(v2)
            } else {
                "Check your values. Stock must be positive, target must be between zero and stock, and final volume must be positive."
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Calculate Dilution")
    }

    if (result.isNotEmpty()) Text(result)
}

@Composable
fun ConcentrationCalculator() {
    var mass by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Text(
        text = "Mass Concentration",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Text("Calculate grams per litre using the final solution volume.")

    NumberField("Mass of substance (g)", mass) {
        mass = it
        result = ""
    }

    NumberField("Final solution volume (L)", volume) {
        volume = it
        result = ""
    }

    Button(
        onClick = {
            val m = mass.toDoubleOrNull()
            val v = volume.toDoubleOrNull()

            result = if (m != null && v != null && m >= 0 && v > 0) {
                "Result: %.4f g/L".format(m / v)
            } else {
                "Enter a non-negative mass and a final volume greater than zero."
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Calculate g/L")
    }

    if (result.isNotEmpty()) Text(result)
}

@Composable
fun SurvivalCalculator() {
    var survived by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Text(
        text = "Culture Survival",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Text("Enter the number of surviving cultures and the total assessed.")

    NumberField("Number surviving", survived) {
        survived = it
        result = ""
    }

    NumberField("Total assessed", total) {
        total = it
        result = ""
    }

    Button(
        onClick = {
            val s = survived.toDoubleOrNull()
            val t = total.toDoubleOrNull()

            result = if (
                s != null && t != null &&
                s >= 0 && t > 0 && s <= t
            ) {
                "Survival: %.2f%%".format((s / t) * 100.0)
            } else {
                "Enter valid counts: total must be positive and survivors cannot exceed the total."
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Calculate Survival")
    }

    if (result.isNotEmpty()) Text(result)
}
