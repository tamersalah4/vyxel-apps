package com.vythera.vyxelapps

import android.os.Build
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

enum class VAppTab { HOME, SEARCH, INSTALLED, PROFILE, SETTINGS }

private const val Q_TRENDING     = "topic:android apk stars:>500"
private const val Q_MEDIA        = "topic:android media player stars:>100"
private const val Q_TOOLS        = "topic:android utility tool stars:>100"
private const val Q_GAMES        = "topic:android game emulator stars:>100"
private const val Q_BROWSERS     = "topic:android browser privacy stars:>50"
private const val Q_PRODUCTIVITY = "topic:android productivity notes stars:>50"
private const val Q_SECURITY     = "topic:android security stars:>50"
private const val Q_DEVTOOLS     = "topic:android developer-tools stars:>50"
private const val Q_PHOTO        = "topic:android photo video editor stars:>100"
private const val Q_MUSIC        = "topic:android music audio stars:>100"
private const val Q_FINANCE      = "topic:android finance banking stars:>100"
private const val Q_EDUCATION    = "topic:android education learning stars:>100"
private const val Q_FITNESS      = "topic:android fitness health workout stars:>100"
private const val Q_ART          = "topic:android art design creative stars:>100"
private const val Q_NEWS         = "topic:android news reader stars:>100"
private const val Q_SOCIAL       = "topic:android social network stars:>100"
private const val Q_CLOUD        = "topic:android cloud storage files stars:>100"
private const val Q_COOKING      = "topic:android cooking food recipe stars:>50"



