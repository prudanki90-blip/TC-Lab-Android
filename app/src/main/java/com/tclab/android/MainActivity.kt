
package com.tclab.android

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeParseException

private fun workspacePrefs(context: Context): SharedPreferences =
    context.getSharedPreferences(
        "tc_lab_workspace_records_v1",
        Context.MODE_PRIVATE
    )

private fun workspaceKey(section: String, plant: String): String =
    "${section}_${plant}"

private fun readWorkspaceArray(
    context: Context,
    key: String
): JSONArray {
    val raw = workspacePrefs(context).getString(key, "[]")
    return try {
        JSONArray(raw ?: "[]")
    } catch (_: Exception) {
        JSONArray()
    }
}

private fun saveWorkspaceArray(
    context: Context,
    key: String,
    array: JSONArray
) {
    workspacePrefs(context).edit()
        .putString(key, array.toString())
        .apply()
}

private fun workspaceText(
    obj: JSONObject,
    key: String
): String = obj.optString(key, "")

private fun workspaceField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine
    )
}

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var stockConcentration by remember { mutableStateOf("1.0") }
    var targetConcentration by remember { mutableStateOf("0.1") }
    var finalVolume by remember { mutableStateOf("100") }

    var molarity by remember { mutableStateOf("0.1") }
    var molecularWeight by remember { mutableStateOf("180.16") }
    var solutionVolume by remember { mutableStateOf("0.1") }

    val dilutionResult = remember(
        stockConcentration,
        targetConcentration,
        finalVolume
    ) {
        val c1 = stockConcentration.toDoubleOrNull()
        val c2 = targetConcentration.toDoubleOrNull()
        val v2 = finalVolume.toDoubleOrNull()

        if (c1 != null && c2 != null && v2 != null &&
            c1 > 0 && c2 >= 0 && v2 > 0 && c2 <= c1
        ) {
            "%.3f mL".format((c2 * v2) / c1)
        } else {
            "Check inputs"
        }
    }

    val massResult = remember(
        molarity,
        molecularWeight,
        solutionVolume
    ) {
        val m = molarity.toDoubleOrNull()
        val mw = molecularWeight.toDoubleOrNull()
        val volume = solutionVolume.toDoubleOrNull()

        if (m != null && mw != null && volume != null &&
            m >= 0 && mw > 0 && volume > 0
        ) {
            "%.4f g".format(m * mw * volume)
        } else {
            "Check inputs"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TC LAB Calculators", style = MaterialTheme.typography.headlineSmall)

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }

        HorizontalDivider()

        Text("Dilution Calculator", style = MaterialTheme.typography.titleLarge)
        Text("Use C1 × V1 = C2 × V2. Keep concentration units consistent.")

        workspaceField(
            "Stock concentration (C1)",
            stockConcentration,
            { stockConcentration = it },
            singleLine = true
        )

        workspaceField(
            "Target concentration (C2)",
            targetConcentration,
            { targetConcentration = it },
            singleLine = true
        )

        workspaceField(
            "Final volume (V2, mL)",
            finalVolume,
            { finalVolume = it },
            singleLine = true
        )

        Text("Stock volume required: $dilutionResult")

        HorizontalDivider()

        Text("Molarity to Mass", style = MaterialTheme.typography.titleLarge)
        Text("Mass (g) = molarity (mol/L) × molecular weight (g/mol) × volume (L).")

        workspaceField(
            "Molarity (mol/L)",
            molarity,
            { molarity = it }
        )

        workspaceField(
            "Molecular weight (g/mol)",
            molecularWeight,
            { molecularWeight = it }
        )

        workspaceField(
            "Solution volume (L)",
            solutionVolume,
            { solutionVolume = it }
        )

        Text("Required mass: $massResult")
        Text(
            "Verify chemical identity, units and preparation requirements " +
                "against a reliable laboratory protocol before preparing a solution.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
