package com.tclab.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

// ============================================================
// TC LAB — TISSUE CULTURE LABORATORY
// MainActivity.kt
// Section 1: Application foundation and theme
// ============================================================

private val TCLabBackground = Color(0xFF101820)
private val TCLabSurface = Color(0xFF1B2935)
private val TCLabPrimary = Color(0xFF62D6A5)
private val TCLabSecondary = Color(0xFF8CB8FF)
private val TCLabText = Color(0xFFF1F5F9)
private val TCLabMuted = Color(0xFFB0BEC9)
private val TCLabWarning = Color(0xFFFFC857)
private val TCLabDanger = Color(0xFFFF7979)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TCLabTheme {
                TCLabApp()
            }
        }
    }
}

@Composable
private fun TCLabTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = TCLabPrimary,
        secondary = TCLabSecondary,
        background = TCLabBackground,
        surface = TCLabSurface,
        onPrimary = Color(0xFF09251B),
        onSecondary = Color(0xFF10213B),
        onBackground = TCLabText,
        onSurface = TCLabText,
        error = TCLabDanger
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// The application navigation and screens will be connected
// in the following sections of this file.


@Composable
fun TCLabApp() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val store = remember(context) {
        CultureLabStore(context)
    }

    var selectedScreen by remember {
        mutableStateOf("Plant Selection")
    }

    var selectedPlant by remember {
        mutableStateOf("Apple")
    }

    var batches by remember(context, store) {
        mutableStateOf(store.loadBatches())
    }

    LaunchedEffect(batches) {
        store.saveBatches(batches)
    }

    when (selectedScreen) {
        "Plant Selection" -> {
            PlantSelectionScreen(
                onSelectPlant = { plant ->
                    selectedPlant = plant
                    selectedScreen = "Plant Workspace"
                }
            )
        }

        "Plant Workspace" -> {
            PlantWorkspaceScreen(
                plant = selectedPlant,
                onBack = {
                    selectedScreen = "Plant Selection"
                },
                onOpenProtocols = {
                    selectedScreen = "Protocols"
                },
                onOpenCalculator = {
                    selectedScreen = "Calculator"
                },
                onOpenNotebook = {
                    selectedScreen = "Notebook"
                },
                onOpenCultures = {
                    selectedScreen = "Culture Records"
                },
                onOpenTimeline = {
                    selectedScreen = "Timeline"
                }
            )
        }

        "Calculator" -> {
            // CalculatorScreen already has its own Back control.
            CalculatorScreen(
                onBack = {
                    selectedScreen = "Plant Workspace"
                }
            )
        }

        "Notebook" -> {
            WorkspacePage(
                title = "$selectedPlant Research Notebook",
                onBack = {
                    selectedScreen = "Plant Workspace"
                }
            ) {
                NotebookWorkspace(
                    plant = selectedPlant
                )
            }
        }

        "Culture Records" -> {
            WorkspacePage(
                title = "$selectedPlant Culture Records",
                onBack = {
                    selectedScreen = "Plant Workspace"
                }
            ) {
                CultureRecordWorkspace(
                    plant = selectedPlant
                )
            }
        }

        "Protocols" -> {
            ProtocolLibraryScreen(
                onBatchCreated = { newBatch ->
                    batches = batches.filterNot {
                        it.id == newBatch.id
                    } + newBatch

                    selectedScreen = "Timeline"
                },
                onBack = {
                    selectedScreen = "Plant Workspace"
                }
            )
        }

        "Timeline" -> {
            CultureTimelineScreen(
                batches = batches,
                onCompleteTask = { batchId, taskId, note ->
                    batches = batches.map { batch ->
                        if (batch.id != batchId) {
                            batch
                        } else {
                            batch.copy(
                                tasks = batch.tasks.map { task ->
                                    if (task.id != taskId) {
                                        task
                                    } else {
                                        task.copy(
                                            completedAtMillis =
                                                System.currentTimeMillis(),
                                            completionNote = note,
                                            status =
                                                CultureTaskStatus.COMPLETED
                                        )
                                    }
                                }
                            )
                        }
                    }

                    store.recordHistory(
                        taskId = taskId,
                        action = "Completed",
                        note = note
                    )
                },
                onRescheduleTask = {
                    batchId, taskId, newTime, note ->
                    batches = batches.map { batch ->
                        if (batch.id != batchId) {
                            batch
                        } else {
                            batch.copy(
                                tasks = batch.tasks.map { task ->
                                    if (task.id != taskId) {
                                        task
                                    } else {
                                        task.copy(
                                            scheduledAtMillis = newTime,
                                            status =
                                                CultureTaskStatus.RESCHEDULED,
                                            completionNote = note
                                        )
                                    }
                                }
                            )
                        }
                    }

                    store.recordHistory(
                        taskId = taskId,
                        action = "Rescheduled",
                        note = note
                    )
                },
                onBack = {
                    selectedScreen = "Plant Workspace"
                }
            )
        }

        else -> {
            // Render a fallback without changing state during composition.
            WorkspacePage(
                title = "TC Lab",
                onBack = {
                    selectedScreen = "Plant Selection"
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose a plant to continue.",
                        color = TCLabText
                    )

                    Button(
                        onClick = {
                            selectedScreen = "Plant Selection"
                        }
                    ) {
                        Text("Plant Selection")
                    }
                }
            }
        }
    }
}


