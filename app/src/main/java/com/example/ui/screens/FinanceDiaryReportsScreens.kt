package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.data.local.CustomReminderEntity
import com.example.data.local.FinanceTransactionEntity
import com.example.notifications.NotificationHelper
import com.example.ui.components.*
import com.example.ui.viewmodel.DecryptedDiaryNote
import com.example.ui.viewmodel.MainUiState

@Composable
fun FinanceScreen(
    state: MainUiState,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToday: () -> Unit,
    onAddFinance: (date: String, type: String, category: String, amount: Double, note: String) -> Unit,
    onToggleSettled: (FinanceTransactionEntity) -> Unit,
    onDeleteFinance: (String) -> Unit,
    onUpdateBudget: (Double) -> Unit
) {
    val lang = state.prefs.language
    val types = listOf("INCOME", "EXPENSE", "SADAQAH", "SAVINGS", "DUE_RECEIVABLE", "DUE_PAYABLE")
    val categories = listOf(
        AppStrings.t(lang, "বাজার", "Bazar"),
        AppStrings.t(lang, "পরিবার", "Family"),
        AppStrings.t(lang, "যাতায়াত", "Transport"),
        AppStrings.t(lang, "দান-সদকা", "Sadaqah"),
        AppStrings.t(lang, "বেতন/হাদিয়া", "Salary/Gift"),
        AppStrings.t(lang, "সঞ্চয়", "Savings"),
        AppStrings.t(lang, "বকেয়া", "Due"),
        AppStrings.t(lang, "অন্যান্য", "Other")
    )

    var selectedType by remember { mutableStateOf("EXPENSE") }
    var selectedCat by remember { mutableStateOf(categories.first()) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var budgetText by remember(state.prefs.monthlyBudget) {
        mutableStateOf(if (state.prefs.monthlyBudget > 0) state.prefs.monthlyBudget.toInt().toString() else "")
    }

    val dayTransactions = state.financeList.filter { it.date == state.selectedDate }

    // Build last 6 months Income vs Expense series for DualBarChart
    val currentYear = state.todayIso.take(4).toIntOrNull() ?: 2026
    val currentMonth = state.todayIso.substring(5, 7).toIntOrNull() ?: 11
    val chartMonths = (-4..0).map { offset ->
        var y = currentYear
        var m = currentMonth + offset
        if (m < 1) { m += 12; y-- }
        val prefix = String.format(java.util.Locale.US, "%04d-%02d", y, m)
        Triple(
            CalendarEngine.gregMonthName(m, lang).take(3),
            state.financeList.filter { it.date.startsWith(prefix) && it.type == "INCOME" }.sumOf { it.amount }.toFloat(),
            state.financeList.filter { it.date.startsWith(prefix) && it.type == "EXPENSE" }.sumOf { it.amount }.toFloat()
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("finance_screen"),
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

        // Monthly Summary & Budget Progress
        item {
            SectionCard(
                title = AppStrings.t(lang, "মাসিক আর্থিক সারাংশ ও বাজেট", "Monthly Finance & Budget"),
                icon = Icons.Default.AccountBalanceWallet
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        value = "৳${AppStrings.digits(state.monthIncome.toInt(), lang)}",
                        label = AppStrings.t(lang, "মাসিক আয়", "Month Income"),
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "৳${AppStrings.digits(state.monthExpense.toInt(), lang)}",
                        label = AppStrings.t(lang, "মাসিক ব্যয়", "Month Expense"),
                        icon = Icons.Default.TrendingDown,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        value = "৳${AppStrings.digits(state.monthSadaqah.toInt(), lang)}",
                        label = AppStrings.t(lang, "দান-সদকা", "Sadaqah"),
                        icon = Icons.Default.VolunteerActivism,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "৳${AppStrings.digits(state.monthSavings.toInt(), lang)}",
                        label = AppStrings.t(lang, "সঞ্চয়", "Savings"),
                        icon = Icons.Default.Savings,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = AppStrings.t(
                        lang,
                        "বকেয়া হিসাব — পাওনা: ৳${AppStrings.digits(state.totalReceivableDue.toInt(), lang)} | দেনা: ৳${AppStrings.digits(state.totalPayableDue.toInt(), lang)}",
                        "Dues — Receivable: ৳${state.totalReceivableDue.toInt()} | Payable: ৳${state.totalPayableDue.toInt()}"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { if (it.all(Char::isDigit)) budgetText = it },
                        label = { Text(AppStrings.t(lang, "মাসিক বাজেট (৳)", "Monthly Budget (৳)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { onUpdateBudget(budgetText.toDoubleOrNull() ?: 0.0) }) {
                        Text(AppStrings.t(lang, "সেট", "Set"))
                    }
                }
                if (state.prefs.monthlyBudget > 0) {
                    Spacer(Modifier.height(6.dp))
                    val ratio = (state.monthExpense / state.prefs.monthlyBudget).toFloat()
                    AnimatedEmeraldProgress(progressFraction = ratio)
                    Text(
                        text = AppStrings.t(
                            lang,
                            "বাজেট ব্যবহৃত: ${AppStrings.digits((ratio * 100).toInt(), lang)}%",
                            "Budget used: ${(ratio * 100).toInt()}%"
                        ),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Income vs Expense Chart
        item {
            SectionCard(
                title = AppStrings.t(lang, "আয় ও ব্যয়ের চার্ট (৫ মাস)", "Income vs Expense Chart"),
                icon = Icons.Default.BarChart
            ) {
                DualBarChart(
                    labels = chartMonths.map { it.first },
                    seriesA = chartMonths.map { it.second },
                    seriesB = chartMonths.map { it.third },
                    labelA = AppStrings.t(lang, "আয়", "Income"),
                    labelB = AppStrings.t(lang, "ব্যয়", "Expense")
                )
            }
        }

        // Add Transaction Card
        item {
            SectionCard(
                title = AppStrings.t(lang, "নতুন হিসাব যোগ করুন", "Add Transaction"),
                icon = Icons.Default.AddCard
            ) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { tp ->
                        FilterChip(
                            selected = selectedType == tp,
                            onClick = { selectedType = tp },
                            label = { Text(AppStrings.financeTypeLabel(tp, lang)) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(AppStrings.t(lang, "পরিমাণ (৳)", "Amount (৳)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("finance_amount_input")
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(AppStrings.t(lang, "বিবরণ", "Note")) },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            onAddFinance(state.selectedDate, selectedType, selectedCat, amt, noteText)
                            amountText = ""; noteText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("finance_add_btn")
                ) {
                    Text(AppStrings.t(lang, "+ হিসাব সংরক্ষণ করুন", "+ Save Transaction"))
                }
            }
        }

        if (dayTransactions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.ReceiptLong,
                    message = AppStrings.t(lang, "এই তারিখে কোনো আয়-ব্যয়ের হিসাব নেই।", "No financial records for this date.")
                )
            }
        } else {
            items(dayTransactions, key = { it.id }) { tx ->
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${AppStrings.financeTypeLabel(tx.type, lang)} • ${tx.category}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (tx.note.isNotBlank()) {
                                Text(tx.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (tx.type.startsWith("DUE_")) {
                                TextButton(onClick = { onToggleSettled(tx) }, contentPadding = PaddingValues(0.dp)) {
                                    Text(
                                        if (tx.isSettled) AppStrings.t(lang, "পরিশোধিত ✔", "Settled ✔")
                                        else AppStrings.t(lang, "বকেয়া — পরিশোধ চিহ্নিত করুন", "Mark Settled")
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳${AppStrings.digits(tx.amount.toInt(), lang)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == "INCOME" || tx.type == "SAVINGS") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            IconButton(onClick = { onDeleteFinance(tx.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryScreen(
    state: MainUiState,
    onSaveDiary: (id: String?, date: String, type: String, title: String, body: String, tag: String, locked: Boolean) -> Unit,
    onDeleteDiary: (String) -> Unit
) {
    val lang = state.prefs.language
    val types = listOf(
        "DIARY" to AppStrings.t(lang, "ব্যক্তিগত ডায়েরি", "Personal Diary"),
        "MUHASABAH" to AppStrings.t(lang, "আত্মসমালোচনা", "Muhasabah"),
        "PLAN" to AppStrings.t(lang, "ভবিষ্যৎ পরিকল্পনা", "Future Plan"),
        "THOUGHT" to AppStrings.t(lang, "চিন্তাভাবনা ও নোট", "Thoughts & Notes")
    )
    var selectedType by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<DecryptedDiaryNote?>(null) }

    val filtered = state.diaryNotes.filter { note ->
        (selectedType == "ALL" || note.entity.type == selectedType) &&
            (searchQuery.isBlank() || note.entity.title.contains(searchQuery, true) || note.plainBody.contains(searchQuery, true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("diary_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(
                title = AppStrings.t(lang, "নোট ও এনক্রিপ্টেড ডায়েরি", "Notes & Encrypted Diary"),
                icon = Icons.Default.Lock,
                trailing = {
                    Button(onClick = { editingNote = null; showEditor = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(AppStrings.t(lang, "নতুন লিখুন", "Write"))
                    }
                }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(AppStrings.t(lang, "নোট খুঁজুন...", "Search notes...")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedType == "ALL",
                        onClick = { selectedType = "ALL" },
                        label = { Text(AppStrings.t(lang, "সব", "All")) }
                    )
                    types.forEach { (k, lbl) ->
                        FilterChip(selected = selectedType == k, onClick = { selectedType = k }, label = { Text(lbl) })
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.EditNote,
                    message = AppStrings.t(
                        lang,
                        "আপনার দৈনিক অভিজ্ঞতা, আত্মসমালোচনা ও ভবিষ্যৎ পরিকল্পনা নিরাপদে লিখে রাখুন। সব নোট AES-256 এনক্রিপ্টেড।",
                        "Write your daily reflections, Muhasabah, and plans. All entries are encrypted locally with Android Keystore."
                    ),
                    actionLabel = AppStrings.t(lang, "+ নতুন ডায়েরি লিখুন", "+ New Diary Entry"),
                    onAction = { editingNote = null; showEditor = true }
                )
            }
        } else {
            items(filtered, key = { it.entity.id }) { item ->
                Card(onClick = { editingNote = item; showEditor = true }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.entity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${item.entity.date} • ${types.firstOrNull { it.first == item.entity.type }?.second ?: item.entity.type}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onDeleteDiary(item.entity.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(item.plainBody, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
                    }
                }
            }
        }
    }

    if (showEditor) {
        var title by remember { mutableStateOf(editingNote?.entity?.title ?: "") }
        var type by remember { mutableStateOf(editingNote?.entity?.type ?: "DIARY") }
        var body by remember { mutableStateOf(editingNote?.plainBody ?: "") }
        var tag by remember { mutableStateOf(editingNote?.entity?.moodOrTag ?: "") }

        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(AppStrings.t(lang, "নোট / ডায়েরি সংরক্ষণ", "Diary & Note Editor")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        types.forEach { (k, lbl) ->
                            FilterChip(selected = type == k, onClick = { type = k }, label = { Text(lbl) })
                        }
                    }
                    if (type == "MUHASABAH" && body.isBlank()) {
                        TextButton(onClick = {
                            body = "১. আজকের সবচেয়ে ভালো কাজ:\n২. আজ কোন ভুল/গুনাহ হয়েছে:\n৩. কোন আমলে ঘাটতি ছিল:\n৪. আজ নতুন কী শিখেছি:\n৫. আগামীকালের সংশোধন পরিকল্পনা:"
                        }) {
                            Text(AppStrings.t(lang, "মুহাসাবা টেমপ্লেট বসাও", "Insert Muhasabah Prompts"))
                        }
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(AppStrings.t(lang, "শিরোনাম", "Title")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(AppStrings.t(lang, "বিস্তারিত লিখুন", "Write details")) },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text(AppStrings.t(lang, "ট্যাগ / অনুভূতি", "Tag / Mood")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() || body.isNotBlank()) {
                        onSaveDiary(
                            editingNote?.entity?.id,
                            editingNote?.entity?.date ?: state.selectedDate,
                            type,
                            title.ifBlank { state.selectedDate },
                            body,
                            tag,
                            true
                        )
                        showEditor = false
                    }
                }) { Text(AppStrings.t(lang, "সংরক্ষণ", "Save")) }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text(AppStrings.t(lang, "বাতিল", "Cancel")) }
            }
        )
    }
}

@Composable
fun ReportsAnalyticsScreen(
    state: MainUiState,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    val lang = state.prefs.language
    val weeklyAvg = if (state.last7DaysSummaries.isEmpty()) 0
    else state.last7DaysSummaries.map { it.overallPercent }.average().toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(
                title = AppStrings.t(lang, "রিপোর্ট ও অ্যানালিটিক্স", "Reports & Analytics"),
                icon = Icons.Default.Analytics,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(onClick = onExportCsv) { Text("CSV") }
                        Button(onClick = onExportPdf) { Text("PDF") }
                    }
                }
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        value = "${AppStrings.digits(state.todaySummary.overallPercent, lang)}%",
                        label = AppStrings.t(lang, "দৈনিক হার", "Daily Rate"),
                        icon = Icons.Default.Today,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "${AppStrings.digits(weeklyAvg, lang)}%",
                        label = AppStrings.t(lang, "সাপ্তাহিক হার", "Weekly Avg"),
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        value = "${AppStrings.digits(state.monthlyAvgPercent, lang)}%",
                        label = AppStrings.t(lang, "৩০ দিনের গড়", "30-Day Avg"),
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "${AppStrings.digits(state.yearlyAvgPercent, lang)}%",
                        label = AppStrings.t(lang, "বার্ষিক গড়", "Yearly Avg"),
                        icon = Icons.Default.Timeline,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = AppStrings.t(
                        lang,
                        "আমলের ধারাবাহিকতা (Streak): ${AppStrings.digits(state.amalStreakDays, lang)} দিন",
                        "Amal Consistency Streak: ${state.amalStreakDays} days"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            SectionCard(
                title = AppStrings.t(lang, "গত ৭ দিনের অগ্রগতি গ্রাফ", "7-Day Progress Graph"),
                icon = Icons.Default.BarChart
            ) {
                val bars = state.last7DaysSummaries.map { s ->
                    BarDatum(
                        label = AppStrings.digits(s.date.takeLast(2), lang),
                        value = s.overallPercent.toFloat(),
                        displayValue = "${AppStrings.digits(s.overallPercent, lang)}%"
                    )
                }
                SimpleBarChart(data = bars)
            }
        }

        item {
            SectionCard(
                title = AppStrings.t(lang, "বিভাগভিত্তিক সময় ও কাজ সম্পন্নের হিসাব", "Time & Task Breakdown by Category"),
                icon = Icons.Default.PieChart
            ) {
                if (state.categoryBreakdownToday.isEmpty()) {
                    Text(
                        AppStrings.t(lang, "আজকের কোনো কাজের ডেটা নেই।", "No category task data for today."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.categoryBreakdownToday.forEach { cat ->
                        val frac = if (cat.totalTasks == 0) 0f else cat.completedTasks.toFloat() / cat.totalTasks
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(AppStrings.categoryLabel(cat.category, lang), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${AppStrings.digits(cat.completedTasks, lang)}/${AppStrings.digits(cat.totalTasks, lang)} (${AppStrings.digits(cat.totalMinutes, lang)}m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            AnimatedEmeraldProgress(progressFraction = frac, height = 8.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersScreen(
    state: MainUiState,
    onTogglePrayerRem: (Boolean) -> Unit,
    onToggleWaterRem: (Boolean) -> Unit,
    onUpdateSleepWake: (wake: String, sleep: String) -> Unit,
    onSaveReminder: (id: String?, title: String, type: String, time: String, date: String, interval: Int, enabled: Boolean) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onSendTestNotification: () -> Unit
) {
    val context = LocalContext.current
    val lang = state.prefs.language
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MEDICINE") }
    var time by remember { mutableStateOf("09:00") }
    var date by remember { mutableStateOf("") }

    var wakeTime by remember(state.prefs.wakeReminderTime) { mutableStateOf(state.prefs.wakeReminderTime) }
    var sleepTime by remember(state.prefs.sleepReminderTime) { mutableStateOf(state.prefs.sleepReminderTime) }

    val remTypes = listOf(
        "MEDICINE" to AppStrings.t(lang, "ওষুধ", "Medicine"),
        "AMAL" to AppStrings.t(lang, "আমল", "Amal"),
        "IMPORTANT_DATE" to AppStrings.t(lang, "গুরুত্বপূর্ণ তারিখ", "Important Date"),
        "CUSTOM" to AppStrings.t(lang, "ব্যক্তিগত", "Custom")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("reminders_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(
                title = AppStrings.t(lang, "রিমাইন্ডার ও নোটিফিকেশন", "Reminders & Notifications"),
                icon = Icons.Default.NotificationsActive
            ) {
                if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppStrings.t(lang, "নোটিফিকেশন অনুমতি প্রয়োজন", "Notification permission required"),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                Text(AppStrings.t(lang, "অনুমতি দিন", "Grant"))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(AppStrings.t(lang, "পাঁচ ওয়াক্ত নামাজের রিমাইন্ডার", "5 Daily Prayer Reminders"))
                    Switch(checked = state.prefs.prayerRemindersEnabled, onCheckedChange = onTogglePrayerRem)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(AppStrings.t(lang, "পানি পানের নিয়মিত রিমাইন্ডার", "Periodic Water Reminders"))
                    Switch(checked = state.prefs.waterRemindersEnabled, onCheckedChange = onToggleWaterRem)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text(AppStrings.t(lang, "ওঠার সময়", "Wake Time")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sleepTime,
                        onValueChange = { sleepTime = it },
                        label = { Text(AppStrings.t(lang, "ঘুমের সময়", "Sleep Time")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { onUpdateSleepWake(wakeTime, sleepTime) }) {
                        Text(AppStrings.t(lang, "সেভ", "Save"))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onSendTestNotification()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(AppStrings.t(lang, "টেস্ট নোটিফিকেশন পাঠান", "Send Test Notification"))
                }
            }
        }

        item {
            SectionCard(
                title = AppStrings.t(lang, "ওষুধ, আমল ও গুরুত্বপূর্ণ তারিখের রিমাইন্ডার", "Add Custom Reminder"),
                icon = Icons.Default.AddAlert
            ) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    remTypes.forEach { (k, lbl) ->
                        FilterChip(selected = type == k, onClick = { type = k }, label = { Text(lbl) })
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.t(lang, "রিমাইন্ডারের শিরোনাম", "Reminder Title")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text(AppStrings.t(lang, "সময় (HH:mm)", "Time (HH:mm)")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(AppStrings.t(lang, "তারিখ (ঐচ্ছিক)", "Date (optional)")) },
                        placeholder = { Text("yyyy-MM-dd") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveReminder(null, title, type, time, date, 0, true)
                            title = ""; date = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(AppStrings.t(lang, "+ রিমাইন্ডার যোগ করুন", "+ Add Reminder"))
                }
            }
        }

        items(state.reminders, key = { it.id }) { rem ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rem.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${AppStrings.digits(rem.time, lang)} ${if (rem.date.isNotBlank()) "• ${rem.date}" else "• দৈনিক"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = rem.isEnabled,
                            onCheckedChange = {
                                onSaveReminder(rem.id, rem.title, rem.type, rem.time, rem.date, rem.intervalHours, it)
                            }
                        )
                        IconButton(onClick = { onDeleteReminder(rem.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
