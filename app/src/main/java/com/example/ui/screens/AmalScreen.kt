package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.data.local.AmalDefinitionEntity
import com.example.data.local.DailyPrayerLogEntity
import com.example.data.local.QuranProgressEntity
import com.example.ui.components.AnimatedEmeraldProgress
import com.example.ui.components.DateNavigatorStrip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionCard
import com.example.ui.viewmodel.MainUiState

@Composable
fun AmalScreen(
    state: MainUiState,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToday: () -> Unit,
    onIncrementAmal: (String, Int) -> Unit,
    onUpdateAmalNote: (String, String) -> Unit,
    onSaveAmal: (id: String?, name: String, category: String, target: Int, unit: String, note: String) -> Unit,
    onDeleteAmal: (String) -> Unit,
    onCyclePrayer: (String) -> Unit,
    onUpdatePrayerExtras: ((DailyPrayerLogEntity) -> DailyPrayerLogEntity) -> Unit,
    onAdjustQaza: (String, Int) -> Unit,
    onUpdateQuran: ((QuranProgressEntity) -> QuranProgressEntity) -> Unit
) {
    val lang = state.prefs.language
    var tabIndex by remember { mutableIntStateOf(0) }
    var showAmalDialog by remember { mutableStateOf(false) }
    var editingAmal by remember { mutableStateOf<AmalDefinitionEntity?>(null) }

    val tabs = listOf(
        AppStrings.t(lang, "দৈনিক আমল", "Daily Amals"),
        AppStrings.t(lang, "নামাজ ও কাজা", "Prayers & Qaza"),
        AppStrings.t(lang, "কুরআন ও হিফজ", "Quran & Hifz")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("amal_screen_list"),
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = tabIndex == idx,
                        onClick = { tabIndex = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
                        modifier = Modifier.testTag("amal_tab_$idx")
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }
        }

        when (tabIndex) {
            // TAB 0: Daily Amals & Digital Tasbih
            0 -> {
                item {
                    SectionCard(
                        title = AppStrings.t(lang, "দৈনিক আমল ট্র্যাকার", "Daily Amal Tracker"),
                        icon = Icons.Default.AutoAwesome,
                        trailing = {
                            Button(
                                onClick = {
                                    editingAmal = null
                                    showAmalDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_amal_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(AppStrings.t(lang, "নতুন আমল", "Add Amal"))
                            }
                        }
                    ) {
                        val s = state.selectedSummary
                        Text(
                            text = AppStrings.t(
                                lang,
                                "আজ সম্পন্ন: ${AppStrings.digits(s.amalsCompleted, lang)}/${AppStrings.digits(s.amalsTotal, lang)} টি আমল • ধারাবাহিকতা: ${AppStrings.digits(state.amalStreakDays, lang)} দিন",
                                "Completed: ${s.amalsCompleted}/${s.amalsTotal} • Streak: ${state.amalStreakDays} days"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        val frac = if (s.amalsTotal == 0) 0f else s.amalsCompleted.toFloat() / s.amalsTotal
                        AnimatedEmeraldProgress(progressFraction = frac)
                    }
                }

                if (state.amals.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.AutoAwesome,
                            message = AppStrings.t(lang, "কোনো আমল যোগ করা হয়নি।", "No amals configured yet."),
                            actionLabel = AppStrings.t(lang, "+ আমল যোগ করুন", "+ Add Amal"),
                            onAction = { showAmalDialog = true }
                        )
                    }
                } else {
                    items(state.amals, key = { it.id }) { amal ->
                        val log = state.amalCountsSelectedDate[amal.id]
                        val count = log?.count ?: 0
                        val note = log?.note ?: ""
                        val done = count >= amal.dailyTarget
                        var noteExpanded by remember { mutableStateOf(false) }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (done) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = amal.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = AppStrings.t(
                                                lang,
                                                "লক্ষ্য: ${AppStrings.digits(amal.dailyTarget, lang)} ${amal.unit}",
                                                "Target: ${amal.dailyTarget} ${amal.unit}"
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${AppStrings.digits(count, lang)}/${AppStrings.digits(amal.dailyTarget, lang)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                AnimatedEmeraldProgress(
                                    progressFraction = (count.toFloat() / amal.dailyTarget.coerceAtLeast(1))
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { onIncrementAmal(amal.id, -1) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) { Text("−১") }
                                        Button(
                                            onClick = { onIncrementAmal(amal.id, 1) },
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                        ) { Text("+১") }
                                        if (amal.dailyTarget >= 10) {
                                            FilledTonalButton(
                                                onClick = { onIncrementAmal(amal.id, 10) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) { Text("+১০") }
                                        }
                                        if (amal.dailyTarget >= 33) {
                                            FilledTonalButton(
                                                onClick = { onIncrementAmal(amal.id, 33) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) { Text("+৩৩") }
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = { noteExpanded = !noteExpanded }) {
                                            Icon(Icons.Default.EditNote, contentDescription = "Note")
                                        }
                                        IconButton(onClick = {
                                            editingAmal = amal
                                            showAmalDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = { onDeleteAmal(amal.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                if (noteExpanded || note.isNotBlank()) {
                                    var noteText by remember(amal.id, state.selectedDate, note) { mutableStateOf(note) }
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        label = { Text(AppStrings.t(lang, "ব্যক্তিগত নোট", "Personal Note")) },
                                        trailingIcon = {
                                            IconButton(onClick = { onUpdateAmalNote(amal.id, noteText) }) {
                                                Icon(Icons.Default.Save, contentDescription = "Save note")
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: Five Daily Prayers + Nafl + Prep + Qaza
            1 -> {
                val log = state.prayerLogSelectedDate
                val schedule = state.todayPrayerSchedule
                item {
                    SectionCard(
                        title = AppStrings.t(lang, "পাঁচ ওয়াক্ত নামাজ", "Five Daily Prayers"),
                        icon = Icons.Default.Mosque
                    ) {
                        val waqts = listOf(
                            Triple("fajr", log.fajrStatus, schedule?.fajr ?: "--:--"),
                            Triple("zuhr", log.zuhrStatus, schedule?.zuhr ?: "--:--"),
                            Triple("asr", log.asrStatus, schedule?.asr ?: "--:--"),
                            Triple("maghrib", log.maghribStatus, schedule?.maghrib ?: "--:--"),
                            Triple("isha", log.ishaStatus, schedule?.isha ?: "--:--")
                        )
                        waqts.forEach { (key, status, time) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = AppStrings.prayerName(key, lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = AppStrings.t(lang, "ওয়াক্ত: ${AppStrings.digits(time, lang)}", "Time: $time"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (status > 0) {
                                    Button(
                                        onClick = { onCyclePrayer(key) },
                                        modifier = Modifier.testTag("prayer_btn_$key")
                                    ) {
                                        Text(AppStrings.prayerStatusLabel(status, lang))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onCyclePrayer(key) },
                                        modifier = Modifier.testTag("prayer_btn_$key")
                                    ) {
                                        Text(AppStrings.prayerStatusLabel(status, lang))
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = AppStrings.t(lang, "বাটনে চাপলে পরিবর্তন হবে: বাকি → একা আদায় → জামাতে আদায়।", "Tap button to cycle: Pending → Alone → Jamaat."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    SectionCard(
                        title = AppStrings.t(lang, "নফল নামাজ ও পূর্বপ্রস্তুতি", "Nafl Prayers & Preparation"),
                        icon = Icons.Default.CheckCircleOutline
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = log.tahajjudDone,
                                onClick = { onUpdatePrayerExtras { it.copy(tahajjudDone = !it.tahajjudDone) } },
                                label = { Text(AppStrings.t(lang, "তাহাজ্জুদ", "Tahajjud")) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = log.ishraqDone,
                                onClick = { onUpdatePrayerExtras { it.copy(ishraqDone = !it.ishraqDone) } },
                                label = { Text(AppStrings.t(lang, "ইশরাক", "Ishraq")) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = log.chashtDone,
                                onClick = { onUpdatePrayerExtras { it.copy(chashtDone = !it.chashtDone) } },
                                label = { Text(AppStrings.t(lang, "চাশত", "Chasht")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        val preps = listOf(
                            Triple(log.prepMiswak, AppStrings.t(lang, "মিসওয়াক", "Miswak")) { l: DailyPrayerLogEntity -> l.copy(prepMiswak = !l.prepMiswak) },
                            Triple(log.prepWudu, AppStrings.t(lang, "পূর্ণ অজু", "Proper Wudu")) { l: DailyPrayerLogEntity -> l.copy(prepWudu = !l.prepWudu) },
                            Triple(log.prepCleanDress, AppStrings.t(lang, "পবিত্র পোশাক", "Clean Dress")) { l: DailyPrayerLogEntity -> l.copy(prepCleanDress = !l.prepCleanDress) },
                            Triple(log.prepMasjid, AppStrings.t(lang, "সময়মতো মসজিদে গমন", "Early to Masjid")) { l: DailyPrayerLogEntity -> l.copy(prepMasjid = !l.prepMasjid) }
                        )
                        preps.forEach { (checked, label, updater) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = checked, onCheckedChange = { onUpdatePrayerExtras(updater) })
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    SectionCard(
                        title = AppStrings.t(lang, "কাজা নামাজের ব্যক্তিগত হিসাব", "Personal Qaza Prayer Tracker"),
                        icon = Icons.Default.History
                    ) {
                        val qazaItems = listOf(
                            "fajr" to state.prefs.qazaFajr,
                            "zuhr" to state.prefs.qazaZuhr,
                            "asr" to state.prefs.qazaAsr,
                            "maghrib" to state.prefs.qazaMaghrib,
                            "isha" to state.prefs.qazaIsha
                        )
                        qazaItems.forEach { (key, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(AppStrings.prayerName(key, lang), style = MaterialTheme.typography.titleSmall)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onAdjustQaza(key, -1) }) {
                                        Text(AppStrings.t(lang, "আদায় −১", "Done -1"))
                                    }
                                    Text(
                                        text = AppStrings.digits(count, lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledTonalButton(onClick = { onAdjustQaza(key, 1) }) {
                                        Text("+১")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: Quran Tilawat, Hifz, Muraja'a
            2 -> {
                val q = state.quranSelectedDate
                item {
                    SectionCard(
                        title = AppStrings.t(lang, "কুরআন তিলাওয়াত, হিফজ ও মুরাজাআ", "Quran Tilawat, Hifz & Muraja'a"),
                        icon = Icons.Default.MenuBook
                    ) {
                        val target = state.prefs.dailyQuranTargetPages.coerceAtLeast(1)
                        Text(
                            text = AppStrings.t(
                                lang,
                                "দৈনিক লক্ষ্য: ${AppStrings.digits(q.pagesRead, lang)}/${AppStrings.digits(target, lang)} পৃষ্ঠা",
                                "Daily Goal: ${q.pagesRead}/$target pages"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        AnimatedEmeraldProgress(progressFraction = q.pagesRead.toFloat() / target)
                        Spacer(Modifier.height(14.dp))

                        QuranCounterRow(
                            label = AppStrings.t(lang, "তিলাওয়াত (পৃষ্ঠা)", "Tilawat (Pages)"),
                            value = q.pagesRead,
                            lang = lang,
                            onChange = { next -> onUpdateQuran { it.copy(pagesRead = next) } }
                        )
                        QuranCounterRow(
                            label = AppStrings.t(lang, "তিলাওয়াত (আয়াত)", "Tilawat (Ayahs)"),
                            value = q.ayahsRead,
                            lang = lang,
                            stepLarge = 10,
                            onChange = { next -> onUpdateQuran { it.copy(ayahsRead = next) } }
                        )
                        QuranCounterRow(
                            label = AppStrings.t(lang, "নতুন হিফজ (লাইন)", "New Hifz (Lines)"),
                            value = q.hifzLines,
                            lang = lang,
                            onChange = { next -> onUpdateQuran { it.copy(hifzLines = next) } }
                        )
                        QuranCounterRow(
                            label = AppStrings.t(lang, "মুরাজাআ / দোহরানো (পৃষ্ঠা)", "Muraja'a Revision (Pages)"),
                            value = q.murajaPages,
                            lang = lang,
                            onChange = { next -> onUpdateQuran { it.copy(murajaPages = next) } }
                        )

                        var surahNote by remember(state.selectedDate, q.surahNote) { mutableStateOf(q.surahNote) }
                        OutlinedTextField(
                            value = surahNote,
                            onValueChange = { surahNote = it },
                            label = { Text(AppStrings.t(lang, "সুরা / পারা ও নোট", "Surah / Juz & Note")) },
                            trailingIcon = {
                                IconButton(onClick = { onUpdateQuran { it.copy(surahNote = surahNote) } }) {
                                    Icon(Icons.Default.Save, contentDescription = "Save note")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAmalDialog) {
        AmalEditorDialog(
            initial = editingAmal,
            lang = lang,
            onDismiss = { showAmalDialog = false },
            onSave = { name, cat, target, unit, note ->
                onSaveAmal(editingAmal?.id, name, cat, target, unit, note)
                showAmalDialog = false
            }
        )
    }
}

@Composable
private fun QuranCounterRow(
    label: String,
    value: Int,
    lang: String,
    stepLarge: Int = 5,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = { onChange((value - 1).coerceAtLeast(0)) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("−১") }
            Text(
                text = AppStrings.digits(value, lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { onChange(value + 1) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("+১") }
            FilledTonalButton(
                onClick = { onChange(value + stepLarge) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("+${AppStrings.digits(stepLarge, lang)}") }
        }
    }
}

@Composable
private fun AmalEditorDialog(
    initial: AmalDefinitionEntity?,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, target: Int, unit: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "ZIKR") }
    var target by remember { mutableStateOf((initial?.dailyTarget ?: 1).toString()) }
    var unit by remember { mutableStateOf(initial?.unit ?: "বার") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    val cats = listOf("NAMAZ", "QURAN", "ZIKR", "SUNNAH", "CHARITY", "ILM")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) AppStrings.t(lang, "নতুন আমল যোগ করুন", "New Amal")
                else AppStrings.t(lang, "আমল সম্পাদনা", "Edit Amal")
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.t(lang, "আমলের নাম", "Amal Name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cats.forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { if (it.all(Char::isDigit)) target = it },
                        label = { Text(AppStrings.t(lang, "দৈনিক লক্ষ্য", "Daily Target")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(AppStrings.t(lang, "একক (বার/পৃষ্ঠা)", "Unit")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(AppStrings.t(lang, "নোট", "Note")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onSave(name, category, target.toIntOrNull() ?: 1, unit, note)
            }) {
                Text(AppStrings.t(lang, "সংরক্ষণ", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.t(lang, "বাতিল", "Cancel")) }
        }
    )
}
