package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.AppStrings
import com.example.ui.components.PinLockScreen
import com.example.ui.components.SplashScreenOverlay
import com.example.ui.screens.*
import com.example.ui.theme.AmarJibonTheme
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()

            AmarJibonTheme(themeMode = state.prefs.themeMode) {
                when {
                    state.showSplash -> SplashScreenOverlay()
                    !state.prefs.setupCompleted -> {
                        SetupWizardScreen(
                            lang = state.prefs.language,
                            onToggleLang = { nextLang -> vm.updatePreferences { it.copy(language = nextLang) } },
                            onCompleteSetup = { name, city, lat, lon, tz, qTarget, wTarget, tasks, amals ->
                                vm.completeSetup(name, city, lat, lon, tz, qTarget, wTarget, tasks, amals)
                            }
                        )
                    }
                    !state.isUnlocked -> {
                        PinLockScreen(
                            lang = state.prefs.language,
                            onVerifyPin = { pin -> vm.verifyPin(pin) }
                        )
                    }
                    else -> {
                        AmarJibonRootShell(state = state, vm = vm)
                    }
                }
            }
        }
    }
}

private data class NavTab(val route: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmarJibonRootShell(
    state: MainUiState,
    vm: MainViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lang = state.prefs.language
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"

    val bottomTabs = listOf(
        NavTab("home", Icons.Default.Home),
        NavTab("routine", Icons.Default.Schedule),
        NavTab("amal", Icons.Default.AutoAwesome),
        NavTab("calendar", Icons.Default.CalendarMonth),
        NavTab("profile", Icons.Default.Person)
    )
    val primaryRoutes = bottomTabs.map { it.route }.toSet()

    val drawerItems = listOf(
        "daily_life" to Icons.Default.SelfImprovement,
        "family" to Icons.Default.FamilyRestroom,
        "madrasa_work" to Icons.Default.School,
        "education" to Icons.Default.MenuBook,
        "finance" to Icons.Default.Payments,
        "diary" to Icons.Default.EditNote,
        "reports" to Icons.Default.Analytics,
        "reminders" to Icons.Default.NotificationsActive
    )

    LaunchedEffect(state.toastMessage) {
        val msg = state.toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            vm.clearToast()
        }
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> uri?.let(vm::exportPdf) }

    val csvExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(vm::exportCsv) }

    fun navigateTo(route: String) {
        if (route in primaryRoutes) {
            navController.navigate(route) {
                popUpTo("home") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.widthIn(max = 310.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text(
                            text = AppStrings.t(lang, "আমার জীবন", "My Life"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Islamic Life & Amal Manager",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    bottomTabs.forEach { tab ->
                        NavigationDrawerItem(
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(AppStrings.navTitle(tab.route, lang)) },
                            selected = currentRoute == tab.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateTo(tab.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = AppStrings.t(lang, "অন্যান্য বিভাগ", "More Modules"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                    drawerItems.forEach { (route, icon) ->
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(AppStrings.navTitle(route, lang)) },
                            selected = currentRoute == route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateTo(route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = AppStrings.navTitle(currentRoute, lang),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        if (currentRoute !in primaryRoutes) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = AppStrings.t(lang, "ফিরে যান", "Back")
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("drawer_menu_button")
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = AppStrings.t(lang, "মেনু", "Menu")
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { navigateTo("reminders") }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = AppStrings.t(lang, "রিমাইন্ডার", "Reminders")
                            )
                        }
                        IconButton(onClick = {
                            val nextTheme = if (state.prefs.themeMode == "DARK") "LIGHT" else "DARK"
                            vm.updatePreferences { it.copy(themeMode = nextTheme) }
                        }) {
                            Icon(
                                imageVector = if (state.prefs.themeMode == "DARK") Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = AppStrings.t(lang, "থিম বদলান", "Toggle Theme")
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        val title = AppStrings.navTitle(tab.route, lang)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTo(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = title) },
                            label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("nav_${tab.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = 720.dp).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                state = state,
                                onToggleTodayTask = vm::toggleTask,
                                onAdjustWater = vm::adjustWaterToday,
                                onNavigateRoute = ::navigateTo
                            )
                        }
                        composable("routine") {
                            RoutineScreen(
                                state = state,
                                onPrevDay = { vm.stepSelectedDate(-1) },
                                onNextDay = { vm.stepSelectedDate(1) },
                                onGoToday = { vm.selectDate(state.todayIso) },
                                onToggleTask = vm::toggleTask,
                                onCarryForwardTask = vm::carryTaskToTomorrow,
                                onCarryAllUnfinished = vm::carryAllUnfinishedToTomorrow,
                                onSkipTaskToday = vm::skipTaskForSelectedDate,
                                onRestoreSkippedTask = vm::restoreTaskForSelectedDate,
                                onDeleteTask = vm::deleteTask,
                                onSaveTask = vm::saveTask,
                                onSaveSpecialDay = vm::saveSpecialDay
                            )
                        }
                        composable("amal") {
                            AmalScreen(
                                state = state,
                                onPrevDay = { vm.stepSelectedDate(-1) },
                                onNextDay = { vm.stepSelectedDate(1) },
                                onGoToday = { vm.selectDate(state.todayIso) },
                                onIncrementAmal = vm::incrementAmal,
                                onUpdateAmalNote = vm::updateAmalDailyNote,
                                onSaveAmal = vm::saveAmalDefinition,
                                onDeleteAmal = vm::deleteAmalDefinition,
                                onCyclePrayer = vm::cyclePrayerStatus,
                                onUpdatePrayerExtras = vm::updatePrayerExtras,
                                onAdjustQaza = vm::adjustQaza,
                                onUpdateQuran = vm::updateQuranProgress
                            )
                        }
                        composable("calendar") {
                            CalendarPlanScreen(
                                state = state,
                                onSelectDate = vm::selectDate,
                                onOpenRoutineForDate = { d ->
                                    vm.selectDate(d)
                                    navigateTo("routine")
                                },
                                onSetMonthYear = vm::setMonthYear,
                                onSetActiveYear = vm::setActiveYear,
                                onSaveGoal = vm::saveGoal,
                                onAdjustGoal = vm::adjustGoalProgress,
                                onDeleteGoal = vm::deleteGoal
                            )
                        }
                        composable("profile") {
                            ProfileSettingsScreen(
                                state = state,
                                onUpdatePrefs = vm::updatePreferences,
                                onSetPin = vm::setPin,
                                onExportJson = vm::exportJson,
                                onImportJson = vm::importJson,
                                onExportCsv = vm::exportCsv,
                                onExportPdf = vm::exportPdf,
                                onResetAllData = vm::resetAllData,
                                onNavigateRoute = ::navigateTo
                            )
                        }
                        composable("daily_life") {
                            BackHandler { navController.popBackStack() }
                            DailyLifeHealthScreen(
                                state = state,
                                onPrevDay = { vm.stepSelectedDate(-1) },
                                onNextDay = { vm.stepSelectedDate(1) },
                                onGoToday = { vm.selectDate(state.todayIso) },
                                onUpdateHealth = vm::updateHealthSelectedDate,
                                onToggleTask = vm::toggleTask,
                                onAddPersonalHealthTask = { title, time, cat ->
                                    vm.saveTask(null, title, "", time, "", cat, "", 1, "ONCE", state.selectedDate, false, 0, "")
                                }
                            )
                        }
                        composable("family") {
                            BackHandler { navController.popBackStack() }
                            FamilySocialScreen(
                                state = state,
                                onPrevDay = { vm.stepSelectedDate(-1) },
                                onNextDay = { vm.stepSelectedDate(1) },
                                onGoToday = { vm.selectDate(state.todayIso) },
                                onToggleTask = vm::toggleTask,
                                onDeleteTask = vm::deleteTask,
                                onAddFamilyTask = { title, personTag, time, rec, note ->
                                    vm.saveTask(null, title, "", time, "", "FAMILY", personTag, 2, rec, state.selectedDate, false, 30, note)
                                }
                            )
                        }
                        composable("madrasa_work") {
                            BackHandler { navController.popBackStack() }
                            MadrasaWorkScreen(
                                state = state,
                                onSaveItem = vm::saveMadrasaWorkItem,
                                onToggleDone = vm::toggleMadrasaWorkCompleted,
                                onDelete = vm::deleteMadrasaWork
                            )
                        }
                        composable("education") {
                            BackHandler { navController.popBackStack() }
                            EducationSkillsScreen(
                                state = state,
                                onSaveItem = vm::saveMadrasaWorkItem,
                                onIncrementProgress = vm::incrementMadrasaWorkProgress,
                                onDelete = vm::deleteMadrasaWork
                            )
                        }
                        composable("finance") {
                            BackHandler { navController.popBackStack() }
                            FinanceScreen(
                                state = state,
                                onPrevDay = { vm.stepSelectedDate(-1) },
                                onNextDay = { vm.stepSelectedDate(1) },
                                onGoToday = { vm.selectDate(state.todayIso) },
                                onAddFinance = vm::addFinance,
                                onToggleSettled = vm::toggleFinanceSettled,
                                onDeleteFinance = vm::deleteFinance,
                                onUpdateBudget = { b -> vm.updatePreferences { it.copy(monthlyBudget = b) } }
                            )
                        }
                        composable("diary") {
                            BackHandler { navController.popBackStack() }
                            DiaryScreen(
                                state = state,
                                onSaveDiary = vm::saveDiary,
                                onDeleteDiary = vm::deleteDiary
                            )
                        }
                        composable("reports") {
                            BackHandler { navController.popBackStack() }
                            ReportsAnalyticsScreen(
                                state = state,
                                onExportPdf = { pdfExportLauncher.launch("amar-jibon-report-${state.todayIso}.pdf") },
                                onExportCsv = { csvExportLauncher.launch("amar-jibon-report-${state.todayIso}.csv") }
                            )
                        }
                        composable("reminders") {
                            BackHandler { navController.popBackStack() }
                            RemindersScreen(
                                state = state,
                                onTogglePrayerRem = { en -> vm.updatePreferences { it.copy(prayerRemindersEnabled = en) } },
                                onToggleWaterRem = { en -> vm.updatePreferences { it.copy(waterRemindersEnabled = en) } },
                                onUpdateSleepWake = { w, s ->
                                    vm.updatePreferences { it.copy(wakeReminderTime = w, sleepReminderTime = s) }
                                    vm.showToast(AppStrings.t(lang, "ঘুম ও ওঠার সময় সংরক্ষিত", "Sleep & wake reminders saved"))
                                },
                                onSaveReminder = vm::saveReminder,
                                onDeleteReminder = vm::deleteReminder,
                                onSendTestNotification = vm::sendTestNotification
                            )
                        }
                    }
                }
            }
        }
    }
}