@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun HomeScreen(viewModel: AppViewModel = viewModel()) {

    val state = viewModel.state   // ← must be FIRST

    val view         = LocalView.current
    val context      = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val homeListState = rememberLazyListState()

    var selectedTab   by remember { mutableStateOf(VAppTab.HOME) }
    var selectedRepo  by remember { mutableStateOf<GitHubRepo?>(null) }
    var showSeeAll    by remember { mutableStateOf(false) }
    var showCompare by remember { mutableStateOf(false) }
    var lastBackPress by remember { mutableLongStateOf(0L) }
    var dockVisible   by remember { mutableStateOf(true) }

    // ── Theme ─────────────────────────────────────────────────────────────
    val baseTheme: AppThemeColors = when {
        state.settings.themeMode == "Custom" -> state.customTheme.toAppThemeColors()
        state.settings.themeMode == "AMOLED" -> AmoledTheme
        state.settings.amoledBlack           -> AmoledTheme
        state.settings.themeMode == "Light"  -> LightTheme
        state.settings.themeMode == "Dark"   -> DarkTheme
        state.settings.themeMode == "Minimal"-> MinimalTheme
        state.settings.themeMode == "Sunset" -> SunsetTheme
        state.settings.themeMode == "System" -> if (isSystemDark) DarkTheme else LightTheme
        else                                  -> DarkTheme
    }

    // ── Full Monet palette extraction (Android 12+) ───────────────────
    // Each theme uses M3-correct tone mappings so Monet plays a structural
    // role (not just accent tinting).
    val monetPalette: Map<String, Color?> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        remember(view) {
            val ctx = view.context
            fun safe(id: Int): Color? = try {
                Color(ctx.resources.getColor(id, ctx.theme))
            } catch (_: Exception) { null }
            mapOf(
                "a1_100" to safe(android.R.color.system_accent1_100),
                "a1_200" to safe(android.R.color.system_accent1_200),
                "a1_300" to safe(android.R.color.system_accent1_300),
                "a1_400" to safe(android.R.color.system_accent1_400),
                "a1_600" to safe(android.R.color.system_accent1_600),
                "a1_700" to safe(android.R.color.system_accent1_700),
                "a1_900" to safe(android.R.color.system_accent1_900),
                "a2_200" to safe(android.R.color.system_accent2_200),
                "a2_400" to safe(android.R.color.system_accent2_400),
                "a2_600" to safe(android.R.color.system_accent2_600),
                "n1_800" to safe(android.R.color.system_neutral1_800),
                "n1_900" to safe(android.R.color.system_neutral1_900),
            )
        }
    } else emptyMap()

    val theme: AppThemeColors = run {
        val manualAccent = state.accentColor
        if (state.useMonet && monetPalette.isNotEmpty()) {
            val dark = baseTheme.isDark
            // M3 tonal mapping: dark themes use lighter tones on dark bg, light themes use darker tones
            val primary    = manualAccent ?: if (dark) monetPalette["a1_200"] else monetPalette["a1_600"]
            val secondary  = if (dark) monetPalette["a2_200"] else monetPalette["a2_600"]
            val container  = if (dark) monetPalette["a1_700"] else monetPalette["a1_100"]
            val onContainer= if (dark) monetPalette["a1_100"] else monetPalette["a1_900"]
            baseTheme.copy(
                accent            = primary      ?: baseTheme.accent,
                accentAlt         = secondary    ?: baseTheme.accentAlt,
                accentContainer   = container    ?: baseTheme.accentContainer,
                onAccentContainer = onContainer  ?: baseTheme.onAccentContainer,
                dockForeground    = (primary     ?: baseTheme.accent)
            )
        } else {
            val eff = manualAccent ?: baseTheme.accent
            baseTheme.copy(accent = eff, dockForeground = eff)
        }
    }
    val fontFamily = remember(state.settings.fontName) { fontFamilyFor(state.settings.fontName.ifEmpty { "Default" }) }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !theme.isDark
        }
    }

    val installedSet = remember(state.installStates) {
        state.installStates.filter { e -> e.value.isInstalled }.keys
    }

    // ── Back ──────────────────────────────────────────────────────────────
    BackHandler {
        when {
            showCompare -> { showCompare = false; viewModel.setCompareTarget(null) }
            selectedRepo != null -> {
                viewModel.refreshInstall(selectedRepo!!.id)
                selectedRepo = null
            }
            showSeeAll -> showSeeAll = false
            selectedTab == VAppTab.SEARCH &&
                (state.platform != AppPlatform.ALL || state.selectedSubCategories.isNotEmpty()) -> {
                viewModel.clearSearchFilter()
            }
            selectedTab != VAppTab.HOME -> selectedTab = VAppTab.HOME
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPress < 2000) {
                    (context as? android.app.Activity)?.finish()
                } else {
                    lastBackPress = now
                    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    LaunchedEffect(selectedRepo) {
        selectedRepo?.let {
            viewModel.fetchRelease(it)
            viewModel.fetchScreenshots(it)
        }
    }
    LaunchedEffect(selectedRepo, state.settings.language) {
        if (selectedRepo != null && state.settings.language != "English") {
            viewModel.translateDescription(selectedRepo!!)
        }
    }

    CompositionLocalProvider(LocalTheme provides theme, LocalStrings provides stringsForLanguage(state.settings.language)) {
        MaterialTheme(
            typography = MaterialTheme.typography.run {
                copy(
                    displayLarge   = displayLarge.copy(fontFamily   = fontFamily),
                    displayMedium  = displayMedium.copy(fontFamily  = fontFamily),
                    displaySmall   = displaySmall.copy(fontFamily   = fontFamily),
                    headlineLarge  = headlineLarge.copy(fontFamily  = fontFamily),
                    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
                    headlineSmall  = headlineSmall.copy(fontFamily  = fontFamily),
                    titleLarge     = titleLarge.copy(fontFamily     = fontFamily),
                    titleMedium    = titleMedium.copy(fontFamily    = fontFamily),
                    titleSmall     = titleSmall.copy(fontFamily     = fontFamily),
                    bodyLarge      = bodyLarge.copy(fontFamily      = fontFamily),
                    bodyMedium     = bodyMedium.copy(fontFamily     = fontFamily),
                    bodySmall      = bodySmall.copy(fontFamily      = fontFamily),
                    labelLarge     = labelLarge.copy(fontFamily     = fontFamily),
                    labelMedium    = labelMedium.copy(fontFamily    = fontFamily),
                    labelSmall     = labelSmall.copy(fontFamily     = fontFamily)
                )
            }
        ) {
            Scaffold(
                containerColor      = theme.bgPrimary,
                contentWindowInsets = WindowInsets(0)
            ) { _ ->

                // Holds the last non-null repo so exit animations still have content to render
                var lastRepo by remember { mutableStateOf<GitHubRepo?>(null) }
                if (selectedRepo != null) lastRepo = selectedRepo

                val currentScreen = when {
                    showCompare -> "COMPARE"
                    selectedRepo != null -> "DETAIL"
                    showSeeAll -> "SEE_ALL"
                    else -> "TABS"
                }

                Box(modifier = Modifier.fillMaxSize()) {

                    AnimatedContent(
                        targetState  = currentScreen,
                        transitionSpec = {
                            val order   = listOf("TABS", "SEE_ALL", "DETAIL", "COMPARE")
                            val forward = order.indexOf(targetState) > order.indexOf(initialState)
                            if (forward) {
                                (slideInHorizontally(tween(320)) { it } + fadeIn(tween(250))) togetherWith
                                        (slideOutHorizontally(tween(280)) { -it } + fadeOut(tween(180)))
                            } else {
                                (slideInHorizontally(tween(320)) { -it } + fadeIn(tween(250))) togetherWith
                                        (slideOutHorizontally(tween(280)) { it } + fadeOut(tween(180)))
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        label    = "screen_nav"
                    ) { screen ->
                        val repo = lastRepo
                        when (screen) {
// ── Compare ───────────────────────────────────
                            "COMPARE" -> {
                                if (repo != null) {
                                    CompareScreen(
                                        leftRepo      = repo,
                                        rightRepo     = state.compareTargetRepo,
                                        searchResults = state.searchResults,
                                        onSearch      = { viewModel.onSearch(it) },
                                        onPickRight   = { viewModel.setCompareTarget(it) },
                                        onBack        = { showCompare = false; viewModel.setCompareTarget(null) }
                                    )
                                }
                            }
// ── Detail ────────────────────────────────────
                            "DETAIL" -> {
                                if (repo != null) {
                                    val installState = state.installStates[repo.id] ?: InstallState()
                                    AppDetailScreen(
                                        repo              = repo,
                                        installState      = installState,
                                        isFavourite       = state.favourites.any { f -> f.id == repo.id },
                                        translatedDesc    = state.translatedDescriptions[repo.id],
                                        isTranslating     = state.isTranslating[repo.id] ?: false,
                                        state             = state,
                                        screenshots       = state.screenshots[repo.id] ?: emptyList(),
                                        onInstall         = { installState.apkAsset?.let { a -> viewModel.downloadAndInstall(repo, a) } },
                                        onDownloadOnly    = { installState.apkAsset?.let { a -> viewModel.downloadOnly(repo, a) } },
                                        onUninstall       = { viewModel.uninstall(repo) },
                                        onCancelDownload  = { viewModel.cancelDownload(repo) },
                                        onTranslate       = { viewModel.translateDescription(repo) },
                                        onToggleFavourite = { viewModel.toggleFavourite(repo) },
                                        onIgnoreVersion   = {
                                            installState.release?.tag_name?.let { viewModel.ignoreVersion(repo.id, it) }
                                        },
                                        onCompare         = { showCompare = true },
                                        onBack            = { viewModel.refreshInstall(repo.id); selectedRepo = null }
                                    )
                                }
                            }
// ── See All ───────────────────────────────────
                            "SEE_ALL" -> SeeAllScreen(
                                title      = state.seeAllTitle,
                                apps       = state.seeAllApps,
                                installed  = installedSet,
                                isLoading  = state.isLoadingSeeAll,
                                onLoadMore = { viewModel.loadMoreSeeAll() },
                                onAppClick = { r -> viewModel.addToHistory(r); selectedRepo = r },
                                onBack     = { showSeeAll = false }
                            )
// ── Tabs ──────────────────────────────────────
                            else -> AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    val goingRight = targetState.ordinal > initialState.ordinal
                                    if (goingRight) {
                                        (slideInHorizontally(tween(280)) { it } + fadeIn(tween(200))) togetherWith
                                                (slideOutHorizontally(tween(280)) { -it } + fadeOut(tween(160)))
                                    } else {
                                        (slideInHorizontally(tween(280)) { -it } + fadeIn(tween(200))) togetherWith
                                                (slideOutHorizontally(tween(280)) { it } + fadeOut(tween(160)))
                                    }
                                },
                                label = "tab_switch"
                            ) { tab ->
                                when (tab) {
                                    VAppTab.HOME -> HomeTab(
                                        state          = state,
                                        viewModel      = viewModel,
                                        installed      = installedSet,
                                        listState      = homeListState,
                                        onAppClick     = { r -> viewModel.addToHistory(r); selectedRepo = r },
                                        onSeeAll       = { showSeeAll = true },
                                        onScrollChange = { scrolling -> dockVisible = !scrolling }
                                    )
                                    VAppTab.SEARCH -> SearchScreen(
                                        query                 = state.searchQuery,
                                        results               = state.searchResults,
                                        platform              = state.platform,
                                        selectedSubCategories = state.selectedSubCategories,
                                        installed             = installedSet,
                                        suggestions           = state.trending.take(10),
                                        isSearching           = state.isSearching,
                                        onQueryChange         = { viewModel.onSearch(it) },
                                        onPlatformChange      = { viewModel.setPlatform(it) },
                                        onSubCategoryToggle   = { viewModel.toggleSubCategory(it) },
                                        onAppClick            = { r -> viewModel.addToHistory(r); selectedRepo = r },
                                        isFilterMenuOpen      = state.isFilterMenuOpen,
                                        activeSubMenuPlatform = state.activeSubMenuPlatform,
                                        onToggleFilterMenu    = { viewModel.toggleFilterMenu(it) },
                                        onSetSubMenuPlatform  = { viewModel.setSubMenuPlatform(it) }
                                    )
                                    VAppTab.INSTALLED -> InstalledScreen(
                                        installHistory    = state.installHistory,
                                        installStates     = state.installStates,
                                        updates           = state.updates,
                                        onAppClick        = { r -> selectedRepo = r },
                                        onCheckUpdates    = { viewModel.checkForUpdatesNow() },
                                        isCheckingUpdates = state.isCheckingUpdates
                                    )
                                    VAppTab.PROFILE -> ProfileScreen(
                                        profile         = state.profile,
                                        history         = state.history,
                                        favourites      = state.favourites,
                                        installHistory  = state.installHistory,
                                        updates         = state.updates,
                                        onSave          = { viewModel.updateProfile(it) },
                                        onAppClick      = { r -> selectedRepo = r },
                                        onCheckUpdates  = { viewModel.checkForUpdatesNow() },
                                        onRollback      = { entry -> viewModel.rollbackTo(entry) }
                                    )
                                    VAppTab.SETTINGS -> SettingsScreen(
                                        settings          = state.settings,
                                        currentAccent     = state.accentColor,
                                        useMonet          = state.useMonet,
                                        customTheme       = state.customTheme,
                                        onSave            = { viewModel.updateSettings(it) },
                                        onAccentSelect    = { viewModel.setAccentColor(it) },
                                        onMonetToggle     = { viewModel.setUseMonet(it) },
                                        onCustomThemeSave = { viewModel.setCustomTheme(it) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Floating dock ─────────────────────────────────
                    if (selectedRepo == null && !showSeeAll && !showCompare) {
                        AnimatedVisibility(
                            visible  = dockVisible,
                            enter    = slideInVertically(tween(220)) { it } + fadeIn(tween(180)),
                            exit     = slideOutVertically(tween(180)) { it } + fadeOut(tween(140)),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                FloatingNavBar(
                                    selectedTab = selectedTab,
                                    theme       = theme,
                                    updateCount = state.updates.size,
                                    onTabSelect = { VApptab ->
                                        selectedTab  = VApptab
                                        selectedRepo = null
                                        showSeeAll   = false
                                        dockVisible  = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// HOME TAB
// ─────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    state          : UiState,
    viewModel      : AppViewModel,
    installed      : Set<Long>,
    listState      : LazyListState,
    onAppClick     : (GitHubRepo) -> Unit,
    onSeeAll       : () -> Unit,
    onScrollChange : (Boolean) -> Unit = {}
) {
    val theme = LocalTheme.current
    val strings = LocalStrings.current
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) isRefreshing = false
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            onScrollChange(true)
        } else {
            delay(300)
            onScrollChange(false)
        }
    }

    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && last >= total - 6
        }
    }
    LaunchedEffect(nearEnd) {
        if (nearEnd && !state.isLoadingMore && !state.isLoading && state.platform == AppPlatform.ALL) {
            viewModel.loadMoreTrending()
        }
    }

    fun openSeeAll(title: String, query: String) {
        viewModel.openSeeAll(title, query)
        onSeeAll()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            if (!isRefreshing) {
                isRefreshing = true; viewModel.loadAll()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state          = listState,
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 180.dp)
            ) {

                if (state.trending.isNotEmpty()) {
                    item(key = "hero_banner") {
                        HeroBanner(apps = state.trending, onAppClick = onAppClick)
                    }
                }

                item(key = "collections") {
                    CollectionsRow { collection ->
                        viewModel.openCollection(collection)
                        onSeeAll()
                    }
                }

                item(key = "sources") {
                    SourcesRow(
                        gitlabCount   = state.gitlabApps.size,
                        codebergCount = state.codebergApps.size,
                        fdroidCount   = state.fdroidApps.size,
                        flathubCount  = state.flathubApps.size,
                        wingetCount   = state.wingetApps.size,
                        onSourceClick = { source ->
                            viewModel.openSourceBrowse(source)
                            onSeeAll()
                        }
                    )
                }

// Collections row — add this right after banner


                if (state.error != null) {
                    item(key = "error") { ErrorPlaceholder(state.error) { viewModel.loadAll() } }
                } else if (state.platform != AppPlatform.ALL) {
                    if (state.platformApps.isEmpty()) {
                        item(key = "plat_load") { LoadingPlaceholder() }
                    } else {
                        item(key = "plat_grid") {
                            PlatformGrid(
                                platform = state.platform,
                                apps = state.platformApps,
                                installed = installed,
                                onAppClick = onAppClick
                            )
                        }
                    }
                } else if (state.isLoading) {
                    item(key = "loading") { LoadingPlaceholder() }
                } else {
                    if (state.recommendations.isNotEmpty()) {
                        item(key = "recs") {
                            AppRow(
                                strings.sectionRecommended,
                                state.recommendations,
                                installed
                            ) { onAppClick(it) }
                        }
                    }
                    item(key = "r1") {
                        AppRow(
                            title = strings.sectionTrending,
                            apps = state.trending,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r2") {
                        AppRow(
                            title = strings.sectionMedia,
                            apps = state.media,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r3") {
                        AppRow(
                            title = strings.sectionTools,
                            apps = state.tools,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r4") {
                        AppRow(
                            title = strings.sectionGames,
                            apps = state.games,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r5") {
                        AppRow(
                            title = strings.sectionBrowsers,
                            apps = state.browsers,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r6") {
                        AppRow(
                            title = strings.sectionProductivity,
                            apps = state.productivity,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r7") {
                        AppRow(
                            title = strings.sectionSecurity,
                            apps = state.security,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r8") {
                        AppRow(
                            title = strings.sectionDevTools,
                            apps = state.devtools,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r9") {
                        AppRow(
                            title = strings.sectionPhotoVideo,
                            apps = state.photoVideo,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r10") {
                        AppRow(
                            title = strings.sectionMusic,
                            apps = state.music,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r11") {
                        AppRow(
                            title = strings.sectionFinance,
                            apps = state.finance,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r12") {
                        AppRow(
                            title = strings.sectionEducation,
                            apps = state.education,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r13") {
                        AppRow(
                            title = strings.sectionFitness,
                            apps = state.fitness,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r14") {
                        AppRow(
                            title = strings.sectionArtDesign,
                            apps = state.artDesign,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r15") {
                        AppRow(
                            title = strings.sectionNews,
                            apps = state.news,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r16") {
                        AppRow(
                            title = strings.sectionSocial,
                            apps = state.social,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r17") {
                        AppRow(
                            title = strings.sectionCloudStorage,
                            apps = state.cloudStorage,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    item(key = "r18") {
                        AppRow(
                            title = strings.sectionCooking,
                            apps = state.cooking,
                            installed = installed,
                            refreshToken = state.refreshToken,
                            onAppClick = { repo -> onAppClick(repo) }
                        )
                    }
                    if (state.isLoadingMore) {
                        item(key = "load_more") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = theme.accent,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }


TopBar(modifier = Modifier.align(Alignment.TopCenter))

        }
    }
}
@Composable
fun LoadingPlaceholder() {
    val t = LocalTheme.current
    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = t.accent)
            Spacer(Modifier.height(14.dp))
            Text("Loading apps from all sources…", color = t.textSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorPlaceholder(message: String, onRetry: () -> Unit) {
    val t = LocalTheme.current
    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️", fontSize = 44.sp)
            Text(message, color = t.textSecondary, fontSize = 14.sp)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = t.accent)) {
                Text("Try Again")
            }
        }
    }
}