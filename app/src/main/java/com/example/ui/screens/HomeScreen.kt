package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.ui.components.*
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGoldBright
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ResolvedTaskItem

@Composable
fun HomeScreen(
    state: MainUiState,
    onToggleTodayTask: (ResolvedTaskItem) -> Unit,
    onAdjustWater: (Int) -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    val lang = state.prefs.language
    val d = state.todayTriple
    val summary = state.todaySummary
    val qPages = state.quranSelectedDate.pagesRead
    val water = state.healthToday.waterGlasses
    val schedule = state.todayPrayerSchedule
    val nextPrayer = state.nextPrayerCountdown

    val todayTasks = state.resolvedTasksForSelectedDate.take(5)
    val importantTasks = state.resolvedTasksForSelectedDate.filter { it.task.priority >= 2 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Header: Bangla, English, Hijri Date + Live Time & Ring
        item {
            IslamicHeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val greeting = if (state.prefs.userName.isNotBlank()) {
                            AppStrings.t(lang, "আসসালামু আলাইকুম, ${state.prefs.userName}", "Assalamu Alaikum, ${state.prefs.userName}")
                        } else {
                            AppStrings.t(lang, "আসসালামু আলাইকুম", "Assalamu Alaikum")
                        }
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelLarge,
                            color = SoftGoldBright
                        )
                        Spacer(Modifier.height(4.dp))
                        if (d != null) {
                            Text(
                                text = "${if (lang == "en") d.weekdayEn else d.weekdayBn}, ${d.gregorianDisplay}",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${d.banglaDisplay}  |  ${d.hijriDisplay}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite.copy(alpha = 0.88f)
                            )
                            if (d.specialOccasion != null) {
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    color = SoftGoldBright.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = d.specialOccasion,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftGoldBright,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = SoftGoldBright, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${state.currentTimeDisplay} • ${state.prefs.cityName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite.copy(alpha = 0.92f)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    ProgressRing(
                        percent = summary.overallPercent,
                        label = "${AppStrings.digits(summary.overallPercent, lang)}%"
                    )
                }

                // Next Prayer Countdown Strip
                if (nextPrayer != null && schedule != null) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mosque, contentDescription = null, tint = SoftGoldBright, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = AppStrings.t(
                                            lang,
                                            "পরবর্তী নামাজ: ${AppStrings.prayerName(nextPrayer.prayerKey, lang)} (${AppStrings.digits(nextPrayer.prayerTime, lang)})",
                                            "Next Prayer: ${AppStrings.prayerName(nextPrayer.prayerKey, lang)} (${nextPrayer.prayerTime})"
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureWhite
                                    )
                                }
                                Text(
                                    text = nextPrayer.formattedRemaining,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftGoldBright,
                                    modifier = Modifier.testTag("next_prayer_countdown")
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val waqts = listOf(
                                    "fajr" to schedule.fajr,
                                    "zuhr" to schedule.zuhr,
                                    "asr" to schedule.asr,
                                    "maghrib" to schedule.maghrib,
                                    "isha" to schedule.isha
                                )
                                waqts.forEach { (key, tm) ->
                                    val isNext = key == nextPrayer.prayerKey
                                    Surface(
                                        color = if (isNext) SoftGoldBright.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.clickable { onNavigateRoute("amal") }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = AppStrings.prayerName(key, lang),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isNext) SoftGoldBright else PureWhite.copy(alpha = 0.85f)
                                            )
                                            Text(
                                                text = AppStrings.digits(tm, lang),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
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

        // 2. KPI Summary Grid: Tasks, Pending, Amal, Quran, Water, Income/Expense
        item {
            val pending = (summary.tasksTotal - summary.tasksCompleted).coerceAtLeast(0)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        value = "${AppStrings.digits(summary.tasksCompleted, lang)}/${AppStrings.digits(summary.tasksTotal, lang)}",
                        label = AppStrings.t(lang, "আজকের মোট ও সম্পন্ন কাজ", "Today's Tasks Done"),
                        icon = Icons.Default.TaskAlt,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("routine") }
                    )
                    StatTile(
                        value = AppStrings.digits(pending, lang),
                        label = AppStrings.t(lang, "অসম্পন্ন কাজ বাকি", "Remaining Tasks"),
                        icon = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("routine") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        value = "${AppStrings.digits(summary.amalsCompleted, lang)}/${AppStrings.digits(summary.amalsTotal, lang)}",
                        label = AppStrings.t(lang, "দৈনিক আমল অগ্রগতি", "Daily Amal Progress"),
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("amal") }
                    )
                    StatTile(
                        value = "${AppStrings.digits(qPages, lang)}/${AppStrings.digits(state.prefs.dailyQuranTargetPages, lang)}",
                        label = AppStrings.t(lang, "কুরআন তিলাওয়াত (পৃষ্ঠা)", "Quran Tilawat (Pages)"),
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("amal") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Quick Water & Health Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateRoute("daily_life") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "${AppStrings.digits(water, lang)}/${AppStrings.digits(state.prefs.dailyWaterTargetGlasses, lang)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = AppStrings.t(lang, "পানি পান (গ্লাস)", "Water Tracker"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalIconButton(
                                    onClick = { onAdjustWater(-1) },
                                    modifier = Modifier.size(36.dp).testTag("water_minus_btn")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease water", modifier = Modifier.size(18.dp))
                                }
                                FilledIconButton(
                                    onClick = { onAdjustWater(1) },
                                    modifier = Modifier.size(36.dp).testTag("water_plus_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add water", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Today's Income & Expense Card
                    StatTile(
                        value = "৳${AppStrings.digits(state.todayIncome.toInt(), lang)} / ৳${AppStrings.digits(state.todayExpense.toInt(), lang)}",
                        label = AppStrings.t(lang, "আজকের আয় / ব্যয়", "Today's Income / Expense"),
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("finance") }
                    )
                }
            }
        }

        // 3. Today's Important Tasks
        item {
            SectionCard(
                title = AppStrings.t(lang, "আজকের গুরুত্বপূর্ণ কাজ", "Today's Important Tasks"),
                icon = Icons.Default.PriorityHigh,
                trailing = {
                    TextButton(onClick = { onNavigateRoute("routine") }) {
                        Text(AppStrings.t(lang, "সব দেখুন", "View All"))
                    }
                }
            ) {
                val listToShow = if (importantTasks.isNotEmpty()) importantTasks else todayTasks
                if (listToShow.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.EventNote,
                        message = AppStrings.t(
                            lang,
                            "আজকের জন্য এখনো কোনো কাজ যোগ করা হয়নি।",
                            "No tasks scheduled for today yet."
                        ),
                        actionLabel = AppStrings.t(lang, "রুটিনে কাজ যোগ করুন", "Add Task in Routine"),
                        onAction = { onNavigateRoute("routine") }
                    )
                } else {
                    listToShow.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTodayTask(item) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { onToggleTodayTask(item) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${AppStrings.digits(item.task.startTime.ifBlank { "--:--" }, lang)} • ${AppStrings.priorityLabel(item.task.priority, lang)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Tomorrow's Preparation & 7-day Progress
        item {
            SectionCard(
                title = AppStrings.t(lang, "আগামীকালের প্রস্তুতি", "Tomorrow's Preparation"),
                icon = Icons.Default.Upcoming
            ) {
                if (state.tomorrowImportantTasks.isEmpty()) {
                    Text(
                        text = AppStrings.t(
                            lang,
                            "আগামীকালের জন্য গুরুত্বপূর্ণ বা জরুরি কাজ চিহ্নিত করুন — এখানে প্রদর্শিত হবে।",
                            "Mark important or urgent tasks in Routine to see tomorrow's priority prep here."
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.tomorrowImportantTasks.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${t.title} (${AppStrings.digits(t.startTime.ifBlank { "--:--" }, lang)})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionCard(
                title = AppStrings.t(lang, "গত ৭ দিনের অগ্রগতি (%)", "Last 7 Days Progress (%)"),
                icon = Icons.Default.BarChart,
                trailing = {
                    TextButton(onClick = { onNavigateRoute("reports") }) {
                        Text(AppStrings.t(lang, "বিস্তারিত", "Reports"))
                    }
                }
            ) {
                val bars = state.last7DaysSummaries.map { s ->
                    val dayNum = s.date.takeLast(2).toIntOrNull() ?: 1
                    BarDatum(
                        label = AppStrings.digits(dayNum, lang),
                        value = s.overallPercent.toFloat(),
                        displayValue = "${AppStrings.digits(s.overallPercent, lang)}%"
                    )
                }
                SimpleBarChart(data = bars)
            }
        }

        // 5. Quick Access Module Hub (More Modules)
        item {
            SectionCard(
                title = AppStrings.t(lang, "জীবন ব্যবস্থাপনা মডিউলসমূহ", "Life Management Modules"),
                icon = Icons.Default.Apps
            ) {
                val modules = listOf(
                    Triple("daily_life", AppStrings.t(lang, "দৈনন্দিন ও স্বাস্থ্য", "Daily Life & Health"), Icons.Default.SelfImprovement),
                    Triple("family", AppStrings.t(lang, "পরিবার ও সমাজ", "Family & Social"), Icons.Default.FamilyRestroom),
                    Triple("madrasa_work", AppStrings.t(lang, "পেশা ও মাদরাসা", "Madrasa & Work"), Icons.Default.School),
                    Triple("education", AppStrings.t(lang, "শিক্ষা ও দক্ষতা", "Education & Skills"), Icons.Default.MenuBook),
                    Triple("finance", AppStrings.t(lang, "আর্থিক ব্যবস্থাপনা", "Finance"), Icons.Default.Payments),
                    Triple("diary", AppStrings.t(lang, "নোট ও ডায়েরি", "Diary & Notes"), Icons.Default.EditNote),
                    Triple("reports", AppStrings.t(lang, "রিপোর্ট ও চার্ট", "Analytics & PDF"), Icons.Default.Analytics),
                    Triple("reminders", AppStrings.t(lang, "রিমাইন্ডার", "Reminders"), Icons.Default.NotificationsActive)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    modules.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { (route, label, icon) ->
                                ModuleQuickChip(
                                    label = label,
                                    icon = icon,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigateRoute(route) }
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
private fun ModuleQuickChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
