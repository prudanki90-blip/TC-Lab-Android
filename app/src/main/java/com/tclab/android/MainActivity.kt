
package com.tclab.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val LabBackground = Color(0xFF101613)
private val LabSurface = Color(0xFF19231D)
private val LabCard = Color(0xFF202D25)
private val LabMint = Color(0xFF9AD6B0)
private val LabMuted = Color(0xFFB8C8BC)

private val plantNames = listOf(
    "Apple",
    "Pomegranate",
    "Seedless Breadfruit",
    "Banana / Plantain",
    "Pineapple",
    "Passion Fruit"
)

private val workspaces = listOf(
    "Protocols",
    "Calculations",
    "Research Notebook",
    "Culture Records",
    "Timeline & Reminders"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = LabMint,
                    secondary = LabMuted,
                    background = LabBackground,
                    surface = LabSurface,
                    onBackground = Color.White,
                    onSurface = Color.White
                ),
                shapes = Shapes(
                    medium = RoundedCornerShape(16.dp),
                    large = RoundedCornerShape(22.dp)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LabBackground
                ) {
                    TCLabApp()
                }
            }
        }
    }
}

@Composable
fun TCLabApp() {
    var screen by remember { mutableStateOf("Home") }
    var selectedPlant by remember { mutableStateOf(plantNames[0]) }
    var selectedWorkspace by remember { mutableStateOf("Protocols") }

    when (screen) {
        "Home" -> HomeScreen(
            onNavigate = { screen = it }
        )

        "Plant Library" -> PlantLibraryScreen(
            onBack = { screen = "Home" },
            onSelectPlant = {
                selectedPlant = it
                screen = "Plant Workspace"
            }
        )

        "Plant Workspace" -> PlantWorkspaceScreen(
            plant = selectedPlant,
            selectedWorkspace = selectedWorkspace,
            onSelectWorkspace = {
                selectedWorkspace = it
            },
            onBack = { screen = "Plant Library" }
        )

        "Calculators" -> CalculatorScreen(
            onBack = { screen = "Home" }
        )

        "Culture Protocols" -> {
            SimpleWorkspaceScreen(
                title = "Culture Protocols",
                description = "Store protocol notes, sources and revisions.",
                onBack = { screen = "Home" }
            )
        }

        "Media & Chemicals" -> {
            SimpleWorkspaceScreen(
                title = "Media & Chemicals",
                description = "Organize media formulations, chemical references and preparation notes.",
                onBack = { screen = "Home" }
            )
        }

        "Experiments" -> {
            SimpleWorkspaceScreen(
                title = "Experiments",
                description = "Use the plant workspaces to organize culture records, observations and timelines.",
                onBack = { screen = "Home" }
            )
        }

        "Equipment" -> {
            SimpleWorkspaceScreen(
                title = "Equipment",
                description = "Record equipment specifications, maintenance notes and estimated costs.",
                onBack = { screen = "Home" }
            )
        }

        "Tasks & Reminders" -> {
            SimpleWorkspaceScreen(
                title = "Tasks & Reminders",
                description = "Open a plant workspace to record and manage manual reminders.",
                onBack = { screen = "Home" }
            )
        }

        "Research Notebook" -> {
            SimpleWorkspaceScreen(
                title = "Research Notebook",
                description = "Open a plant workspace to keep research notes and observations.",
                onBack = { screen = "Home" }
            )
        }

        else -> HomeScreen(onNavigate = { screen = it })
    }
}