@Composable
private fun WorkspacePage(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TCLabBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }

            Text(
                text = title,
                color = TCLabPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {
            content()
        }
    }
}






    

/*
 * ============================================================
 * SECTION 2: PROTOCOL MODELS AND CULTURE TIMELINE DATA
 * ============================================================
 */

enum class CultureTaskStatus {
    UPCOMING,
    DUE,
    OVERDUE,
    COMPLETED,
    RESCHEDULED
}

data class ProtocolStep(
    val id: String,
    val title: String,
    val description: String,
    val dayOffset: Int,
    val repeatEveryDays: Int = 0
)

data class CultureProtocol(
    val id: String,
    val name: String,
    val organism: String,
    val description: String,
    val durationDays: Int,
    val steps: List<ProtocolStep>
)

data class CultureTimelineTask(
    val id: String,
    val batchId: String,
    val protocolId: String,
    val title: String,
    val description: String,
    val scheduledAtMillis: Long,
    val advanceReminderDays: Int = 1,
    val completedAtMillis: Long? = null,
    val originalScheduledAtMillis: Long = scheduledAtMillis,
    val status: CultureTaskStatus = CultureTaskStatus.UPCOMING,
    val completionNote: String = ""
)

data class CultureBatch(
    val id: String,
    val name: String,
    val protocolId: String,
    val startedAtMillis: Long,
    val expectedEndAtMillis: Long,
    val notes: String = "",
    val tasks: List<CultureTimelineTask> = emptyList()
)

data class CultureTaskHistory(
    val taskId: String,
    val action: String,
    val timestampMillis: Long,
    val note: String = ""
)

/*
 * ============================================================
 * SECTION 3: PROTOCOL LIBRARY AND TIMELINE GENERATION
 * ============================================================
 */

private fun createId(prefix: String): String =
    "$prefix-${java.util.UUID.randomUUID()}"

private val sampleProtocols = listOf(
    CultureProtocol(
        id = "general-observation",
        name = "General Culture Observation",
        organism = "User-defined culture",
        description = "An editable culture observation template.",
        durationDays = 30,
        steps = listOf(
            ProtocolStep(
                id = "initial",
                title = "Initial culture check",
                description = "Record starting conditions and observations.",
                dayOffset = 0
            ),
            ProtocolStep(
                id = "early",
                title = "Routine observation",
                description = "Record appearance and notable changes.",
                dayOffset = 1,
                repeatEveryDays = 2
            ),
            ProtocolStep(
                id = "weekly",
                title = "Weekly review",
                description = "Review observations and batch records.",
                dayOffset = 7,
                repeatEveryDays = 7
            ),
            ProtocolStep(
                id = "checkpoint",
                title = "Protocol review checkpoint",
                description = "Check the approved protocol for scheduled actions.",
                dayOffset = 28
            ),
            ProtocolStep(
                id = "final",
                title = "Final scheduled review",
                description = "Summarize observations and outstanding tasks.",
                dayOffset = 30
            )
        )
    ),
    CultureProtocol(
        id = "custom-14-day",
        name = "Custom 14-Day Protocol",
        organism = "User-defined culture",
        description = "A short, editable timeline template.",
        durationDays = 14,
        steps = listOf(
            ProtocolStep(
                id = "start",
                title = "Starting conditions",
                description = "Record batch details and the approved procedure.",
                dayOffset = 0
            ),
            ProtocolStep(
                id = "observation",
                title = "Routine observation",
                description = "Record observations according to the protocol.",
                dayOffset = 1,
                repeatEveryDays = 2
            ),
            ProtocolStep(
                id = "midpoint",
                title = "Midpoint review",
                description = "Review the records and upcoming tasks.",
                dayOffset = 7
            ),
            ProtocolStep(
                id = "finish",
                title = "End-of-template review",
                description = "Document results and follow-up actions.",
                dayOffset = 14
            )
        )
    )
)

/**
 * Provides the bundled sample protocol templates.
 */
fun getCultureProtocols(): List<CultureProtocol> =
    sampleProtocols

/**
 * Generates timeline tasks using calendar dates in the
 * device's local time zone.
 *
 * These templates are examples, not validated laboratory
 * procedures. Confirm actual timings against the relevant
 * approved protocol.
 */
