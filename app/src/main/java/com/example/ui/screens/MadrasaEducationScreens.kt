package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.data.local.MadrasaProfessionalItemEntity
import com.example.ui.components.AnimatedEmeraldProgress
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionCard
import com.example.ui.viewmodel.MainUiState

@Composable
fun MadrasaWorkScreen(
    state: MainUiState,
    onSaveItem: (
        id: String?, section: String, title: String, subtitleOrPerson: String,
        scheduleDayOrTime: String, progress: Int, target: Int, notes: String, date: String, isCompleted: Boolean
    ) -> Unit,
    onToggleDone: (MadrasaProfessionalItemEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val lang = state.prefs.language
    val sections = listOf(
        "MADRASA_CLASS" to AppStrings.t(lang, "মাদরাসা ও ক্লাস", "Madrasa & Classes"),
        "STUDENT_MONITOR" to AppStrings.t(lang, "ছাত্র পর্যবেক্ষণ", "Student Monitoring"),
        "MEETING" to AppStrings.t(lang, "শিক্ষক বৈঠক ও মিটিং", "Meetings & Contact"),
        "OFFICE_DECISION" to AppStrings.t(lang, "অফিস, কাগজ ও সিদ্ধান্ত", "Office, Docs & Plans")
    )
    var activeSection by remember { mutableStateOf("MADRASA_CLASS") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = state.madrasaWorkItems.filter { it.section == activeSection }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("madrasa_work_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(
                title = AppStrings.t(lang, "পেশাগত ও মাদরাসা ব্যবস্থাপনা", "Madrasa & Professional Management"),
                icon = Icons.Default.School,
                trailing = {
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(AppStrings.t(lang, "যোগ করুন", "Add"))
                    }
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEach { (key, lbl) ->
                        FilterChip(
                            selected = activeSection == key,
                            onClick = { activeSection = key },
                            label = { Text(lbl) }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Assignment,
                    message = AppStrings.t(
                        lang,
                        "এই বিভাগে এখনো কোনো এন্ট্রি নেই। '+' বাটনে চেপে ক্লাস, ছাত্র পর্যবেক্ষণ, বৈঠক বা অফিসের কাজ যোগ করুন।",
                        "No entries in this section yet. Tap Add to record classes, student monitoring, meetings, or office decisions."
                    ),
                    actionLabel = AppStrings.t(lang, "+ নতুন যোগ করুন", "+ Add Entry"),
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                Card(onClick = { onToggleDone(item) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = item.isCompleted, onCheckedChange = { onToggleDone(item) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (item.subtitleOrPerson.isNotBlank()) {
                                Text(item.subtitleOrPerson, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            if (item.scheduleDayOrTime.isNotBlank()) {
                                Text(item.scheduleDayOrTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (item.notes.isNotBlank()) {
                                Text(item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onDelete(item.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MadrasaItemDialog(
            titleLabel = sections.first { it.first == activeSection }.second,
            lang = lang,
            isProgressMode = false,
            onDismiss = { showAddDialog = false },
            onSave = { title, sub, sched, target, notes ->
                onSaveItem(null, activeSection, title, sub, sched, 0, target, notes, state.selectedDate, false)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EducationSkillsScreen(
    state: MainUiState,
    onSaveItem: (
        id: String?, section: String, title: String, subtitleOrPerson: String,
        scheduleDayOrTime: String, progress: Int, target: Int, notes: String, date: String, isCompleted: Boolean
    ) -> Unit,
    onIncrementProgress: (MadrasaProfessionalItemEntity, Int) -> Unit,
    onDelete: (String) -> Unit
) {
    val lang = state.prefs.language
    val subjects = listOf(
        AppStrings.t(lang, "কুরআন", "Quran"),
        AppStrings.t(lang, "হাদিস", "Hadith"),
        AppStrings.t(lang, "ফিকহ", "Fiqh"),
        AppStrings.t(lang, "আরবি", "Arabic"),
        AppStrings.t(lang, "বাংলা", "Bangla"),
        AppStrings.t(lang, "সাধারণ জ্ঞান", "General Knowledge"),
        AppStrings.t(lang, "ইসলামী বই", "Islamic Books"),
        AppStrings.t(lang, "লেখালেখি ও সম্পাদনা", "Writing & Editing"),
        AppStrings.t(lang, "নতুন দক্ষতা", "New Skill")
    )
    var showAddDialog by remember { mutableStateOf(false) }
    var prefillSubject by remember { mutableStateOf("") }

    val studyItems = state.madrasaWorkItems.filter { it.section == "STUDY_SKILL" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("education_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(
                title = AppStrings.t(lang, "শিক্ষা ও ব্যক্তিগত উন্নয়ন", "Education & Personal Growth"),
                icon = Icons.Default.MenuBook,
                trailing = {
                    Button(onClick = { prefillSubject = ""; showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(AppStrings.t(lang, "নতুন বিষয়", "Add Track"))
                    }
                }
            ) {
                Text(
                    text = AppStrings.t(lang, "দ্রুত বিষয় নির্বাচন করুন:", "Quick select subject:"),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { subj ->
                        SuggestionChip(
                            onClick = {
                                prefillSubject = subj
                                showAddDialog = true
                            },
                            label = { Text(subj) }
                        )
                    }
                }
            }
        }

        if (studyItems.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.AutoStories,
                    message = AppStrings.t(
                        lang,
                        "কুরআন, হাদিস, ফিকহ, আরবি, ইসলামী বই, লেখালেখি বা নতুন দক্ষতা শেখার লক্ষ্য যোগ করুন।",
                        "Track Quran, Hadith, Fiqh, Arabic, Islamic books, writing, editing, and new skills."
                    ),
                    actionLabel = AppStrings.t(lang, "+ অধ্যয়ন যোগ করুন", "+ Add Study Track"),
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(studyItems, key = { it.id }) { item ->
                Card {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (item.subtitleOrPerson.isNotBlank()) {
                                    Text(item.subtitleOrPerson, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(
                                text = "${AppStrings.digits(item.statusOrProgress, lang)}/${AppStrings.digits(item.targetProgress, lang)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        AnimatedEmeraldProgress(
                            progressFraction = item.statusOrProgress.toFloat() / item.targetProgress.coerceAtLeast(1)
                        )
                        if (item.notes.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onIncrementProgress(item, -1) }) { Text("−১") }
                                Button(onClick = { onIncrementProgress(item, 1) }) { Text("+১") }
                                FilledTonalButton(onClick = { onIncrementProgress(item, 5) }) { Text("+৫") }
                            }
                            IconButton(onClick = { onDelete(item.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MadrasaItemDialog(
            titleLabel = AppStrings.t(lang, "শিক্ষা / বই / দক্ষতা ট্র্যাকার", "Study / Book / Skill Tracker"),
            initialSub = prefillSubject,
            lang = lang,
            isProgressMode = true,
            onDismiss = { showAddDialog = false },
            onSave = { title, sub, sched, target, notes ->
                onSaveItem(null, "STUDY_SKILL", title, sub, sched, 0, target, notes, state.selectedDate, false)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MadrasaItemDialog(
    titleLabel: String,
    initialSub: String = "",
    lang: String,
    isProgressMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, sub: String, sched: String, target: Int, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var sub by remember { mutableStateOf(initialSub) }
    var sched by remember { mutableStateOf("") }
    var target by remember { mutableStateOf(if (isProgressMode) "100" else "1") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.t(lang, "শিরোনাম / কিতাব / কাজের নাম", "Title / Book / Task")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sub,
                    onValueChange = { sub = it },
                    label = {
                        Text(
                            if (isProgressMode) AppStrings.t(lang, "বিষয় (হাদিস/ফিকহ/আরবি/দক্ষতা)", "Subject / Skill")
                            else AppStrings.t(lang, "শ্রেণি / ছাত্র / ব্যক্তি", "Class / Student / Person")
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isProgressMode) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { if (it.all(Char::isDigit)) target = it },
                        label = { Text(AppStrings.t(lang, "মোট লক্ষ্য (পৃষ্ঠা/পাঠ/অধ্যায়)", "Total Target (pages/lessons)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = sched,
                        onValueChange = { sched = it },
                        label = { Text(AppStrings.t(lang, "সময়সূচি / দিন / তারিখ", "Schedule / Time")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(AppStrings.t(lang, "বিস্তারিত নোট", "Notes")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) onSave(title, sub, sched, target.toIntOrNull() ?: 1, notes)
            }) { Text(AppStrings.t(lang, "সংরক্ষণ", "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.t(lang, "বাতিল", "Cancel")) }
        }
    )
}
