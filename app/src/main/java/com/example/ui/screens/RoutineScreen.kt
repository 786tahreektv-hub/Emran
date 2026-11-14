package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.data.local.RoutineTaskEntity
import com.example.ui.components.AnimatedEmeraldProgress
import com.example.ui.components.DateNavigatorStrip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionCard
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ResolvedTaskItem

@Composable
fun RoutineScreen(
    state: MainUiState,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToday: () -> Unit,
    onToggleTask: (ResolvedTaskItem) -> Unit,
    onCarryForwardTask: (RoutineTaskEntity) -> Unit,
    onCarryAllUnfinished: () -> Unit,
    onSkipTaskToday: (String) -> Unit,
    onRestoreSkippedTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onSaveTask: (
        id: String?, title: String, description: String, startTime: String, endTime: String,
        category: String, subTag: String, priority: Int, recurrence: String,
        startDate: String, reminderEnabled: Boolean, estimatedMinutes: Int, note: String
    ) -> Unit,
    onSaveSpecialDay: (date: String, dayType: String, title: String, note: String) -> Unit
) {
    val lang = state.prefs.language
    var editingTask by remember { mutableStateOf<RoutineTaskEntity?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showSpecialDayDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val allTasks = state.resolvedTasksForSelectedDate
    val filteredTasks = if (selectedCategoryFilter == "ALL") allTasks
    else allTasks.filter { it.task.category == selectedCategoryFilter }

    val groupedByHour = remember(filteredTasks) {
        val map = linkedMapOf<Int, MutableList<ResolvedTaskItem>>()
        for (h in 0..23) map[h] = mutableListOf()
        val unscheduled = mutableListOf<ResolvedTaskItem>()
        filteredTasks.forEach { item ->
            val hr = item.task.startTime.split(":").firstOrNull()?.toIntOrNull()
            if (hr != null && hr in 0..23) {
                map[hr]?.add(item)
            } else {
                unscheduled.add(item)
            }
        }
        map to unscheduled
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("routine_screen_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DateNavigatorStrip(
                    tripleDate = state.selectedDateTriple,
                    isToday = state.selectedDate == state.todayIso,
                    lang = lang,
                    onPrevDay = onPrevDay,
                    onNextDay = onNextDay,
                    onGoToday = onGoToday
                )
            }

            // Progress + Holiday/Special Day + Carry Forward Actions
            item {
                val summary = state.selectedSummary
                SectionCard(
                    title = AppStrings.t(lang, "২৪ ঘণ্টার পরিকল্পনা", "24-Hour Routine Planner"),
                    icon = Icons.Default.Schedule,
                    trailing = {
                        TextButton(onClick = { showSpecialDayDialog = true }) {
                            Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = state.specialDayForSelectedDate?.let {
                                    it.title.ifBlank { AppStrings.t(lang, "বিশেষ দিন", "Special Day") }
                                } ?: AppStrings.t(lang, "ছুটি/বিশেষ দিন", "Holiday/Special")
                            )
                        }
                    }
                ) {
                    if (state.specialDayForSelectedDate != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = AppStrings.t(
                                    lang,
                                    "বিশেষ দিন সক্রিয়: ${state.specialDayForSelectedDate.title.ifBlank { state.specialDayForSelectedDate.dayType }} — অপ্রয়োজনীয় কাজ আজকের জন্য বাদ দিতে পারেন।",
                                    "Special day active: ${state.specialDayForSelectedDate.title}"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.t(
                                lang,
                                "সম্পন্ন: ${AppStrings.digits(summary.tasksCompleted, lang)}/${AppStrings.digits(summary.tasksTotal, lang)} কাজ (${AppStrings.digits(summary.overallPercent, lang)}% সামগ্রিক)",
                                "Done: ${summary.tasksCompleted}/${summary.tasksTotal} tasks (${summary.overallPercent}% overall)"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    AnimatedEmeraldProgress(progressFraction = summary.overallPercent / 100f)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                editingTask = null
                                showTaskDialog = true
                            },
                            modifier = Modifier.testTag("add_task_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(AppStrings.t(lang, "নতুন কাজ", "New Task"))
                        }
                        OutlinedButton(
                            onClick = onCarryAllUnfinished,
                            modifier = Modifier.testTag("carry_all_button")
                        ) {
                            Icon(Icons.Default.NextPlan, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(AppStrings.t(lang, "অসমাপ্ত আগামীকাল নিন", "Carry Unfinished"))
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                val cats = listOf("ALL", "IBADAT", "PERSONAL", "FAMILY", "WORK_MADRASA", "EDUCATION", "HEALTH", "FINANCE")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cats.forEach { c ->
                        FilterChip(
                            selected = selectedCategoryFilter == c,
                            onClick = { selectedCategoryFilter = c },
                            label = {
                                Text(
                                    if (c == "ALL") AppStrings.t(lang, "সব", "All")
                                    else AppStrings.categoryLabel(c, lang)
                                )
                            }
                        )
                    }
                }
            }

            // 24-Hour Timeline (00:00 -> 23:59)
            if (filteredTasks.isEmpty() && state.skippedTasksForSelectedDate.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Schedule,
                        message = AppStrings.t(
                            lang,
                            "এই দিনে কোনো কাজ নির্ধারিত নেই। '+ নতুন কাজ' চেপে রাত ১২টা থেকে রাত ১২টার মধ্যে আপনার রুটিন তৈরি করুন।",
                            "No tasks for this day. Tap 'New Task' to plan your 24-hour day."
                        ),
                        actionLabel = AppStrings.t(lang, "+ নতুন কাজ যোগ করুন", "+ Add Task"),
                        onAction = {
                            editingTask = null
                            showTaskDialog = true
                        }
                    )
                }
            } else {
                val (hourMap, unscheduled) = groupedByHour
                for (hour in 0..23) {
                    val items = hourMap[hour].orEmpty()
                    if (items.isNotEmpty()) {
                        item(key = "hr_$hour") {
                            HourSectionHeader(hour = hour, lang = lang)
                        }
                        items.forEach { item ->
                            item(key = "task_${item.task.id}") {
                                RoutineTaskRowCard(
                                    item = item,
                                    lang = lang,
                                    onToggle = { onToggleTask(item) },
                                    onCarryTomorrow = { onCarryForwardTask(item.task) },
                                    onSkipToday = { onSkipTaskToday(item.task.id) },
                                    onEdit = {
                                        editingTask = item.task
                                        showTaskDialog = true
                                    },
                                    onDelete = { onDeleteTask(item.task.id) }
                                )
                            }
                        }
                    }
                }
                if (unscheduled.isNotEmpty()) {
                    item(key = "hr_unscheduled") {
                        Text(
                            text = AppStrings.t(lang, "নির্ধারিত সময়হীন কাজ", "Unscheduled Tasks"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    unscheduled.forEach { item ->
                        item(key = "utask_${item.task.id}") {
                            RoutineTaskRowCard(
                                item = item,
                                lang = lang,
                                onToggle = { onToggleTask(item) },
                                onCarryTomorrow = { onCarryForwardTask(item.task) },
                                onSkipToday = { onSkipTaskToday(item.task.id) },
                                onEdit = {
                                    editingTask = item.task
                                    showTaskDialog = true
                                },
                                onDelete = { onDeleteTask(item.task.id) }
                            )
                        }
                    }
                }
            }

            // Skipped tasks for holiday/special day override
            if (state.skippedTasksForSelectedDate.isNotEmpty()) {
                item {
                    SectionCard(
                        title = AppStrings.t(lang, "আজকের জন্য স্থগিত কাজ", "Skipped for Today"),
                        icon = Icons.Default.EventBusy
                    ) {
                        state.skippedTasksForSelectedDate.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(task.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                TextButton(onClick = { onRestoreSkippedTask(task.id) }) {
                                    Text(AppStrings.t(lang, "ফিরিয়ে আনুন", "Restore"))
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingTask = null
                showTaskDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("routine_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = AppStrings.t(lang, "নতুন কাজ যোগ", "Add Task"))
        }
    }

    if (showTaskDialog) {
        TaskEditorDialog(
            initial = editingTask,
            defaultDate = state.selectedDate,
            lang = lang,
            onDismiss = { showTaskDialog = false },
            onSave = { title, desc, sTime, eTime, cat, tag, prio, rec, sDate, rem, mins, note ->
                onSaveTask(editingTask?.id, title, desc, sTime, eTime, cat, tag, prio, rec, sDate, rem, mins, note)
                showTaskDialog = false
            }
        )
    }

    if (showSpecialDayDialog) {
        SpecialDayDialog(
            date = state.selectedDate,
            existingType = state.specialDayForSelectedDate?.dayType ?: "HOLIDAY",
            existingTitle = state.specialDayForSelectedDate?.title ?: "",
            existingNote = state.specialDayForSelectedDate?.note ?: "",
            lang = lang,
            onDismiss = { showSpecialDayDialog = false },
            onSave = { type, title, note ->
                onSaveSpecialDay(state.selectedDate, type, title, note)
                showSpecialDayDialog = false
            }
        )
    }
}

@Composable
private fun HourSectionHeader(hour: Int, lang: String) {
    val label = if (lang == "en") {
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        val suffix = if (hour < 12) "AM" else "PM"
        "$h12:00 $suffix"
    } else {
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        val period = when {
            hour < 4 -> "রাত"
            hour < 12 -> "সকাল"
            hour < 16 -> "দুপুর"
            hour < 18 -> "বিকেল"
            hour < 20 -> "সন্ধ্যা"
            else -> "রাত"
        }
        "$period ${AppStrings.digits(h12, lang)}:০০"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RoutineTaskRowCard(
    item: ResolvedTaskItem,
    lang: String,
    onToggle: () -> Unit,
    onCarryTomorrow: () -> Unit,
    onSkipToday: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val t = item.task
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (t.priority >= 3 && !item.isCompleted) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = t.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (t.priority >= 2) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = if (t.priority == 3) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = AppStrings.priorityLabel(t.priority, lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    val timeSpan = buildString {
                        if (t.startTime.isNotBlank()) {
                            append(AppStrings.digits(t.startTime, lang))
                            if (t.endTime.isNotBlank()) append("–${AppStrings.digits(t.endTime, lang)}")
                            append(" • ")
                        }
                        append(AppStrings.categoryLabel(t.category, lang))
                        append(" • ")
                        append(AppStrings.recurrenceLabel(t.recurrence, lang))
                        if (t.estimatedMinutes > 0) {
                            append(" • ${AppStrings.digits(t.estimatedMinutes, lang)}m")
                        }
                    }
                    Text(
                        text = timeSpan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (t.description.isNotBlank()) {
                        Text(
                            text = t.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (t.note.isNotBlank()) {
                        Text(
                            text = "${AppStrings.t(lang, "নোট", "Note")}: ${t.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!item.isCompleted) {
                    IconButton(onClick = onCarryTomorrow, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(
                            Icons.Default.NextPlan,
                            contentDescription = AppStrings.t(lang, "আগামীকাল নিন", "Carry forward"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onSkipToday, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = AppStrings.t(lang, "আজকের জন্য বাদ", "Skip today")
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = AppStrings.t(lang, "সম্পাদনা", "Edit")
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = AppStrings.t(lang, "মুছুন", "Delete"),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TaskEditorDialog(
    initial: RoutineTaskEntity?,
    defaultDate: String,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String, description: String, startTime: String, endTime: String,
        category: String, subTag: String, priority: Int, recurrence: String,
        startDate: String, reminderEnabled: Boolean, estimatedMinutes: Int, note: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var startTime by remember { mutableStateOf(initial?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(initial?.endTime ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "PERSONAL") }
    var priority by remember { mutableIntStateOf(initial?.priority ?: 1) }
    var recurrence by remember { mutableStateOf(initial?.recurrence ?: "DAILY") }
    var startDate by remember { mutableStateOf(initial?.startDate ?: defaultDate) }
    var reminder by remember { mutableStateOf(initial?.reminderEnabled ?: false) }
    var mins by remember { mutableStateOf(initial?.estimatedMinutes?.takeIf { it > 0 }?.toString() ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showError by remember { mutableStateOf(false) }

    val categories = listOf("IBADAT", "PERSONAL", "FAMILY", "WORK_MADRASA", "EDUCATION", "HEALTH", "FINANCE")
    val recurrences = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY", "ONCE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) AppStrings.t(lang, "নতুন রুটিন কাজ", "New Routine Task")
                else AppStrings.t(lang, "কাজ সম্পাদনা", "Edit Task")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; showError = false },
                            label = { Text(AppStrings.t(lang, "কাজের নাম *", "Task Title *")) },
                            isError = showError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text(AppStrings.t(lang, "বিস্তারিত বিবরণ", "Description")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text(AppStrings.t(lang, "শুরু (HH:mm)", "Start (HH:mm)")) },
                                placeholder = { Text("08:30") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text(AppStrings.t(lang, "শেষ (HH:mm)", "End (HH:mm)")) },
                                placeholder = { Text("09:30") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Text(AppStrings.t(lang, "বিভাগ", "Category"), style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { c ->
                                FilterChip(
                                    selected = category == c,
                                    onClick = { category = c },
                                    label = { Text(AppStrings.categoryLabel(c, lang)) }
                                )
                            }
                        }
                    }
                    item {
                        Text(AppStrings.t(lang, "অগ্রাধিকার", "Priority"), style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3).forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(AppStrings.priorityLabel(p, lang)) }
                                )
                            }
                        }
                    }
                    item {
                        Text(AppStrings.t(lang, "পুনরাবৃত্তি", "Recurrence"), style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            recurrences.forEach { r ->
                                FilterChip(
                                    selected = recurrence == r,
                                    onClick = { recurrence = r },
                                    label = { Text(AppStrings.recurrenceLabel(r, lang)) }
                                )
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text(AppStrings.t(lang, "তারিখ (yyyy-MM-dd)", "Date")) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mins,
                                onValueChange = { if (it.all(Char::isDigit)) mins = it },
                                label = { Text(AppStrings.t(lang, "সময় (মিনিট)", "Minutes")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text(AppStrings.t(lang, "কাজের নোট", "Task Note")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = reminder, onCheckedChange = { reminder = it })
                            Spacer(Modifier.width(8.dp))
                            Text(AppStrings.t(lang, "রিমাইন্ডার সক্রিয় রাখুন", "Enable Reminder"))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        showError = true
                    } else {
                        onSave(
                            title, desc, startTime.trim(), endTime.trim(),
                            category, "", priority, recurrence,
                            startDate.ifBlank { defaultDate }, reminder,
                            mins.toIntOrNull() ?: 0, note
                        )
                    }
                },
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text(AppStrings.t(lang, "সংরক্ষণ", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.t(lang, "বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
private fun SpecialDayDialog(
    date: String,
    existingType: String,
    existingTitle: String,
    existingNote: String,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (dayType: String, title: String, note: String) -> Unit
) {
    var dayType by remember { mutableStateOf(existingType) }
    var title by remember { mutableStateOf(existingTitle) }
    var note by remember { mutableStateOf(existingNote) }
    val types = listOf(
        "HOLIDAY" to AppStrings.t(lang, "ছুটির দিন", "Holiday"),
        "JUMUAH" to AppStrings.t(lang, "জুমার বিশেষ", "Jumu'ah"),
        "RAMADAN" to AppStrings.t(lang, "রমজান", "Ramadan"),
        "EID" to AppStrings.t(lang, "ঈদ / উৎসব", "Eid"),
        "NORMAL" to AppStrings.t(lang, "স্বাভাবিক দিন", "Normal Day")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.t(lang, "ছুটির দিন ও বিশেষ রুটিন ($date)", "Holiday / Special Day ($date)")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { (k, lbl) ->
                        FilterChip(selected = dayType == k, onClick = { dayType = k }, label = { Text(lbl) })
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.t(lang, "দিনের নাম (যেমন: সাপ্তাহিক ছুটি)", "Day Title")) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(AppStrings.t(lang, "বিশেষ নোট", "Special Note")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(dayType, title, note) }) {
                Text(AppStrings.t(lang, "সংরক্ষণ", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.t(lang, "বাতিল", "Cancel"))
            }
        }
    )
}