@Composable
fun LabHeader(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "TC LAB  /  TISSUE CULTURE",
            color = LabMint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Text(
            text = title,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = subtitle,
            color = LabMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LabHeader(
            title = "Laboratory Hub",
            subtitle = "Plan cultures. Record evidence. Track progress."
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = LabCard
            ),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "OFFLINE-FIRST WORKSPACE",
                    color = LabMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Your laboratory, organized.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Keep plant-specific protocols, calculations, " +
                        "culture records and research notes organized.",
                    color = LabMuted
                )

                Button(
                    onClick = { onNavigate("Plant Library") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Plant Library →")
                }
            }
        }

        Text(
            "LABORATORY MODULES",
            color = LabMint,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )

        LabCard(
            "Plant Library",
            "Six plant workspaces with five organized work areas."
        ) {
            onNavigate("Plant Library")
        }

        LabCard(
            "Calculators",
            "Molarity, dilution, grams per litre and survival."
        ) {
            onNavigate("Calculators")
        }

        LabCard(
            "Culture Protocols",
            "Review and organize protocol information."
        ) {
            onNavigate("Culture Protocols")
        }

        LabCard(
            "Media & Chemicals",
            "Formulations, chemical references and preparation notes."
        ) {
            onNavigate("Media & Chemicals")
        }

        LabCard(
            "Experiments",
            "Organize your work by plant and culture batch."
        ) {
            onNavigate("Experiments")
        }

        LabCard(
            "Equipment",
            "Equipment specifications, maintenance and costs."
        ) {
            onNavigate("Equipment")
        }

        LabCard(
            "Tasks & Reminders",
            "Manage manually entered laboratory tasks."
        ) {
            onNavigate("Tasks & Reminders")
        }

        LabCard(
            "Research Notebook",
            "Record observations and separate research from measurements."
        ) {
            onNavigate("Research Notebook")
        }

        Text(
            "Scientific note: verify protocols, sterilization procedures, " +
                "chemical handling and culture conditions against suitable sources.",
            color = LabMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LabCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LabSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(66.dp)
                    .background(
                        LabMint,
                        RoundedCornerShape(4.dp)
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    description,
                    fontSize = 13.sp,
                    color = LabMuted
                )

                Text(
                    "OPEN WORKSPACE  →",
                    color = LabMint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlantLibraryScreen(
    onBack: () -> Unit,
    onSelectPlant: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackButton(onBack)

        LabHeader(
            title = "World Plant Library",
            subtitle = "Select a plant to open its dedicated workspace."
        )

        plantNames.forEachIndexed { index, plant ->
            Card(
                onClick = { onSelectPlant(plant) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = LabSurface
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "%02d".format(index + 1),
                        color = LabMint,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            plant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Protocols · Calculations · Research · Records · Timeline",
                            fontSize = 12.sp,
                            color = LabMuted
                        )
                    }

                    Text("→", color = LabMint)
                }
            }
        }

        Text(
            "Plant-specific scientific protocols and reference data " +
                "must be added and verified separately.",
            color = LabMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PlantWorkspaceScreen(
    plant: String,
    selectedWorkspace: String,
    onSelectWorkspace: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BackButton(onBack)

        LabHeader(
            title = plant,
            subtitle = "Plant research workspace"
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "WORK AREAS",
            color = LabMint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(workspaces) { workspace ->
                Card(
                    onClick = { onSelectWorkspace(workspace) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (
                            selectedWorkspace == workspace
                        ) LabCard else LabSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            if (selectedWorkspace == workspace) "●" else "○",
                            color = LabMint
                        )

                        Text(
                            workspace,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )

                        Text("→", color = LabMint)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        when (selectedWorkspace) {
            "Protocols" -> ProtocolWorkspace(plant)
            "Calculations" -> CalculatorScreen(onBack = {})
            "Research Notebook" -> NotebookWorkspace(plant)
            "Culture Records" -> CultureRecordWorkspace(plant)
            "Timeline & Reminders" -> TimelineWorkspace(plant)
        }
    }
}

@Composable
fun BackButton(onBack: () -> Unit) {
    TextButton(onClick = onBack) {
        Text("← Back")
    }
}

@Composable
fun SimpleWorkspaceScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackButton(onBack)

        LabHeader(title, description)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = LabSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "WORKSPACE",
                    color = LabMint,
                    fontWeight = FontWeight.Bold
                )

                Text(description)

                Text(
                    "For plant-specific work, open the Plant Library. " +
                        "This module is not yet a complete scientific database.",
                    color = LabMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Local storage helpers

private fun preferences(context: Context) =
    context.getSharedPreferences("tc_lab_records", Context.MODE_PRIVATE)

private fun storageKey(plant: String, type: String) =
    "${type}_${plant}"

private fun readText(
    context: Context,
    plant: String,
    type: String
): String {
    return preferences(context).getString(
        storageKey(plant, type), ""
    ) ?: ""
}

private fun saveText(
    context: Context,
    plant: String,
    type: String,
    value: String
) {
    preferences(context).edit()
        .putString(storageKey(plant, type), value)
        .apply()
}

data class ProtocolEntry(
    val title: String,
    val source: String,
    val notes: String
)

private fun readProtocols(
    context: Context,
    plant: String
): List<ProtocolEntry> {
    val raw = readText(context, plant, "protocols")
    if (raw.isBlank()) return emptyList()

    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            ProtocolEntry(
                item.optString("title"),
                item.optString("source"),
                item.optString("notes")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveProtocols(
    context: Context,
    plant: String,
    protocols: List<ProtocolEntry>
) {
    val array = JSONArray()

    protocols.forEach { protocol ->
        array.put(
            JSONObject().apply {
                put("title", protocol.title)
                put("source", protocol.source)
                put("notes", protocol.notes)
            }
        )
    }

    saveText(
        context,
        plant,
        "protocols",
        array.toString()
    )
}

@Composable
fun ProtocolWorkspace(plant: String) {
    val context = LocalContext.current

    var title by remember(plant) { mutableStateOf("") }
    var source by remember(plant) { mutableStateOf("") }
    var notes by remember(plant) { mutableStateOf("") }

    var protocols by remember(plant) {
        mutableStateOf(readProtocols(context, plant))
    }

    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 440.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Protocol Register",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Text(
            "Plant: $plant",
            color = LabMint,
            fontSize = 13.sp
        )

        LabInput("Protocol title", title) { title = it }

        LabInput("Source or reference URL", source) {
            source = it
        }

        LabInput(
            "Protocol steps, conditions and revision notes",
            notes,
            singleLine = false
        ) { notes = it }

        
        Button(
            onClick = {
                if (title.isBlank()) {
                    message = "Enter a protocol title first."
                } else {
                    val updated = protocols + ProtocolEntry(
                        title.trim(),
                        source.trim(),
                        notes.trim()
                    )
                    saveProtocols(context, plant, updated)
                    protocols = updated
                    title = ""
                    source = ""
                    notes = ""
                    message = "Protocol saved on this device."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Protocol")
        }

        if (message.isNotBlank()) {
            Text(message, color = LabMint, fontSize = 13.sp)
        }

        Text(
            "SAVED PROTOCOLS (${protocols.size})",
            color = LabMint,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        if (protocols.isEmpty()) {
            Text(
                "No protocols saved for this plant yet.",
                color = LabMuted,
                fontSize = 13.sp
            )
        } else {
            protocols.forEachIndexed { index, protocol ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = LabSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "${index + 1}. ${protocol.title}",
                            fontWeight = FontWeight.Bold
                        )

                        if (protocol.source.isNotBlank()) {
                            Text(
                                "Source: ${protocol.source}",
                                color = LabMint,
                                fontSize = 12.sp
                            )
                        }

                        if (protocol.notes.isNotBlank()) {
                            Text(protocol.notes, color = LabMuted)
                        }

                        TextButton(
                            onClick = {
                                val updated = protocols.filterIndexed { i, _ ->
                                    i != index
                                }
                                saveProtocols(context, plant, updated)
                                protocols = updated
                                message = "Protocol removed."
                            }
                        ) {
                            Text("Delete protocol")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabInput(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LabMint,
            focusedLabelColor = LabMint,
            cursorColor = LabMint
        )
    )
}