fun generateCultureTimeline(
    batchId: String,
    protocol: CultureProtocol,
    startAtMillis: Long,
    advanceReminderDays: Int = 1
): List<CultureTimelineTask> {

    val zone = java.time.ZoneId.systemDefault()

    val startDate = java.time.Instant
        .ofEpochMilli(startAtMillis)
        .atZone(zone)
        .toLocalDate()

    val tasks = mutableListOf<CultureTimelineTask>()

    for (step in protocol.steps) {
        if (step.dayOffset < 0) continue
        if (step.dayOffset > protocol.durationDays) continue

        val interval = step.repeatEveryDays.coerceAtLeast(0)

        val offsets = if (interval == 0) {
            listOf(step.dayOffset)
        } else {
            generateSequence(step.dayOffset) { previous ->
                previous + interval
            }.takeWhile { it <= protocol.durationDays }
                .toList()
        }

        for (offset in offsets) {
            val dueDate = startDate.plusDays(offset.toLong())

            val dueAtMillis = dueDate
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()

            val taskTitle = if (interval > 0) {
                "${step.title} — Day $offset"
            } else {
                step.title
            }

            tasks.add(
                CultureTimelineTask(
                    id = createId("task"),
                    batchId = batchId,
                    protocolId = protocol.id,
                    title = taskTitle,
                    description = step.description,
                    scheduledAtMillis = dueAtMillis,
                    advanceReminderDays =
                        advanceReminderDays.coerceAtLeast(0)
                )
            )
        }
    }

    return tasks.sortedWith(
        compareBy<CultureTimelineTask> {
            it.scheduledAtMillis
        }.thenBy {
            it.title
        }
    )
}

/**
 * Creates a batch and generates its initial timeline.
 * Persistent storage will be implemented in Section 4.
 */
fun createCultureBatch(
    name: String,
    protocol: CultureProtocol,
    startAtMillis: Long = System.currentTimeMillis(),
    notes: String = ""
): CultureBatch {

    val batchId = createId("batch")

    val tasks = generateCultureTimeline(
        batchId = batchId,
        protocol = protocol,
        startAtMillis = startAtMillis
    )

    val zone = java.time.ZoneId.systemDefault()

    val startDate = java.time.Instant
        .ofEpochMilli(startAtMillis)
        .atZone(zone)
        .toLocalDate()

    val endDate = startDate.plusDays(
        protocol.durationDays.toLong()
    )

    val endAtMillis = endDate
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    return CultureBatch(
        id = batchId,
        name = name.trim().ifBlank { "Unnamed batch" },
        protocolId = protocol.id,
        startedAtMillis = startAtMillis,
        expectedEndAtMillis = endAtMillis,
        notes = notes.trim(),
        tasks = tasks
    )
}

/*
 * ============================================================
 * SECTION 4: LOCAL PERSISTENCE AND TASK HISTORY
 * ============================================================
 */

