package com.tclab.android
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

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

@Composable
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
        Text(
            "TC LAB Calculators",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }

        HorizontalDivider()

        Text("Dilution Calculator", style = MaterialTheme.typography.titleLarge)
        Text("Use C1 × V1 = C2 × V2. Keep concentration units consistent.")

        workspaceField(
            "Stock concentration (C1)",
            stockConcentration,
            { stockConcentration = it }
        )

        workspaceField(
            "Target concentration (C2)",
            targetConcentration,
            { targetConcentration = it }
        )

        workspaceField(
            "Final volume (V2, mL)",
            finalVolume,
            { finalVolume = it }
        )

        Text("Stock volume required: $dilutionResult")

        HorizontalDivider()

        Text("Molarity to Mass", style = MaterialTheme.typography.titleLarge)
        Text(
            "Mass (g) = molarity (mol/L) × molecular weight (g/mol) × volume (L)."
        )

        workspaceField("Molarity (mol/L)", molarity, { molarity = it })
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

@Composable
fun NotebookWorkspace(plant: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val key = remember(plant) { workspaceKey("notebook", plant) }

    var entries by remember(plant) {
        mutableStateOf(readWorkspaceArray(context, key).toString())
    }

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }

    val records = remember(entries, refresh) {
        try {
            readWorkspaceArray(context, key)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("$plant Research Notebook", style = MaterialTheme.typography.headlineSmall)
        Text("Record observations, questions, methods and research findings.")

        workspaceField("Entry title", title, { title = it })
        workspaceField(
            "Notes and observations",
            notes,
            { notes = it },
            singleLine = false
        )

        Button(
            onClick = {
                if (title.isNotBlank() || notes.isNotBlank()) {
                    val array = readWorkspaceArray(context, key)
                    array.put(
                        JSONObject()
                            .put("title", title.trim())
                            .put("notes", notes.trim())
                            .put("date", LocalDate.now().toString())
                    )
                    saveWorkspaceArray(context, key, array)
                    entries = array.toString()
                    title = ""
                    notes = ""
                    refresh++
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save notebook entry")
        }

        HorizontalDivider()
        Text("Saved entries: ${records.length()}", style = MaterialTheme.typography.titleMedium)

        for (i in records.length() - 1 downTo 0) {
            val record = records.optJSONObject(i) ?: continue
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        workspaceText(record, "title").ifBlank { "Untitled entry" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(workspaceText(record, "date"))
                    Text(workspaceText(record, "notes"))
                    TextButton(
                        onClick = {
                            val updated = readWorkspaceArray(context, key)
                            updated.remove(i)
                            saveWorkspaceArray(context, key, updated)
                            entries = updated.toString()
                            refresh++
                        }
                    ) {
                        Text("Delete entry")
                    }
                }
            }
        }
    }
}

@Composable
fun CultureRecordWorkspace(plant: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val key = remember(plant) { workspaceKey("culture_records", plant) }

    var batchName by remember { mutableStateOf("") }
    var medium by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var observations by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }

    val records = remember(plant, refresh) {
        readWorkspaceArray(context, key)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("$plant Culture Records", style = MaterialTheme.typography.headlineSmall)
        Text("Track batches, culture medium and observations.")

        workspaceField("Batch or culture ID", batchName, { batchName = it })
        workspaceField("Medium / formulation", medium, { medium = it })
        workspaceField("Start date (YYYY-MM-DD)", startDate, { startDate = it })
        workspaceField(
            "Observations",
            observations,
            { observations = it },
            singleLine = false
        )

        Button(
            onClick = {
                if (batchName.isNotBlank()) {
                    val array = readWorkspaceArray(context, key)
                    array.put(
                        JSONObject()
                            .put("batch", batchName.trim())
                            .put("medium", medium.trim())
                            .put("startDate", startDate.trim())
                            .put("observations", observations.trim())
                            .put("status", "Active")
                    )
                    saveWorkspaceArray(context, key, array)
                    batchName = ""
                    medium = ""
                    observations = ""
                    refresh++
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save culture record")
        }

        HorizontalDivider()
        Text("Records: ${records.length()}", style = MaterialTheme.typography.titleMedium)

        for (i in records.length() - 1 downTo 0) {
            val record = records.optJSONObject(i) ?: continue
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        workspaceText(record, "batch"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Medium: ${workspaceText(record, "medium")}")
                    Text("Start date: ${workspaceText(record, "startDate")}")
                    Text("Status: ${workspaceText(record, "status")}")
                    Text(workspaceText(record, "observations"))

                    TextButton(
                        onClick = {
                            val updated = readWorkspaceArray(context, key)
                            updated.remove(i)
                            saveWorkspaceArray(context, key, updated)
                            refresh++
                        }
                    ) {
                        Text("Delete record")
                    }
                }
            }
        }
    }
}

private fun timelineStatus(dueDate: String, completed: Boolean): String {
    if (completed) return "Completed"

    val due = try {
        LocalDate.parse(dueDate)
    } catch (_: Exception) {
        return "Date needs checking"
    }

    return when {
        due.isBefore(LocalDate.now()) -> "Overdue"
        due.isEqual(LocalDate.now()) -> "Due today"
        else -> "Upcoming"
    }
}

@Composable
fun TimelineWorkspace(plant: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val key = remember(plant) { workspaceKey("timeline", plant) }

    var taskName by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var details by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }

    val records = remember(plant, refresh) {
        readWorkspaceArray(context, key)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("$plant Timeline & Reminders", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Create dated tasks and track their status. These are in-app records; " +
                "Android notifications are not enabled by this screen."
        )

        workspaceField("Task name", taskName, { taskName = it })
        workspaceField("Due date (YYYY-MM-DD)", dueDate, { dueDate = it })
        workspaceField("Details", details, { details = it }, singleLine = false)

        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    val array = readWorkspaceArray(context, key)
                    array.put(
                        JSONObject()
                            .put("task", taskName.trim())
                            .put("dueDate", dueDate.trim())
                            .put("details", details.trim())
                            .put("completed", false)
                            .put("history", JSONArray())
                    )
                    saveWorkspaceArray(context, key, array)
                    taskName = ""
                    details = ""
                    refresh++
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add timeline task")
        }

        HorizontalDivider()
        Text("Tasks: ${records.length()}", style = MaterialTheme.typography.titleMedium)

        for (i in records.length() - 1 downTo 0) {
            val record = records.optJSONObject(i) ?: continue
            val completed = record.optBoolean("completed", false)
            val date = workspaceText(record, "dueDate")
            val status = timelineStatus(date, completed)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        workspaceText(record, "task"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Due: $date")
                    Text("Status: $status")
                    Text(workspaceText(record, "details"))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!completed) {
                            TextButton(
                                onClick = {
                                    val updated = readWorkspaceArray(context, key)
                                    val item = updated.optJSONObject(i)
                                    if (item != null) {
                                        item.put("completed", true)
                                        val history = item.optJSONArray("history") ?: JSONArray()
                                        history.put(
                                            JSONObject()
                                                .put("action", "Completed")
                                                .put("date", LocalDate.now().toString())
                                        )
                                        item.put("history", history)
                                        saveWorkspaceArray(context, key, updated)
                                        refresh++
                                    }
                                }
                            ) {
                                Text("Complete")
                            }
                        }

                        TextButton(
                            onClick = {
                                val updated = readWorkspaceArray(context, key)
                                updated.remove(i)
                                saveWorkspaceArray(context, key, updated)
                                refresh++
                            }
                        ) {
                            Text("Delete")
                        }
                    }

                    val history = record.optJSONArray("history") ?: JSONArray()
                    if (history.length() > 0) {
                        Text("History", style = MaterialTheme.typography.labelLarge)
                        for (h in 0 until history.length()) {
                            val event = history.optJSONObject(h) ?: continue
                            Text(
                                "${workspaceText(event, "action")} — " +
                                    workspaceText(event, "date"),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }




@Composable
fun PlantSelectionScreen(
    onSelectPlant: (String) -> Unit
) {
    val plants = listOf(
        "Apple",
        "Banana",
        "Passion fruit",
        "Breadfruit",
        "Pineapple"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101820)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "TC LAB",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF62D6A5)
            )

            Text(
                text = "Plant Tissue Culture Laboratory",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Text(
                text = "Choose a plant to open its laboratory workspace.",
                color = Color(0xFFB0BEC9)
            )

            plants.forEach { plant ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B2935)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = plant,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Button(
                            onClick = { onSelectPlant(plant) }
                        ) {
                            Text("Open")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlantWorkspaceScreen(
    plant: String,
    onBack: () -> Unit,
    onOpenProtocols: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenCultures: () -> Unit,
    onOpenTimeline: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101820)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back to plant selection")
            }

            Text(
                text = plant,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF62D6A5)
            )

            Text(
                text = "Laboratory Workspace",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Text(
                text = "Choose a laboratory tool or record.",
                color = Color(0xFFB0BEC9)
            )

            WorkspaceMenuItem(
                title = "Protocol Library",
                description = "Browse protocols and start a culture batch.",
                onClick = onOpenProtocols
            )

            WorkspaceMenuItem(
                title = "Culture Calculator",
                description = "Open the existing laboratory calculator.",
                onClick = onOpenCalculator
            )

            WorkspaceMenuItem(
                title = "Research Notebook",
                description = "Record research notes for $plant.",
                onClick = onOpenNotebook
            )

            WorkspaceMenuItem(
                title = "Culture Records",
                description = "Manage records for $plant.",
                onClick = onOpenCultures
            )

            WorkspaceMenuItem(
                title = "Culture Timeline",
                description = "Review scheduled tasks and progress.",
                onClick = onOpenTimeline
            )
        }
    }
}

@Composable
private fun WorkspaceMenuItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B2935)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF62D6A5)
            )

            Text(
                text = description,
                color = Color(0xFFB0BEC9)
            )

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open")
            }
        }
    }
}


    

