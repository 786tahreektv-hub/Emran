package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.AppStrings
import com.example.core.PrayerTimeCalculator
import com.example.data.prefs.UserPreferences
import com.example.ui.components.IslamicHeroCard
import com.example.ui.components.SectionCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGoldBright
import com.example.ui.viewmodel.MainUiState

@Composable
fun ProfileSettingsScreen(
    state: MainUiState,
    onUpdatePrefs: ((UserPreferences) -> UserPreferences) -> Unit,
    onSetPin: (String?) -> Unit,
    onExportJson: (Uri) -> Unit,
    onImportJson: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onExportPdf: (Uri) -> Unit,
    onResetAllData: () -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    val prefs = state.prefs
    val lang = prefs.language

    var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }
    var quranTarget by remember(prefs.dailyQuranTargetPages) { mutableStateOf(prefs.dailyQuranTargetPages.toString()) }
    var waterTarget by remember(prefs.dailyWaterTargetGlasses) { mutableStateOf(prefs.dailyWaterTargetGlasses.toString()) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Zero-permission Photo Picker for profile photo
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdatePrefs { it.copy(profilePhotoUri = uri.toString()) }
        }
    }

    // Storage Access Framework (zero storage permissions!)
    val jsonExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onExportJson) }

    val jsonImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onImportJson) }

    val csvExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(onExportCsv) }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> uri?.let(onExportPdf) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Banner with Photo Picker
        item {
            IslamicHeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SoftGoldBright.copy(alpha = 0.25f))
                            .border(2.dp, SoftGoldBright, CircleShape)
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (prefs.profilePhotoUri.isNotBlank()) {
                            AsyncImage(
                                model = prefs.profilePhotoUri,
                                contentDescription = AppStrings.t(lang, "প্রোফাইল ছবি", "Profile Photo"),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = AppStrings.t(lang, "ছবি যোগ করুন", "Add Photo"),
                                tint = PureWhite,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prefs.userName.ifBlank { AppStrings.t(lang, "আপনার প্রোফাইল", "Your Profile") },
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${prefs.cityName} • ${AppStrings.t(lang, "ছবি পরিবর্তন করতে ট্যাপ করুন", "Tap photo to change")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Personal Name & Daily Goals
        item {
            SectionCard(
                title = AppStrings.t(lang, "ব্যক্তিগত তথ্য ও লক্ষ্য", "Personal Profile & Targets"),
                icon = Icons.Default.Person
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(AppStrings.t(lang, "নাম", "Name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quranTarget,
                        onValueChange = { if (it.all(Char::isDigit)) quranTarget = it },
                        label = { Text(AppStrings.t(lang, "কুরআন (পৃষ্ঠা)", "Quran Pages")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = waterTarget,
                        onValueChange = { if (it.all(Char::isDigit)) waterTarget = it },
                        label = { Text(AppStrings.t(lang, "পানি (গ্লাস)", "Water Glasses")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onUpdatePrefs {
                            it.copy(
                                userName = nameInput.trim(),
                                dailyQuranTargetPages = quranTarget.toIntOrNull()?.coerceAtLeast(1) ?: 5,
                                dailyWaterTargetGlasses = waterTarget.toIntOrNull()?.coerceAtLeast(1) ?: 8
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(AppStrings.t(lang, "তথ্য সংরক্ষণ করুন", "Save Profile"))
                }
            }
        }

        // Language & Theme
        item {
            SectionCard(
                title = AppStrings.t(lang, "ভাষা ও থিম", "Language & Appearance"),
                icon = Icons.Default.Palette
            ) {
                Text(AppStrings.t(lang, "ভাষা নির্বাচন করুন:", "App Language:"), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = lang == "bn",
                        onClick = { onUpdatePrefs { it.copy(language = "bn") } },
                        label = { Text("বাংলা") },
                        modifier = Modifier.testTag("lang_bn_chip")
                    )
                    FilterChip(
                        selected = lang == "en",
                        onClick = { onUpdatePrefs { it.copy(language = "en") } },
                        label = { Text("English") },
                        modifier = Modifier.testTag("lang_en_chip")
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(AppStrings.t(lang, "থিম মোড:", "Theme Mode:"), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        "SYSTEM" to AppStrings.t(lang, "সিস্টেম", "System"),
                        "LIGHT" to AppStrings.t(lang, "লাইট", "Light"),
                        "DARK" to AppStrings.t(lang, "ডার্ক", "Dark")
                    )
                    modes.forEach { (k, lbl) ->
                        FilterChip(
                            selected = prefs.themeMode == k,
                            onClick = { onUpdatePrefs { it.copy(themeMode = k) } },
                            label = { Text(lbl) },
                            modifier = Modifier.testTag("theme_${k.lowercase()}_chip")
                        )
                    }
                }
            }
        }

        // Prayer Calculation Method, Location & Hijri Offset
        item {
            SectionCard(
                title = AppStrings.t(lang, "নামাজের হিসাব ও অবস্থান", "Prayer Calculation & Location"),
                icon = Icons.Default.Mosque
            ) {
                Text(AppStrings.t(lang, "শহর / সময় অঞ্চল:", "City / Timezone:"), style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PrayerTimeCalculator.cities.forEach { city ->
                        val label = if (lang == "en") city.nameEn else city.nameBn
                        FilterChip(
                            selected = prefs.cityName == city.nameBn || prefs.cityName == city.nameEn,
                            onClick = {
                                onUpdatePrefs {
                                    it.copy(
                                        cityName = label,
                                        latitude = city.lat,
                                        longitude = city.lon,
                                        timezoneId = city.tzId
                                    )
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(AppStrings.t(lang, "হিসাবের পদ্ধতি:", "Calculation Method:"), style = MaterialTheme.typography.labelMedium)
                val methods = listOf("KARACHI", "MWL", "ISNA", "UMM_AL_QURA", "CUSTOM")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = prefs.prayerCalcMethod == m,
                            onClick = { onUpdatePrefs { it.copy(prayerCalcMethod = m) } },
                            label = { Text(PrayerTimeCalculator.methodLabel(m, lang)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(AppStrings.t(lang, "আসর মাজহাব:", "Asr Juristic:"))
                    FilterChip(
                        selected = prefs.asrJuristic == "HANAFI",
                        onClick = { onUpdatePrefs { it.copy(asrJuristic = "HANAFI") } },
                        label = { Text(AppStrings.t(lang, "হানাফি", "Hanafi")) }
                    )
                    FilterChip(
                        selected = prefs.asrJuristic == "SHAFI",
                        onClick = { onUpdatePrefs { it.copy(asrJuristic = "SHAFI") } },
                        label = { Text(AppStrings.t(lang, "শাফেয়ী/অন্যান্য", "Standard")) }
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(AppStrings.t(lang, "হিজরি সমন্বয়:", "Hijri Offset:"))
                    OutlinedButton(onClick = { onUpdatePrefs { it.copy(hijriOffset = (it.hijriOffset - 1).coerceIn(-2, 2)) } }) { Text("−১") }
                    Text("${AppStrings.digits(prefs.hijriOffset, lang)} দিন")
                    OutlinedButton(onClick = { onUpdatePrefs { it.copy(hijriOffset = (it.hijriOffset + 1).coerceIn(-2, 2)) } }) { Text("+১") }
                }
            }
        }

        // Security, PIN Lock, Backup/Restore, CSV & PDF Export
        item {
            SectionCard(
                title = AppStrings.t(lang, "নিরাপত্তা, ব্যাকআপ ও এক্সপোর্ট", "Security, Backup & Export"),
                icon = Icons.Default.Security
            ) {
                Button(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (prefs.pinEnabled) AppStrings.t(lang, "অ্যাপ PIN লক পরিবর্তন/বন্ধ করুন", "Manage App PIN Lock")
                        else AppStrings.t(lang, "অ্যাপ PIN লক চালু করুন", "Enable App PIN Lock")
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { jsonExportLauncher.launch("amar-jibon-backup-${state.todayIso}.json") },
                        modifier = Modifier.weight(1f).testTag("backup_json_btn")
                    ) {
                        Text(AppStrings.t(lang, "JSON ব্যাকআপ", "JSON Backup"))
                    }
                    OutlinedButton(
                        onClick = { jsonImportLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f).testTag("restore_json_btn")
                    ) {
                        Text(AppStrings.t(lang, "রিস্টোর", "Restore"))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { csvExportLauncher.launch("amar-jibon-report-${state.todayIso}.csv") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(AppStrings.t(lang, "CSV এক্সপোর্ট", "CSV Export"))
                    }
                    FilledTonalButton(
                        onClick = { pdfExportLauncher.launch("amar-jibon-report-${state.todayIso}.pdf") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(AppStrings.t(lang, "PDF রিপোর্ট", "PDF Report"))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onNavigateRoute("reminders") }, modifier = Modifier.weight(1f)) {
                        Text(AppStrings.t(lang, "রিমাইন্ডার সেটিংস", "Reminder Settings"))
                    }
                    TextButton(onClick = { showGuideDialog = true }, modifier = Modifier.weight(1f)) {
                        Text(AppStrings.t(lang, "ব্যাকআপ নির্দেশিকা", "Backup Guide"))
                    }
                }
                TextButton(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(AppStrings.t(lang, "সব ডেটা রিসেট করুন", "Reset All Data"))
                }
            }
        }
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(AppStrings.t(lang, "অ্যাপ PIN লক", "App PIN Lock")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) newPin = it },
                        label = { Text(AppStrings.t(lang, "নতুন ৪-সংখ্যার PIN", "New 4-digit PIN")) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (prefs.pinEnabled) {
                        TextButton(onClick = {
                            onSetPin(null)
                            showPinDialog = false
                        }) {
                            Text(AppStrings.t(lang, "PIN লক বন্ধ করুন", "Disable PIN Lock"))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPin.length >= 4) {
                        onSetPin(newPin)
                        showPinDialog = false
                    }
                }) { Text(AppStrings.t(lang, "সংরক্ষণ", "Save")) }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text(AppStrings.t(lang, "বাতিল", "Cancel")) }
            }
        )
    }

    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = { Text(AppStrings.t(lang, "ব্যাকআপ ও ব্যবহার নির্দেশিকা", "Backup & Restore Guide")) },
            text = {
                Text(
                    AppStrings.t(
                        lang,
                        "১. আপনার সব ডেটা সম্পূর্ণ অফলাইনে ফোনের এনক্রিপ্টেড ও নিরাপদ Room ডাটাবেসে থাকে — অনুমতি ছাড়া কোনো সার্ভারে পাঠানো হয় না।\n\n২. নিয়মিত 'JSON ব্যাকআপ' চেপে Google Drive বা Downloads ফোল্ডারে ব্যাকআপ ফাইল সংরক্ষণ করুন।\n\n৩. নতুন ফোনে বা অ্যাপ রি-ইনস্টলের পর 'রিস্টোর' চেপে সেই JSON ফাইলটি নির্বাচন করলেই সব রুটিন, আমল, কুরআন, হিসাব ও ডায়েরি ফিরে আসবে।",
                        "1. All data stays 100% offline on your device.\n2. Use JSON Backup regularly to save a copy to Downloads or Drive.\n3. Use Restore to import your JSON backup on any device."
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showGuideDialog = false }) { Text(AppStrings.t(lang, "বুঝেছি", "Got it")) }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(AppStrings.t(lang, "সব ডেটা মুছবেন?", "Reset All Data?")) },
            text = {
                Text(AppStrings.t(lang, "সব রুটিন, আমল, হিসাব ও ডায়েরি স্থায়ীভাবে মুছে যাবে। আগে JSON ব্যাকআপ নিয়ে রাখুন।", "All tasks, amals, finance, and diary records will be deleted."))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(AppStrings.t(lang, "মুছে ফেলুন", "Delete All")) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text(AppStrings.t(lang, "বাতিল", "Cancel")) }
            }
        )
    }
}