class CultureLabStore(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            "tc_lab_local_data",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_BATCHES = "culture_batches"
        private const val KEY_HISTORY = "culture_task_history"
    }

    // --------------------------------------------------------
    // BATCH STORAGE
    // --------------------------------------------------------

    fun saveBatches(batches: List<CultureBatch>) {
        val array = JSONArray()

        batches.forEach { batch ->
            array.put(batchToJson(batch))
        }

        preferences.edit()
            .putString(KEY_BATCHES, array.toString())
            .apply()
    }

    fun loadBatches(): List<CultureBatch> {
        val raw = preferences.getString(KEY_BATCHES, null)
            ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<CultureBatch>()

            for (index in 0 until array.length()) {
                result.add(jsonToBatch(array.getJSONObject(index)))
            }

            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveBatch(batch: CultureBatch) {
        val batches = loadBatches().toMutableList()
        val index = batches.indexOfFirst { it.id == batch.id }

        if (index >= 0) {
            batches[index] = batch
        } else {
            batches.add(batch)
        }

        saveBatches(batches)
    }

    fun deleteBatch(batchId: String) {
        saveBatches(
            loadBatches().filterNot { it.id == batchId }
        )
    }

    // --------------------------------------------------------
    // TASK HISTORY STORAGE
    // --------------------------------------------------------

    fun loadHistory(): List<CultureTaskHistory> {
        val raw = preferences.getString(KEY_HISTORY, null)
            ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<CultureTaskHistory>()

            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                result.add(
                    CultureTaskHistory(
                        taskId = item.optString("taskId"),
                        action = item.optString("action"),
                        timestampMillis =
                            item.optLong("timestampMillis"),
                        note = item.optString("note")
                    )
                )
            }

            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun recordHistory(
        taskId: String,
        action: String,
        note: String = ""
    ) {
        val history = loadHistory().toMutableList()

        history.add(
            CultureTaskHistory(
                taskId = taskId,
                action = action,
                timestampMillis = System.currentTimeMillis(),
                note = note
            )
        )

        val array = JSONArray()

        history.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("taskId", entry.taskId)
                    put("action", entry.action)
                    put("timestampMillis", entry.timestampMillis)
                    put("note", entry.note)
                }
            )
        }

        preferences.edit()
            .putString(KEY_HISTORY, array.toString())
            .apply()
    }

    // --------------------------------------------------------
    // JSON SERIALIZATION: BATCHES AND TASKS
    // --------------------------------------------------------

    private fun batchToJson(
        batch: CultureBatch
    ): JSONObject {
        return JSONObject().apply {
            put("id", batch.id)
            put("name", batch.name)
            put("protocolId", batch.protocolId)
            put("startedAtMillis", batch.startedAtMillis)
            put("expectedEndAtMillis", batch.expectedEndAtMillis)
            put("notes", batch.notes)

            val taskArray = JSONArray()

            batch.tasks.forEach { task ->
                taskArray.put(taskToJson(task))
            }

            put("tasks", taskArray)
        }
    }

    private fun jsonToBatch(
        json: JSONObject
    ): CultureBatch {
        val tasksArray = json.optJSONArray("tasks")
            ?: JSONArray()

        val tasks = mutableListOf<CultureTimelineTask>()

        for (index in 0 until tasksArray.length()) {
            tasks.add(
                jsonToTask(tasksArray.getJSONObject(index))
            )
        }

        return CultureBatch(
            id = json.optString("id"),
            name = json.optString("name"),
            protocolId = json.optString("protocolId"),
            startedAtMillis = json.optLong("startedAtMillis"),
            expectedEndAtMillis =
                json.optLong("expectedEndAtMillis"),
            notes = json.optString("notes"),
            tasks = tasks
        )
    }

    private fun taskToJson(
        task: CultureTimelineTask
    ): JSONObject {
        return JSONObject().apply {
            put("id", task.id)
            put("batchId", task.batchId)
            put("protocolId", task.protocolId)
            put("title", task.title)
            put("description", task.description)
            put("scheduledAtMillis", task.scheduledAtMillis)
            put("advanceReminderDays", task.advanceReminderDays)

            put(
                "completedAtMillis",
                task.completedAtMillis ?: JSONObject.NULL
            )

            put(
                "originalScheduledAtMillis",
                task.originalScheduledAtMillis
            )

            put("status", task.status.name)
            put("completionNote", task.completionNote)
        }
    }

    private fun jsonToTask(
        json: JSONObject
    ): CultureTimelineTask {
        val completedValue = json.opt("completedAtMillis")

        val completedAt = if (
            completedValue == null ||
            completedValue == JSONObject.NULL
        ) {
            null
        } else {
            (completedValue as? Number)?.toLong()
        }

        val statusName = json.optString(
            "status",
            CultureTaskStatus.UPCOMING.name
        )

        val taskStatus = try {
            CultureTaskStatus.valueOf(statusName)
        } catch (_: IllegalArgumentException) {
            CultureTaskStatus.UPCOMING
        }

        val scheduledAt = json.optLong("scheduledAtMillis")

        return CultureTimelineTask(
            id = json.optString("id"),
            batchId = json.optString("batchId"),
            protocolId = json.optString("protocolId"),
            title = json.optString("title"),
            description = json.optString("description"),
            scheduledAtMillis = scheduledAt,
            advanceReminderDays =
                json.optInt("advanceReminderDays", 1),
            completedAtMillis = completedAt,
            originalScheduledAtMillis = json.optLong(
                "originalScheduledAtMillis",
                scheduledAt
            ),
            status = taskStatus,
            completionNote = json.optString("completionNote")
        )
    }
}

/*
 * ============================================================
 * SECTION 5: SCHEDULER AND TASK MANAGEMENT
 * ============================================================
 */

object CultureScheduler {

    /**
     * Calculates the current status of a task.
     * Completed tasks retain their completed status.
     */
    fun calculateStatus(
        task: CultureTimelineTask,
        nowMillis: Long = System.currentTimeMillis()
    ): CultureTaskStatus {

        if (
            task.status == CultureTaskStatus.COMPLETED ||
            task.completedAtMillis != null
        ) {
            return CultureTaskStatus.COMPLETED
        }

        return when {
            nowMillis > task.scheduledAtMillis ->
                CultureTaskStatus.OVERDUE

            nowMillis >= task.scheduledAtMillis ->
                CultureTaskStatus.DUE

            else -> CultureTaskStatus.UPCOMING
        }
    }

