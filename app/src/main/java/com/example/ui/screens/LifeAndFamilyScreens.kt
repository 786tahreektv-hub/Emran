package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.HealthDailyEntity
import com.example.data.local.RoutineTaskEntity
import com.example.ui.components.AnimatedEmeraldProgress
import com.example.ui.components.DateNavigatorStrip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionCard
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ResolvedTaskItem

@Composable
fun DailyLifeHealthScreen(
    state: MainUiState,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToday: () -> Unit,
    onUpdateHealth: ((HealthDailyEntity) -> HealthDailyEntity) -> Unit,
    onToggleTask: (ResolvedTaskItem) -> Unit,
    onAddPersonalHealthTask: (title: String, time: String, category: String) -> Unit
) {
    val lang = state.prefs.language
    val h = state.healthSelectedDate
    val lifeTasks = state.resolvedTasksForSelectedDate.filter {
        it.task.category == "HEALTH" || it.task.category == "PERSONAL"
    }
    var quickTitle by remember { mutableStateOf("") }
    var quickTime by remember { mutableStateOf("") }

    val quickChips = listOf(
        AppStrings.t(lang, "বাজার করা", "Grocery / Bazar"),
        AppStrings.t(lang, "ঘর পরিষ্কার", "Home Cleaning"),
        AppStrings.t(lang, "কাপড় ধোয়া", "Laundry"),
        AppStrings.t(lang, "প্রয়োজনীয় কেনাকাটা", "Shopping"),
        AppStrings.t(lang, "পুষ্টিকর খাবার", "Healthy Meal")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("daily_life_screen"),
        contentPadding = PaddingValues(16.dp),
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

        // Personal Hygiene & Cleanliness
        item {
            SectionCard(
                title = AppStrings.t(lang, "ব্যক্তিগত পরিচ্ছন্নতা", "Personal Hygiene"),
                icon = Icons.Default.CleanHands
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = h.bathDone,
                        onClick = { onUpdateHealth { it.copy(bathDone = !it.bathDone) } },
                        label = { Text(AppStrings.t(lang, "গোসল", "Bath")) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = h.brushDone,
                        onClick = { onUpdateHealth { it.copy(brushDone = !it.brushDone) } },
                        label = { Text(AppStrings.t(lang, "দাঁত ব্রাশ", "Brush")) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = h.cleanClothesDone,
                        onClick = { onUpdateHealth { it.copy(cleanClothesDone = !it.cleanClothesDone) } },
                        label = { Text(AppStrings.t(lang, "পবিত্র পোশাক", "Clean Dress")) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Water, Exercise, Walk, Sleep
        item {
            SectionCard(
                title = AppStrings.t(lang, "পানি, ঘুম ও ব্যায়াম ট্র্যাকার", "Water, Sleep & Exercise"),
                icon = Icons.Default.FitnessCenter
            ) {
                val targetW = state.prefs.dailyWaterTargetGlasses.coerceAtLeast(1)
                Text(
                    text = AppStrings.t(
                        lang,
                        "পানি পান: ${AppStrings.digits(h.waterGlasses, lang)}/${AppStrings.digits(targetW, lang)} গ্লাস",
                        "Water: ${h.waterGlasses}/$targetW glasses"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                AnimatedEmeraldProgress(progressFraction = h.waterGlasses.toFloat() / targetW)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onUpdateHealth { it.copy(waterGlasses = (it.waterGlasses - 1).coerceAtLeast(0)) } }) {
                        Text("−১ গ্লাস")
                    }
                    Button(onClick = { onUpdateHealth { it.copy(waterGlasses = it.waterGlasses + 1) } }) {
                        Text("+১ গ্লাস")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onUpdateHealth { it.copy(exerciseMinutes = it.exerciseMinutes + 15) } }, modifier = Modifier.weight(1f)) {
                        Text(AppStrings.t(lang, "ব্যায়াম +১৫মি (${AppStrings.digits(h.exerciseMinutes, lang)})", "Exercise +15m (${h.exerciseMinutes})"))
                    }
                    OutlinedButton(onClick = { onUpdateHealth { it.copy(walkMinutes = it.walkMinutes + 15) } }, modifier = Modifier.weight(1f)) {
                        Text(AppStrings.t(lang, "হাঁটা +১৫মি (${AppStrings.digits(h.walkMinutes, lang)})", "Walk +15m (${h.walkMinutes})"))
                    }
                }
                Spacer(Modifier.height(10.dp))
                var sleepHours by remember(state.selectedDate, h.sleepHours) {
                    mutableStateOf(if (h.sleepHours > 0f) h.sleepHours.toString() else "")
                }
                var medicineNote by remember(state.selectedDate, h.medicineNote) { mutableStateOf(h.medicineNote) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        label = { Text(AppStrings.t(lang, "ঘুম (ঘণ্টা)", "Sleep (hrs)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            onUpdateHealth { it.copy(sleepHours = sleepHours.toFloatOrNull() ?: 0f, medicineNote = medicineNote) }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(AppStrings.t(lang, "সংরক্ষণ", "Save"))
                    }
                }
                OutlinedTextField(
                    value = medicineNote,
                    onValueChange = { medicineNote = it },
                    label = { Text(AppStrings.t(lang, "ওষুধ / খাবার / স্বাস্থ্য নোট", "Medicine / Diet / Health Note")) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }

        // Personal Chores, Shopping, Bazar, Laundry
        item {
            SectionCard(
                title = AppStrings.t(lang, "বাজার, ঘর পরিষ্কার ও ব্যক্তিগত কাজ", "Chores, Bazar & Personal Tasks"),
                icon = Icons.Default.ShoppingBasket
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickChips.take(3).forEach { chip ->
                        SuggestionChip(
                            onClick = { quickTitle = chip },
                            label = { Text(chip, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quickTitle,
                        onValueChange = { quickTitle = it },
                        label = { Text(AppStrings.t(lang, "কাজ / বাজার / কেনাকাটা", "Task / Shopping")) },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = quickTime,
                        onValueChange = { quickTime = it },
                        label = { Text(AppStrings.t(lang, "সময়", "Time")) },
                        placeholder = { Text("17:30") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (quickTitle.isNotBlank()) {
                            onAddPersonalHealthTask(quickTitle, quickTime, "PERSONAL")
                            quickTitle = ""; quickTime = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(AppStrings.t(lang, "+ দৈনন্দিন তালিকায় যোগ করুন", "+ Add to Daily Life List"))
                }
            }
        }

        items(lifeTasks, key = { it.task.id }) { item ->
            Card(onClick = { onToggleTask(item) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = item.isCompleted, onCheckedChange = { onToggleTask(item) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.task.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${AppStrings.digits(item.task.startTime.ifBlank { "--:--" }, lang)} • ${AppStrings.categoryLabel(item.task.category, lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FamilySocialScreen(
    state: MainUiState,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToday: () -> Unit,
    onToggleTask: (ResolvedTaskItem) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddFamilyTask: (title: String, personTag: String, time: String, recurrence: String, note: String) -> Unit
) {
    val lang = state.prefs.language
    val familyTasks = state.resolvedTasksForSelectedDate.filter { it.task.category == "FAMILY" }
    val peopleGroups = listOf(
        AppStrings.t(lang, "স্ত্রী", "Spouse"),
        AppStrings.t(lang, "সন্তান", "Children"),
        AppStrings.t(lang, "বাবা-মা", "Parents"),
        AppStrings.t(lang, "আত্মীয়স্বজন", "Relatives"),
        AppStrings.t(lang, "প্রতিবেশী", "Neighbors")
    )

    var selectedPerson by remember { mutableStateOf(peopleGroups.first()) }
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("19:30") }
    var recurrence by remember { mutableStateOf("DAILY") }
    var note by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("family_screen"),
        contentPadding = PaddingValues(16.dp),
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

        item {
            SectionCard(
                title = AppStrings.t(lang, "পরিবার ও সামাজিক দায়িত্ব", "Family & Social Responsibilities"),
                icon = Icons.Default.FamilyRestroom
            ) {
                Text(
                    text = AppStrings.t(lang, "কার জন্য সময় বা দায়িত্ব নির্ধারণ করবেন:", "Select family/social group:"),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    peopleGroups.take(3).forEach { p ->
                        FilterChip(
                            selected = selectedPerson == p,
                            onClick = { selectedPerson = p },
                            label = { Text(p) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    peopleGroups.drop(3).forEach { p ->
                        FilterChip(
                            selected = selectedPerson == p,
                            onClick = { selectedPerson = p },
                            label = { Text(p) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.t(lang, "দায়িত্ব / সময় দেওয়ার বিবরণ", "Responsibility / Quality Time")) },
                    placeholder = { Text(AppStrings.t(lang, "যেমন: সন্তানের পড়াশোনা দেখা", "e.g. Help children study")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text(AppStrings.t(lang, "সময় (HH:mm)", "Time")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = recurrence == "DAILY",
                        onClick = { recurrence = if (recurrence == "DAILY") "WEEKLY" else "DAILY" },
                        label = { Text(AppStrings.recurrenceLabel(recurrence, lang)) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(AppStrings.t(lang, "নোট", "Note")) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddFamilyTask("$selectedPerson: $title", selectedPerson, time, recurrence, note)
                            title = ""; note = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(AppStrings.t(lang, "পারিবারিক দায়িত্ব যোগ করুন", "Add Family Task"))
                }
            }
        }

        if (familyTasks.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.People,
                    message = AppStrings.t(lang, "এই দিনে কোনো পারিবারিক বা সামাজিক কাজ নেই।", "No family or social tasks for this date.")
                )
            }
        } else {
            items(familyTasks, key = { it.task.id }) { item ->
                Card(onClick = { onToggleTask(item) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = item.isCompleted, onCheckedChange = { onToggleTask(item) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.task.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${AppStrings.digits(item.task.startTime, lang)} • ${AppStrings.recurrenceLabel(item.task.recurrence, lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDeleteTask(item.task.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
