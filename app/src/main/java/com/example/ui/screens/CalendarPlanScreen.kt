package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.data.local.YearlyGoalEntity
import com.example.ui.components.*
import com.example.ui.theme.SoftGold
import com.example.ui.viewmodel.MainUiState

@Composable
fun CalendarPlanScreen(
    state: MainUiState,
    onSelectDate: (String) -> Unit,
    onOpenRoutineForDate: (String) -> Unit,
    onSetMonthYear: (Int, Int) -> Unit,
    onSetActiveYear: (Int) -> Unit,
    onSaveGoal: (id: String?, year: Int, month: Int, category: String, title: String, target: Int, current: Int, unit: String, note: String) -> Unit,
    onAdjustGoal: (YearlyGoalEntity, Int) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    val lang = state.prefs.language
    var modeTab by remember { mutableIntStateOf(0) } // 0=Calendar, 1=30-Day Plan, 2=365-Day Plan
    var calSubView by remember { mutableIntStateOf(2) } // 0=Daily, 1=Weekly, 2=Monthly, 3=Yearly
    var showGoalDialog by remember { mutableStateOf(false) }

    val (mYear, mMonth) = state.activeMonthYear
    val aYear = state.activeYear

    val mainTabs = listOf(
        AppStrings.t(lang, "ক্যালেন্ডার", "Calendar"),
        AppStrings.t(lang, "৩০ দিন", "30-Day Plan"),
        AppStrings.t(lang, "৩৬৫ দিন", "365-Day Plan")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                mainTabs.forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = modeTab == idx,
                        onClick = { modeTab = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = mainTabs.size),
                        modifier = Modifier.testTag("plan_tab_$idx")
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }
        }

        when (modeTab) {
            // TAB 0: Bangla + English + Hijri Calendar (Daily, Weekly, Monthly, Yearly)
            0 -> {
                item {
                    val subLabels = listOf(
                        AppStrings.t(lang, "দৈনিক", "Daily"),
                        AppStrings.t(lang, "সাপ্তাহিক", "Weekly"),
                        AppStrings.t(lang, "মাসিক", "Monthly"),
                        AppStrings.t(lang, "বার্ষিক", "Yearly")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subLabels.forEachIndexed { idx, lbl ->
                            FilterChip(
                                selected = calSubView == idx,
                                onClick = { calSubView = idx },
                                label = { Text(lbl) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Selected Day Triple Card
                item {
                    val d = state.selectedDateTriple
                    if (d != null) {
                        SectionCard(
                            title = AppStrings.t(lang, "বাংলা • ইংরেজি • হিজরি তারিখ", "Bangla • English • Hijri Date"),
                            icon = Icons.Default.CalendarMonth,
                            trailing = {
                                TextButton(onClick = { onOpenRoutineForDate(d.isoDate) }) {
                                    Text(AppStrings.t(lang, "রুটিন খুলুন", "Open Routine"))
                                }
                            }
                        ) {
                            Text(
                                text = "${if (lang == "en") d.weekdayEn else d.weekdayBn}, ${d.gregorianDisplay}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${AppStrings.t(lang, "বাংলা:", "Bangla:")} ${d.banglaDisplay}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${AppStrings.t(lang, "হিজরি:", "Hijri:")} ${d.hijriDisplay}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (d.specialOccasion != null) {
                                Spacer(Modifier.height(6.dp))
                                AssistChip(
                                    onClick = { onOpenRoutineForDate(d.isoDate) },
                                    label = { Text(d.specialOccasion) },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }

                when (calSubView) {
                    // Daily view: tasks & deadlines on selectedDate
                    0 -> {
                        item {
                            SectionCard(
                                title = AppStrings.t(lang, "দিনের কাজ ও ডেডলাইন", "Tasks & Deadlines"),
                                icon = Icons.Default.TaskAlt
                            ) {
                                if (state.resolvedTasksForSelectedDate.isEmpty()) {
                                    Text(
                                        AppStrings.t(lang, "এই দিনে কোনো কাজ নেই।", "No tasks on this day."),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    state.resolvedTasksForSelectedDate.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenRoutineForDate(state.selectedDate) }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.task.title, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = if (item.isCompleted) "✔" else AppStrings.digits(item.task.startTime.ifBlank { "—" }, lang),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Weekly view: 7-day strip starting Sunday of selected week
                    1 -> {
                        item {
                            val wd = CalendarEngine.dayOfWeek(state.selectedDate)
                            val weekStart = CalendarEngine.addDays(state.selectedDate, -wd)
                            SectionCard(
                                title = AppStrings.t(lang, "সাপ্তাহিক ভিউ", "Weekly View"),
                                icon = Icons.Default.ViewWeek
                            ) {
                                (0..6).forEach { offset ->
                                    val dayIso = CalendarEngine.addDays(weekStart, offset)
                                    val info = CalendarEngine.getTripleDate(dayIso, state.prefs.hijriOffset, lang)
                                    val selected = dayIso == state.selectedDate
                                    Surface(
                                        onClick = { onSelectDate(dayIso) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${if (lang == "en") info.weekdayEn else info.weekdayBn} • ${info.gregorianDisplay}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${info.banglaDisplay} • ${info.hijriDisplay}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (info.specialOccasion != null) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = SoftGold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Monthly view: Interactive Calendar Grid
                    2 -> {
                        item {
                            MonthlyCalendarCard(
                                year = mYear,
                                month = mMonth,
                                selectedDate = state.selectedDate,
                                todayIso = state.todayIso,
                                lang = lang,
                                hijriOffset = state.prefs.hijriOffset,
                                monthSummaries = state.monthSummariesMap,
                                onPrevMonth = { onSetMonthYear(mYear, mMonth - 1) },
                                onNextMonth = { onSetMonthYear(mYear, mMonth + 1) },
                                onTapDate = { d -> onSelectDate(d) }
                            )
                        }
                    }

                    // Yearly view: 12 Month Cards with Leap Year indicator
                    3 -> {
                        item {
                            val isLeap = CalendarEngine.isLeapYear(aYear)
                            SectionCard(
                                title = AppStrings.t(
                                    lang,
                                    "${AppStrings.digits(aYear, lang)} (${if (isLeap) "অধিবর্ষ • ৩৬৬ দিন" else "৩৬৫ দিন"})",
                                    "$aYear (${if (isLeap) "Leap Year • 366 Days" else "365 Days"})"
                                ),
                                icon = Icons.Default.CalendarToday,
                                trailing = {
                                    Row {
                                        IconButton(onClick = { onSetActiveYear(aYear - 1) }) {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                                        }
                                        IconButton(onClick = { onSetActiveYear(aYear + 1) }) {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                                        }
                                    }
                                }
                            ) {
                                (1..12).chunked(3).forEach { rowMonths ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowMonths.forEach { m ->
                                            val dates = CalendarEngine.getMonthDates(aYear, m)
                                            val past = dates.filter { it <= state.todayIso }
                                            val avg = if (past.isEmpty()) 0 else past.mapNotNull { state.yearSummariesMap[it]?.overallPercent }.average().toInt()
                                            Surface(
                                                onClick = {
                                                    onSetMonthYear(aYear, m)
                                                    calSubView = 2
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = CalendarEngine.gregMonthName(m, lang),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${AppStrings.digits(dates.size, lang)} ${AppStrings.t(lang, "দিন", "d")} • ${AppStrings.digits(avg, lang)}%",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: 30-Day Monthly Plan & Unfinished Tasks
            1 -> {
                item {
                    MonthlyCalendarCard(
                        year = mYear,
                        month = mMonth,
                        selectedDate = state.selectedDate,
                        todayIso = state.todayIso,
                        lang = lang,
                        hijriOffset = state.prefs.hijriOffset,
                        monthSummaries = state.monthSummariesMap,
                        onPrevMonth = { onSetMonthYear(mYear, mMonth - 1) },
                        onNextMonth = { onSetMonthYear(mYear, mMonth + 1) },
                        onTapDate = { d -> onOpenRoutineForDate(d) }
                    )
                }

                item {
                    val monthDates = CalendarEngine.getMonthDates(mYear, mMonth)
                    val pastDates = monthDates.filter { it <= state.todayIso }
                    val bestDay = pastDates.maxByOrNull { state.monthSummariesMap[it]?.overallPercent ?: 0 }
                    val bestPct = bestDay?.let { state.monthSummariesMap[it]?.overallPercent ?: 0 } ?: 0
                    val totalPrayers = pastDates.sumOf { state.monthSummariesMap[it]?.prayersCompleted ?: 0 }
                    val totalQuran = pastDates.sumOf { state.monthSummariesMap[it]?.quranPages ?: 0 }

                    SectionCard(
                        title = AppStrings.t(lang, "মাসিক আমল ও কাজের রিপোর্ট", "Monthly Amal & Task Report"),
                        icon = Icons.Default.Assessment
                    ) {
                        Text(
                            text = AppStrings.t(
                                lang,
                                "মাসিক গড় অগ্রগতি: ${AppStrings.digits(state.monthlyAvgPercent, lang)}%",
                                "Monthly Average Progress: ${state.monthlyAvgPercent}%"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(6.dp))
                        AnimatedEmeraldProgress(progressFraction = state.monthlyAvgPercent / 100f)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = AppStrings.t(
                                lang,
                                "মোট আদায়কৃত নামাজ: ${AppStrings.digits(totalPrayers, lang)} ওয়াক্ত • কুরআন: ${AppStrings.digits(totalQuran, lang)} পৃষ্ঠা",
                                "Prayers Logged: $totalPrayers • Quran: $totalQuran pages"
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (bestDay != null) {
                            Text(
                                text = AppStrings.t(
                                    lang,
                                    "সেরা দিন: $bestDay (${AppStrings.digits(bestPct, lang)}%)",
                                    "Best Day: $bestDay ($bestPct%)"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    val unfinished = state.resolvedTasksForSelectedDate.filter { !it.isCompleted }
                    SectionCard(
                        title = AppStrings.t(lang, "অসম্পূর্ণ কাজের তালিকা (${AppStrings.digits(unfinished.size, lang)})", "Unfinished Tasks (${unfinished.size})"),
                        icon = Icons.Default.PendingActions
                    ) {
                        if (unfinished.isEmpty()) {
                            Text(
                                AppStrings.t(lang, "নির্বাচিত দিনে কোনো অসম্পূর্ণ কাজ বাকি নেই।", "No unfinished tasks on selected date."),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            unfinished.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenRoutineForDate(state.selectedDate) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.task.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(AppStrings.digits(item.task.startTime.ifBlank { "—" }, lang), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: 365-Day Yearly Plan + Leap Year + Yearly Goals
            2 -> {
                val isLeap = CalendarEngine.isLeapYear(aYear)
                val totalDays = CalendarEngine.daysInYear(aYear)
                item {
                    SectionCard(
                        title = AppStrings.t(
                            lang,
                            "${AppStrings.digits(aYear, lang)} — ${AppStrings.digits(totalDays, lang)} দিনের বার্ষিক পরিকল্পনা",
                            "$aYear — $totalDays-Day Yearly Plan"
                        ),
                        icon = Icons.Default.Timeline,
                        trailing = {
                            Row {
                                IconButton(onClick = { onSetActiveYear(aYear - 1) }) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                                }
                                IconButton(onClick = { onSetActiveYear(aYear + 1) }) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                                }
                            }
                        }
                    ) {
                        Text(
                            text = AppStrings.t(
                                lang,
                                "${if (isLeap) "অধিবর্ষ (৩৬৬ দিন সমর্থিত) • " else ""}বার্ষিক গড় অগ্রগতি: ${AppStrings.digits(state.yearlyAvgPercent, lang)}%",
                                "${if (isLeap) "Leap Year (366 days) • " else ""}Yearly Average: ${state.yearlyAvgPercent}%"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(10.dp))
                        val monthlyBars = (1..12).map { m ->
                            val dates = CalendarEngine.getMonthDates(aYear, m).filter { it <= state.todayIso }
                            val avg = if (dates.isEmpty()) 0f else dates.mapNotNull { state.yearSummariesMap[it]?.overallPercent }.average().toFloat()
                            BarDatum(
                                label = CalendarEngine.gregMonthName(m, lang).take(3),
                                value = avg,
                                displayValue = "${AppStrings.digits(avg.toInt(), lang)}"
                            )
                        }
                        SimpleBarChart(data = monthlyBars)
                    }
                }

                // 365/366-Day Activity Heatmap
                item {
                    val yearDates = CalendarEngine.getYearDates(aYear)
                    SectionCard(
                        title = AppStrings.t(lang, "৩৬৫ দিনের হিটম্যাপ (${AppStrings.digits(yearDates.size, lang)} দিন)", "Year Heatmap (${yearDates.size} Days)"),
                        icon = Icons.Default.GridView
                    ) {
                        val rows = yearDates.chunked(26)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            rows.forEach { rowDates ->
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    rowDates.forEach { d ->
                                        val pct = state.yearSummariesMap[d]?.overallPercent ?: 0
                                        val future = d > state.todayIso
                                        val color = when {
                                            future -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            pct >= 80 -> MaterialTheme.colorScheme.primary
                                            pct >= 50 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                                            pct > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(color)
                                                .clickable { onOpenRoutineForDate(d) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Yearly Goals: Quran Khatam, Books, Education, Career, Family
                item {
                    val yearGoals = state.goals.filter { it.year == aYear }
                    SectionCard(
                        title = AppStrings.t(lang, "বার্ষিক লক্ষ্য (কুরআন খতম, বই, শিক্ষা, পরিবার)", "Annual Goals"),
                        icon = Icons.Default.Flag,
                        trailing = {
                            Button(
                                onClick = { showGoalDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_goal_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(AppStrings.t(lang, "লক্ষ্য", "Goal"))
                            }
                        }
                    ) {
                        if (yearGoals.isEmpty()) {
                            EmptyStateCard(
                                icon = Icons.Default.Flag,
                                message = AppStrings.t(
                                    lang,
                                    "কুরআন খতম, ইসলামী বই পড়া, শিক্ষা বা পারিবারিক লক্ষ্য যোগ করুন।",
                                    "Add Quran Khatam, reading, education, career, or family goals for $aYear."
                                ),
                                actionLabel = AppStrings.t(lang, "+ নতুন লক্ষ্য", "+ Add Goal"),
                                onAction = { showGoalDialog = true }
                            )
                        } else {
                            yearGoals.forEach { g ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(g.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "${AppStrings.digits(g.currentValue, lang)}/${AppStrings.digits(g.targetValue, lang)} ${g.unit}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                OutlinedButton(
                                                    onClick = { onAdjustGoal(g, -1) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) { Text("−১") }
                                                Button(
                                                    onClick = { onAdjustGoal(g, 1) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) { Text("+১") }
                                                IconButton(onClick = { onDeleteGoal(g.id) }) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        AnimatedEmeraldProgress(progressFraction = g.currentValue.toFloat() / g.targetValue.coerceAtLeast(1))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        AddYearlyGoalDialog(
            year = aYear,
            lang = lang,
            onDismiss = { showGoalDialog = false },
            onSave = { cat, title, target, unit, note ->
                onSaveGoal(null, aYear, 0, cat, title, target, 0, unit, note)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun MonthlyCalendarCard(
    year: Int,
    month: Int,
    selectedDate: String,
    todayIso: String,
    lang: String,
    hijriOffset: Int,
    monthSummaries: Map<String, com.example.ui.viewmodel.DayProgressSummary>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTapDate: (String) -> Unit
) {
    val monthDates = CalendarEngine.getMonthDates(year, month)
    val firstWd = CalendarEngine.dayOfWeek(monthDates.first())
    val cells = List(firstWd) { null } + monthDates

    SectionCard(
        title = "${CalendarEngine.gregMonthName(month, lang)} ${AppStrings.digits(year, lang)}",
        icon = Icons.Default.CalendarMonth,
        trailing = {
            Row {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            (0..6).forEach { wd ->
                Text(
                    text = CalendarEngine.weekdayShort(wd, lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until 7) {
                    val iso = week.getOrNull(i)
                    if (iso == null) {
                        Spacer(modifier = Modifier.weight(1f).height(46.dp))
                    } else {
                        val dayNum = iso.takeLast(2).toIntOrNull() ?: 1
                        val pct = monthSummaries[iso]?.overallPercent ?: 0
                        val isFuture = iso > todayIso
                        val isToday = iso == todayIso
                        val isSelected = iso == selectedDate
                        val hInfo = CalendarEngine.getTripleDate(iso, hijriOffset, lang)

                        val bgColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            !isFuture && pct > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = (0.1f + pct / 220f).coerceAtMost(0.5f))
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .then(
                                    if (isToday) Modifier.border(1.5.dp, SoftGold, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { onTapDate(iso) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = AppStrings.digits(dayNum, lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!isFuture && pct > 0) "${AppStrings.digits(pct, lang)}%"
                                    else AppStrings.digits(hInfo.hijriDay, lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (!isFuture && pct > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddYearlyGoalDialog(
    year: Int,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (category: String, title: String, target: Int, unit: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("QURAN_KHATAM") }
    var target by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(AppStrings.t(lang, "খতম / বার", "times")) }
    var note by remember { mutableStateOf("") }

    val cats = listOf(
        "QURAN_KHATAM" to AppStrings.t(lang, "কুরআন খতম", "Quran Khatam"),
        "BOOKS" to AppStrings.t(lang, "বই পড়া", "Books"),
        "EDUCATION" to AppStrings.t(lang, "শিক্ষা", "Education"),
        "CAREER" to AppStrings.t(lang, "কর্মজীবন", "Career"),
        "FAMILY" to AppStrings.t(lang, "পারিবারিক", "Family"),
        "FINANCE" to AppStrings.t(lang, "সঞ্চয়", "Savings")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.t(lang, "বার্ষিক লক্ষ্য ($year)", "Yearly Goal ($year)")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cats.forEach { (k, lbl) ->
                        FilterChip(selected = category == k, onClick = { category = k }, label = { Text(lbl) })
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.t(lang, "লক্ষ্যের নাম (যেমন: কুরআন খতম)", "Goal Title")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { if (it.all(Char::isDigit)) target = it },
                        label = { Text(AppStrings.t(lang, "লক্ষ্যমাত্রা", "Target")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(AppStrings.t(lang, "একক", "Unit")) },
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
                if (title.isNotBlank()) onSave(category, title, target.toIntOrNull() ?: 1, unit, note)
            }) { Text(AppStrings.t(lang, "সংরক্ষণ", "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.t(lang, "বাতিল", "Cancel")) }
        }
    )
}