    /**
     * Updates task statuses using the current device time.
     */
    fun refreshTasks(
        tasks: List<CultureTimelineTask>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<CultureTimelineTask> {

        return tasks.map { task ->
            task.copy(
                status = calculateStatus(task, nowMillis)
            )
        }
    }

    /**
     * Returns tasks in chronological order.
     */
    fun sortTasks(
        tasks: List<CultureTimelineTask>
    ): List<CultureTimelineTask> {

        return tasks.sortedWith(
            compareBy<CultureTimelineTask> {
                it.scheduledAtMillis
            }.thenBy {
                it.title
            }
        )
    }

    /**
     * Finds tasks requiring attention.
     */
    fun dueTasks(
        tasks: List<CultureTimelineTask>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<CultureTimelineTask> {

        return refreshTasks(tasks, nowMillis).filter {
            it.status == CultureTaskStatus.DUE ||
                it.status == CultureTaskStatus.OVERDUE
        }
    }

    /**
     * Completes a task and records the action.
     */
    fun completeTask(
        batch: CultureBatch,
        taskId: String,
        store: CultureLabStore,
        note: String = "",
        completedAtMillis: Long = System.currentTimeMillis()
    ): CultureBatch? {

        val task = batch.tasks.find { it.id == taskId }
            ?: return null

        if (task.status == CultureTaskStatus.COMPLETED) {
            return batch
        }

        val updatedTask = task.copy(
            status = CultureTaskStatus.COMPLETED,
            completedAtMillis = completedAtMillis,
            completionNote = note
        )

        val updatedBatch = batch.copy(
            tasks = batch.tasks.map {
                if (it.id == taskId) updatedTask else it
            }
        )

        store.saveBatch(updatedBatch)

        store.recordHistory(
            taskId = taskId,
            action = "COMPLETED",
            note = note
        )

        return updatedBatch
    }

    /**
     * Reschedules a task without losing its original date.
     */
    fun rescheduleTask(
        batch: CultureBatch,
        taskId: String,
        newScheduledAtMillis: Long,
        store: CultureLabStore,
        note: String = ""
    ): CultureBatch? {

        val task = batch.tasks.find { it.id == taskId }
            ?: return null

        if (task.completedAtMillis != null) {
            return null
        }

        val updatedTask = task.copy(
            scheduledAtMillis = newScheduledAtMillis,
            originalScheduledAtMillis =
                task.originalScheduledAtMillis,
            status = CultureTaskStatus.RESCHEDULED
        )

        val updatedBatch = batch.copy(
            tasks = batch.tasks.map {
                if (it.id == taskId) updatedTask else it
            }
        )

        store.saveBatch(updatedBatch)

        store.recordHistory(
            taskId = taskId,
            action = "RESCHEDULED",
            note = buildString {
                append("New scheduled time: ")
                append(newScheduledAtMillis)
                if (note.isNotBlank()) {
                    append("; ")
                    append(note)
                }
            }
        )

        return updatedBatch
    }

    /**
     * Adds a user-defined task to an existing batch.
     */
    fun addTask(
        batch: CultureBatch,
        title: String,
        description: String,
        scheduledAtMillis: Long,
        store: CultureLabStore,
        advanceReminderDays: Int = 1
    ): CultureBatch {

        val task = CultureTimelineTask(
            id = "task-${java.util.UUID.randomUUID()}",
            batchId = batch.id,
            protocolId = batch.protocolId,
            title = title.trim().ifBlank { "Custom task" },
            description = description.trim(),
            scheduledAtMillis = scheduledAtMillis,
            advanceReminderDays =
                advanceReminderDays.coerceAtLeast(0)
        )

        val updatedBatch = batch.copy(
            tasks = sortTasks(batch.tasks + task)
        )

        store.saveBatch(updatedBatch)

        store.recordHistory(
            taskId = task.id,
            action = "CREATED",
            note = "Custom timeline task added"
        )

        return updatedBatch
    }

    /**
     * Dashboard summary for a batch.
     */
    fun summarize(
        batch: CultureBatch,
        nowMillis: Long = System.currentTimeMillis()
    ): CultureBatchSummary {

        val tasks = refreshTasks(batch.tasks, nowMillis)

        return CultureBatchSummary(
            totalTasks = tasks.size,
            completedTasks = tasks.count {
                it.status == CultureTaskStatus.COMPLETED
            },
            upcomingTasks = tasks.count {
                it.status == CultureTaskStatus.UPCOMING ||
                    it.status == CultureTaskStatus.RESCHEDULED
            },
            dueTasks = tasks.count {
                it.status == CultureTaskStatus.DUE
            },
            overdueTasks = tasks.count {
                it.status == CultureTaskStatus.OVERDUE
            }
        )
    }
}

data class CultureBatchSummary(
    val totalTasks: Int,
    val completedTasks: Int,
    val upcomingTasks: Int,
    val dueTasks: Int,
    val overdueTasks: Int
)

/*
 * ============================================================
 * SECTION 6: TC LAB HOME DASHBOARD
 * ============================================================
 */

@Composable
private fun HomeScreen(
    batches: List<CultureBatch> = emptyList(),
    onNavigate: (String) -> Unit
) {
    val now = System.currentTimeMillis()

    val allTasks = batches.flatMap { it.tasks }
    val refreshedTasks = CultureScheduler.refreshTasks(
        allTasks,
        now
    )

    val completedCount = refreshedTasks.count {
        it.status == CultureTaskStatus.COMPLETED
    }

    val dueCount = refreshedTasks.count {
        it.status == CultureTaskStatus.DUE
    }

    val overdueCount = refreshedTasks.count {
        it.status == CultureTaskStatus.OVERDUE
    }

    val upcomingTasks = refreshedTasks
        .filter {
            it.status == CultureTaskStatus.UPCOMING ||
                it.status == CultureTaskStatus.RESCHEDULED
        }
        .sortedBy { it.scheduledAtMillis }
        .take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TCLabBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TC LAB",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TCLabPrimary
                )

                Text(
                    text = "Tissue Culture Laboratory",
                    fontSize = 15.sp,
                    color = TCLabMuted
                )

                Text(
                    text = java.time.LocalDate.now()
                        .format(
                            java.time.format.DateTimeFormatter
                                .ofPattern("EEEE, dd MMMM yyyy")
                        ),
                    fontSize = 13.sp,
                    color = TCLabSecondary
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TCLabSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Laboratory overview",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Monitor your batches, protocols and " +
                            "scheduled culture observations.",
                        color = TCLabMuted,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = {
                            onNavigate("Protocols")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Explore protocols")
                    }
                }
            }
        }

        item {
            Text(
                text = "Task overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetric(
                        title = "Batches",
                        value = batches.size.toString(),
                        accent = TCLabSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetric(
                        title = "Completed",
                        value = completedCount.toString(),
                        accent = TCLabPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetric(
                        title = "Due",
                        value = dueCount.toString(),
                        accent = TCLabWarning,
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetric(
                        title = "Overdue",
                        value = overdueCount.toString(),
                        accent = TCLabDanger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Culture batches",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        onNavigate("Batches")
                    }
                ) {
                    Text("View all")
                }
            }
        }

        if (batches.isEmpty()) {
            item {
                EmptyDashboardCard(
                    title = "No batches yet",
                    message = "Create a batch from a protocol to " +
                        "start building your culture timeline."
                )
            }
        } else {
            items(
                items = batches,
                key = { it.id }
            ) { batch ->
                BatchDashboardCard(
                    batch = batch,
                    onClick = {
                        onNavigate("Timeline")
                    }
                )
            }
        }

        item {
            Text(
                text = "Upcoming tasks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (upcomingTasks.isEmpty()) {
            item {
                EmptyDashboardCard(
                    title = "Nothing upcoming",
                    message = if (allTasks.isEmpty()) {
                        "Your scheduled tasks will appear here."
                    } else {
                        "There are no pending future tasks to display."
                    }
                )
            }
        } else {
            items(
                items = upcomingTasks,
                key = { it.id }
            ) { task ->
                UpcomingTaskCard(task = task)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TC LAB • Local laboratory workspace",
                modifier = Modifier.fillMaxWidth(),
                color = TCLabMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DashboardMetric(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TCLabSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = TCLabMuted
            )

            Text(
                text = value,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun BatchDashboardCard(
    batch: CultureBatch,
    onClick: () -> Unit
) {
    val summary = CultureScheduler.summarize(batch)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TCLabSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = batch.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Protocol: ${batch.protocolId}",
                fontSize = 13.sp,
                color = TCLabMuted
            )

            Text(
                text = "${summary.completedTasks} of " +
                    "${summary.totalTasks} tasks completed",
                fontSize = 13.sp,
                color = TCLabPrimary
            )

            val progress = if (summary.totalTasks == 0) {
                0f
            } else {
                summary.completedTasks.toFloat() /
                    summary.totalTasks.toFloat()
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = TCLabPrimary,
                trackColor = TCLabBackground
            )

            if (summary.overdueTasks > 0) {
                Text(
                    text = "${summary.overdueTasks} overdue task(s)",
                    color = TCLabDanger,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun UpcomingTaskCard(
    task: CultureTimelineTask
) {
    val zone = java.time.ZoneId.systemDefault()

    val formattedDate = java.time.Instant
        .ofEpochMilli(task.scheduledAtMillis)
        .atZone(zone)
        .format(
            java.time.format.DateTimeFormatter
                .ofPattern("dd MMM yyyy")
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TCLabSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = task.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Text(
                text = formattedDate,
                color = TCLabSecondary,
                fontSize = 13.sp
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    color = TCLabMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboardCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TCLabSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TCLabSecondary
            )

            Text(
                text = message,
                color = TCLabMuted,
                fontSize = 14.sp
            )
        }
    }
}

/*
 * ============================================================
 * SECTION 7: PROTOCOL LIBRARY AND BATCH CREATION
 * ============================================================
 */

@Composable
private fun ProtocolLibraryScreen(
    onBatchCreated: (CultureBatch) -> Unit,
    onBack: () -> Unit
) {
    val protocols = remember {
        getCultureProtocols()
    }

    var selectedProtocolId by remember {
        mutableStateOf(protocols.firstOrNull()?.id.orEmpty())
    }

    var batchName by remember {
        mutableStateOf("")
    }

    var batchNotes by remember {
        mutableStateOf("")
    }

    var showCreateForm by remember {
        mutableStateOf(false)
    }

    val selectedProtocol = protocols.find {
        it.id == selectedProtocolId
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TCLabBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Protocol Library",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = TCLabPrimary
                )
            }

            Text(
                text = "Choose a template to generate a batch timeline.",
                color = TCLabMuted,
                fontSize = 14.sp
            )
        }

        item {
            Text(
                text = "Available protocols",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(
            items = protocols,
            key = { it.id }
        ) { protocol ->
            ProtocolLibraryCard(
                protocol = protocol,
                selected = protocol.id == selectedProtocolId,
                onSelect = {
                    selectedProtocolId = protocol.id
                }
            )
        }

        item {
            Button(
                onClick = {
                    showCreateForm = !showCreateForm
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedProtocol != null
            ) {
                Text(
                    if (showCreateForm) {
                        "Hide batch form"
                    } else {
                        "Create batch from selected protocol"
                    }
                )
            }
        }

        if (showCreateForm && selectedProtocol != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TCLabSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "New culture batch",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Selected: ${selectedProtocol.name}",
                            color = TCLabSecondary
                        )

                        OutlinedTextField(
                            value = batchName,
                            onValueChange = {
                                batchName = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Batch name")
                            },
                            placeholder = {
                                Text("e.g. Culture Batch 001")
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = batchNotes,
                            onValueChange = {
                                batchNotes = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Notes (optional)")
                            },
                            placeholder = {
                                Text("Record batch details")
                            },
                            minLines = 2,
                            maxLines = 4
                        )

                        Text(
                            text = "Start date: today, using the " +
                                "device's local date.",
                            color = TCLabMuted,
                            fontSize = 13.sp
                        )

                        Button(
                            onClick = {
                                val newBatch = createCultureBatch(
                                    name = batchName,
                                    protocol = selectedProtocol,
                                    startAtMillis =
                                        System.currentTimeMillis(),
                                    notes = batchNotes
                                )

                                onBatchCreated(newBatch)

                                batchName = ""
                                batchNotes = ""
                                showCreateForm = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = batchName.isNotBlank()
                        ) {
                            Text("Start batch and generate timeline")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Template timings are examples. Confirm " +
                    "each schedule against your approved laboratory " +
                    "procedure before using it.",
                color = TCLabWarning,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ProtocolLibraryCard(
    protocol: CultureProtocol,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (selected) {
        TCLabPrimary
    } else {
        TCLabSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = TCLabSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = protocol.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = protocol.organism,
                        color = TCLabSecondary,
                        fontSize = 13.sp
                    )
                }

                if (selected) {
                    Text(
                        text = "SELECTED",
                        color = TCLabPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = protocol.description,
                color = TCLabMuted,
                fontSize = 14.sp
            )

            Text(
                text = "Duration: ${protocol.durationDays} days",
                color = TCLabPrimary,
                fontSize = 13.sp
            )

            Text(
                text = "${protocol.steps.size} protocol steps defined",
                color = TCLabMuted,
                fontSize = 12.sp
            )

            TextButton(onClick = onSelect) {
                Text(
                    if (selected) {
                        "Protocol selected"
                    } else {
                        "Select this protocol"
                    }
                )
            }
        }
    }
}

@Composable
fun CultureTimelineScreen(
    batches: List<CultureBatch>,
    onCompleteTask: (String, String, String) -> Unit,
    onRescheduleTask: (String, String, Long, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedBatchId by remember {
        mutableStateOf(batches.firstOrNull()?.id ?: "")
    }

    var reschedulingTaskId by remember {
        mutableStateOf<String?>(null)
    }

    var rescheduleDays by remember {
        mutableStateOf("1")
    }

    var rescheduleNote by remember {
        mutableStateOf("")
    }

    val selectedBatch = batches.firstOrNull {
        it.id == selectedBatchId
    }

    val tasks = selectedBatch?.tasks
        ?.sortedBy { it.scheduledAtMillis }
        ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Culture Timeline",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (batches.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No culture batches yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Create a batch from the Protocol Library to generate its timeline."
                    )
                }
            }
        } else {
            Text(
                text = "Select batch",
                style = MaterialTheme.typography.titleMedium
            )

            batches.forEach { batch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedBatchId = batch.id
                            reschedulingTaskId = null
                        },
                    border = if (batch.id == selectedBatchId) {
                        BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    } else {
                        null
                    },
                    colors = CardDefaults.cardColors(
                        containerColor =
                            if (batch.id == selectedBatchId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = batch.name,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Protocol: ${batch.protocolId}"
                        )
                        Text(
                            text = "Tasks: ${batch.tasks.size}"
                        )
                    }
                }
            }

            if (selectedBatch != null) {
                Divider()

                Text(
                    text = selectedBatch.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Started: ${
                        java.text.SimpleDateFormat(
                            "dd MMM yyyy",
                            java.util.Locale.getDefault()
                        ).format(
                            java.util.Date(
                                selectedBatch.startedAtMillis
                            )
                        )
                    }"
                )

                Text(
                    text = "Expected end: ${
                        java.text.SimpleDateFormat(
                            "dd MMM yyyy",
                            java.util.Locale.getDefault()
                        ).format(
                            java.util.Date(
                                selectedBatch.expectedEndAtMillis
                            )
                        )
                    }"
                )

                if (tasks.isEmpty()) {
                    Text("This batch has no scheduled tasks.")
                }

                tasks.forEach { task ->
                    val currentStatus =
                        CultureScheduler.calculateStatus(task)

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(task.description)

                            Text(
                                text = "Scheduled: ${
                                    java.text.SimpleDateFormat(
                                        "dd MMM yyyy, HH:mm",
                                        java.util.Locale.getDefault()
                                    ).format(
                                        java.util.Date(
                                            task.scheduledAtMillis
                                        )
                                    )
                                }"
                            )

                            Text(
                                text = "Status: ${
                                    currentStatus.name.replace("_", " ")
                                }",
                                color = when (currentStatus) {
                                    CultureTaskStatus.COMPLETED ->
                                        MaterialTheme.colorScheme.tertiary

                                    CultureTaskStatus.OVERDUE ->
                                        MaterialTheme.colorScheme.error

                                    CultureTaskStatus.DUE ->
                                        MaterialTheme.colorScheme.primary

                                    else ->
                                        MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = FontWeight.Bold
                            )

                            if (task.completedAtMillis != null) {
                                Text(
                                    text = "Completed: ${
                                        java.text.SimpleDateFormat(
                                            "dd MMM yyyy, HH:mm",
                                            java.util.Locale.getDefault()
                                        ).format(
                                            java.util.Date(
                                                task.completedAtMillis
                                            )
                                        )
                                    }"
                                )
                            }

                            if (task.completionNote.isNotBlank()) {
                                Text(
                                    text = "Note: ${task.completionNote}"
                                )
                            }

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    enabled =
                                        currentStatus !=
                                            CultureTaskStatus.COMPLETED,
                                    onClick = {
                                        onCompleteTask(
                                            selectedBatch.id,
                                            task.id,
                                            ""
                                        )
                                    }
                                ) {
                                    Text("Complete")
                                }

                                OutlinedButton(
                                    enabled =
                                        currentStatus !=
                                            CultureTaskStatus.COMPLETED,
                                    onClick = {
                                        reschedulingTaskId =
                                            if (reschedulingTaskId == task.id) {
                                                null
                                            } else {
                                                task.id
                                            }

                                        rescheduleDays = "1"
                                        rescheduleNote = ""
                                    }
                                ) {
                                    Text("Reschedule")
                                }
                            }

                            if (reschedulingTaskId == task.id) {
                                OutlinedTextField(
                                    value = rescheduleDays,
                                    onValueChange = {
                                        rescheduleDays = it.filter { char ->
                                            char.isDigit()
                                        }
                                    },
                                    label = {
                                        Text("Move forward by days")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = rescheduleNote,
                                    onValueChange = {
                                        rescheduleNote = it
                                    },
                                    label = {
                                        Text("Reason or note")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val days =
                                            rescheduleDays.toLongOrNull()

                                        if (days != null && days >= 0L) {
                                            val newTime =
                                                task.scheduledAtMillis +
                                                    days * 24L * 60L *
                                                    60L * 1000L

                                            onRescheduleTask(
                                                selectedBatch.id,
                                                task.id,
                                                newTime,
                                                rescheduleNote
                                            )

                                            reschedulingTaskId = null
                                        }
                                    }
                                ) {
                                    Text("Save new date")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


