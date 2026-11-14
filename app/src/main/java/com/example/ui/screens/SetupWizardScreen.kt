package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.PrayerTimeCalculator
import com.example.data.repository.StarterCatalog
import com.example.ui.components.IslamicHeroCard
import com.example.ui.components.SectionCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGoldBright

@Composable
fun SetupWizardScreen(
    lang: String,
    onToggleLang: (String) -> Unit,
    onCompleteSetup: (
        userName: String,
        cityName: String,
        lat: Double,
        lon: Double,
        tzId: String,
        quranTarget: Int,
        waterTarget: Int,
        selectedTaskKeys: Set<String>,
        selectedAmalKeys: Set<String>
    ) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var selectedCityIdx by remember { mutableIntStateOf(0) }
    var quranTarget by remember { mutableStateOf("5") }
    var waterTarget by remember { mutableStateOf("8") }
    var selectedTasks by remember {
        mutableStateOf(StarterCatalog.defaultTasks.map { it.key }.toSet())
    }
    var selectedAmals by remember {
        mutableStateOf(StarterCatalog.defaultAmals.map { it.key }.toSet())
    }
    var cityDropdownOpen by remember { mutableStateOf(false) }

    val city = PrayerTimeCalculator.cities[selectedCityIdx]

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                IslamicHeroCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Mosque,
                                contentDescription = null,
                                tint = SoftGoldBright,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.t(lang, "বিসমিল্লাহির রাহমানির রাহিম", "Bismillahir Rahmanir Rahim"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SoftGoldBright
                                )
                                Text(
                                    text = AppStrings.t(lang, "আমার জীবন — সেটআপ", "My Life — Initial Setup"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = { onToggleLang(if (lang == "bn") "en" else "bn") }
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (lang == "bn") "EN" else "বাংলা")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = AppStrings.t(
                            lang,
                            "আপনার ২৪ ঘণ্টার রুটিন, নামাজের স্থান ও দৈনিক আমলের তালিকা বেছে নিন। কোনো কাজই নিজে থেকে সম্পন্ন দেখাবে না — আপনি সম্পন্ন করলেই হিসাব হবে।",
                            "Customize your 24-hour routine, prayer location, and daily amals. No item is auto-completed — progress updates only when you check it off."
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.9f)
                    )
                }
            }

            item {
                SectionCard(title = AppStrings.t(lang, "ব্যক্তিগত তথ্য ও লক্ষ্য", "Profile & Daily Targets")) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text(AppStrings.t(lang, "আপনার নাম", "Your Name")) },
                        placeholder = { Text(AppStrings.t(lang, "যেমন: মুহাম্মদ ইমরান", "e.g. Muhammad Imran")) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_name_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { cityDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                AppStrings.t(
                                    lang,
                                    "অবস্থান (নামাজের সময়): ${city.nameBn}",
                                    "Location (Prayer Times): ${city.nameEn}"
                                )
                            )
                        }
                        DropdownMenu(
                            expanded = cityDropdownOpen,
                            onDismissRequest = { cityDropdownOpen = false }
                        ) {
                            PrayerTimeCalculator.cities.forEachIndexed { idx, preset ->
                                DropdownMenuItem(
                                    text = { Text(if (lang == "en") preset.nameEn else preset.nameBn) },
                                    onClick = {
                                        selectedCityIdx = idx
                                        cityDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = quranTarget,
                            onValueChange = { if (it.all(Char::isDigit)) quranTarget = it },
                            label = { Text(AppStrings.t(lang, "কুরআন (পৃষ্ঠা/দিন)", "Quran (pages/day)")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = waterTarget,
                            onValueChange = { if (it.all(Char::isDigit)) waterTarget = it },
                            label = { Text(AppStrings.t(lang, "পানি (গ্লাস/দিন)", "Water (glasses/day)")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                SectionCard(
                    title = AppStrings.t(lang, "দৈনিক রুটিন টেমপ্লেট (${AppStrings.digits(selectedTasks.size, lang)})", "Daily Routine Templates (${selectedTasks.size})"),
                    trailing = {
                        TextButton(onClick = {
                            selectedTasks = if (selectedTasks.size == StarterCatalog.defaultTasks.size) {
                                emptySet()
                            } else {
                                StarterCatalog.defaultTasks.map { it.key }.toSet()
                            }
                        }) {
                            Text(AppStrings.t(lang, "সব/বাতিল", "Toggle All"))
                        }
                    }
                ) {
                    Text(
                        text = AppStrings.t(lang, "পরে যেকোনো সময় সম্পাদনা বা নতুন কাজ যোগ করতে পারবেন।", "You can edit or add tasks anytime later."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(StarterCatalog.defaultTasks, key = { it.key }) { tpl ->
                val checked = tpl.key in selectedTasks
                Card(
                    onClick = {
                        selectedTasks = if (checked) selectedTasks - tpl.key else selectedTasks + tpl.key
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                selectedTasks = if (it) selectedTasks + tpl.key else selectedTasks - tpl.key
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "en") tpl.titleEn else tpl.titleBn,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${AppStrings.digits(tpl.startTime, lang)} – ${AppStrings.digits(tpl.endTime, lang)} • ${AppStrings.categoryLabel(tpl.category, lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = AppStrings.t(lang, "দৈনিক আমল তালিকা (${AppStrings.digits(selectedAmals.size, lang)})", "Daily Amal List (${selectedAmals.size})"),
                    trailing = {
                        TextButton(onClick = {
                            selectedAmals = if (selectedAmals.size == StarterCatalog.defaultAmals.size) {
                                emptySet()
                            } else {
                                StarterCatalog.defaultAmals.map { it.key }.toSet()
                            }
                        }) {
                            Text(AppStrings.t(lang, "সব/বাতিল", "Toggle All"))
                        }
                    }
                ) {
                    StarterCatalog.defaultAmals.forEach { amal ->
                        val checked = amal.key in selectedAmals
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    selectedAmals = if (it) selectedAmals + amal.key else selectedAmals - amal.key
                                }
                            )
                            Text(
                                text = if (lang == "en") "${amal.nameEn} (Target ${amal.dailyTarget})"
                                else "${amal.nameBn} (লক্ষ্য ${AppStrings.digits(amal.dailyTarget, lang)} ${amal.unitBn})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onCompleteSetup(
                            userName,
                            if (lang == "en") city.nameEn else city.nameBn,
                            city.lat,
                            city.lon,
                            city.tzId,
                            quranTarget.toIntOrNull() ?: 5,
                            waterTarget.toIntOrNull() ?: 8,
                            selectedTasks,
                            selectedAmals
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("setup_start_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = AppStrings.t(lang, "বিসমিল্লাহ বলে শুরু করুন", "Start My Journey"),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
