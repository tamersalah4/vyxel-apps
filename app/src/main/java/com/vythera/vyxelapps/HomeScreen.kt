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

enum class AppTab { HOME, SEARCH, INSTALLED, PROFILE, SETTINGS }

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

    if (state.isSetupDone == false) {   // ← == false avoids the WideNavigationRailValue conflict
        SetupScreen(
            onComplete = { token -> viewModel.completeSetup(token) },
            onSkip     = { viewModel.completeSetup("") }
        )
        return
    }
    val view         = LocalView.current
    val context      = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val homeListState = rememberLazyListState()

    var selectedTab   by remember { mutableStateOf(AppTab.HOME) }
    var selectedRepo  by remember { mutableStateOf<GitHubRepo?>(null) }
    var showSeeAll    by remember { mutableStateOf(false) }
    var showCompare by remember { mutableStateOf(false) }
    var lastBackPress by remember { mutableLongStateOf(0L) }
    var dockVisible   by remember { mutableStateOf(true) }

    // ── Theme ─────────────────────────────────────────────────────────────
    val baseTheme = when {
        state.settings.amoledBlack           -> AmoledTheme
        state.settings.themeMode == "Light"  -> LightTheme
        state.settings.themeMode == "Dark"   -> DarkTheme
        state.settings.themeMode == "System" -> if (isSystemDark) DarkTheme else LightTheme
        else                                  -> DarkTheme
    }
    val monetColor = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Color(view.context.resources.getColor(android.R.color.system_accent1_400, view.context.theme))
        else null
    }
    val effectiveAccent = when {
        state.useMonet && monetColor != null -> monetColor
        state.accentColor != null            -> state.accentColor
        else                                 -> baseTheme.accent
    }
    val theme      = baseTheme.copy(accent = effectiveAccent)
    val fontFamily = fontFamilyFor(state.settings.fontName.ifEmpty { "Default" })

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
            selectedTab != AppTab.HOME -> selectedTab = AppTab.HOME
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

    CompositionLocalProvider(LocalTheme provides theme) {
        MaterialTheme(
            typography = MaterialTheme.typography.run {
                copy(
                    bodyLarge   = bodyLarge.copy(fontFamily   = fontFamily),
                    bodyMedium  = bodyMedium.copy(fontFamily  = fontFamily),
                    bodySmall   = bodySmall.copy(fontFamily   = fontFamily),
                    labelMedium = labelMedium.copy(fontFamily = fontFamily),
                    labelSmall  = labelSmall.copy(fontFamily  = fontFamily)
                )
            }
        ) {
            Scaffold(
                containerColor      = theme.bgPrimary,
                contentWindowInsets = WindowInsets(0)
            ) { _ ->
                Box(modifier = Modifier.fillMaxSize()) {

                    // ── Detail ────────────────────────────────────────
                    // ── Compare ───────────────────────────────────
                    if (showCompare && selectedRepo != null) {
                        CompareScreen(
                            leftRepo      = selectedRepo!!,
                            rightRepo     = state.compareTargetRepo,
                            searchResults = state.searchResults,
                            onSearch      = { viewModel.onSearch(it) },
                            onPickRight   = { viewModel.setCompareTarget(it) },
                            onBack        = { showCompare = false; viewModel.setCompareTarget(null) }
                        )
                    }
// ── Detail ────────────────────────────────────
                    else if (selectedRepo != null) {
                        val repo         = selectedRepo!!
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
// ── See All ───────────────────────────────────
                    else if (showSeeAll) {
                        SeeAllScreen(
                            title      = state.seeAllTitle,
                            apps       = state.seeAllApps,
                            installed  = installedSet,
                            isLoading  = state.isLoadingSeeAll,
                            onLoadMore = { viewModel.loadMoreSeeAll() },
                            onAppClick = { repo -> viewModel.addToHistory(repo); selectedRepo = repo },
                            onBack     = { showSeeAll = false }
                        )
                    }
// ── Tabs ──────────────────────────────────────
                    else {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                // Compare ordinal: right swipe if moving to a higher-index tab
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
                                AppTab.HOME -> HomeTab(
                                    state          = state,
                                    viewModel      = viewModel,
                                    installed      = installedSet,
                                    listState      = homeListState,
                                    onAppClick     = { repo -> viewModel.addToHistory(repo); selectedRepo = repo },
                                    onSeeAll       = { showSeeAll = true },
                                    onScrollChange = { scrolling -> dockVisible = !scrolling }
                                )
                                AppTab.SEARCH -> SearchScreen(
                                    query         = state.searchQuery,
                                    results       = state.searchResults,
                                    installed     = installedSet,
                                    suggestions   = state.trending.take(10),
                                    isSearching   = state.isSearching,
                                    onQueryChange = { viewModel.onSearch(it) },
                                    onAppClick    = { repo -> viewModel.addToHistory(repo); selectedRepo = repo }
                                )
                                AppTab.INSTALLED -> InstalledScreen(
                                    installStates = state.installStates,
                                    onAppClick    = { repo -> selectedRepo = repo }
                                )
                                AppTab.PROFILE -> ProfileScreen(
                                    profile         = state.profile,
                                    history         = state.history,
                                    favourites      = state.favourites,
                                    installHistory  = state.installHistory,
                                    updates         = state.updates,
                                    onSave          = { viewModel.updateProfile(it) },
                                    onAppClick      = { repo -> selectedRepo = repo },
                                    onCheckUpdates  = { viewModel.checkForUpdatesNow() },
                                    onRollback      = { entry -> viewModel.rollbackTo(entry) }
                                )
                                AppTab.SETTINGS -> SettingsScreen(
                                    settings       = state.settings,
                                    currentAccent  = state.accentColor,
                                    useMonet       = state.useMonet,
                                    onSave         = { viewModel.updateSettings(it) },
                                    onAccentSelect = { viewModel.setAccentColor(it) },
                                    onMonetToggle  = { viewModel.setUseMonet(it) }
                                )
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
                                    onTabSelect = { tab ->
                                        selectedTab  = tab
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
    val theme        = LocalTheme.current
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) isRefreshing = false
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            onScrollChange(true)
        } else {
            delay(600)
            onScrollChange(false)
        }
    }

    val nearEnd by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
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
        onRefresh    = { if (!isRefreshing) { isRefreshing = true; viewModel.loadAll() } },
        modifier     = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 180.dp)
        ) {
            item(key = "topbar") {
                TopBar(selectedPlatform = state.platform, onPlatformSelect = { viewModel.setPlatform(it) })
            }

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

// Collections row — add this right after banner



            if (state.isLoading) {
                item(key = "loading") { LoadingPlaceholder() }
            } else if (state.error != null) {
                item(key = "error") { ErrorPlaceholder(state.error) { viewModel.loadAll() } }
            } else if (state.platform != AppPlatform.ALL) {
                if (state.platformApps.isEmpty()) {
                    item(key = "plat_load") { LoadingPlaceholder() }
                } else {
                    item(key = "plat_grid") {
                        PlatformGrid(platform = state.platform, apps = state.platformApps, installed = installed, onAppClick = onAppClick)
                    }
                }
            } else {
                if (state.recommendations.isNotEmpty()) {
                    item(key = "recs") {
                        AppRow("⭐ Recommended for You", state.recommendations, installed) { onAppClick(it) }
                    }
                }
                item(key = "r1") {
                    AppRow(
                        title        = "🔥 Trending Now",
                        apps         = state.trending,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r2") {
                    AppRow(
                        title        = "🎬 Media & Entertainment",
                        apps         = state.media,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r3") {
                    AppRow(
                        title        = "🛠 Tools & Utilities",
                        apps         = state.tools,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r4") {
                    AppRow(
                        title        = "🎮 Games & Emulators",
                        apps         = state.games,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r5") {
                    AppRow(
                        title        = "🌐 Browsers & Privacy",
                        apps         = state.browsers,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r6") {
                    AppRow(
                        title        = "📋 Productivity",
                        apps         = state.productivity,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r7") {
                    AppRow(
                        title        = "🔒 Security",
                        apps         = state.security,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r8") {
                    AppRow(
                        title        = "💻 Dev Tools",
                        apps         = state.devtools,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r9") {
                    AppRow(
                        title        = "📸 Photo & Video",
                        apps         = state.photoVideo,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r10") {
                    AppRow(
                        title        = "🎵 Music & Audio",
                        apps         = state.music,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r11") {
                    AppRow(
                        title        = "💰 Finance",
                        apps         = state.finance,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r12") {
                    AppRow(
                        title        = "📚 Education",
                        apps         = state.education,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r13") {
                    AppRow(
                        title        = "💪 Fitness & Health",
                        apps         = state.fitness,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r14") {
                    AppRow(
                        title        = "🎨 Art & Design",
                        apps         = state.artDesign,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r15") {
                    AppRow(
                        title        = "🗞 News",
                        apps         = state.news,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r16") {
                    AppRow(
                        title        = "💬 Social",
                        apps         = state.social,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r17") {
                    AppRow(
                        title        = "☁️ Cloud Storage",
                        apps         = state.cloudStorage,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }
                item(key = "r18") {
                    AppRow(
                        title        = "🍳 Cooking & Food",
                        apps         = state.cooking,
                        installed    = installed,
                        refreshToken = state.refreshToken,
                        onAppClick   = { repo -> onAppClick(repo) }
                    )
                }

                if (state.isLoadingMore) {
                    item(key = "load_more") {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = theme.accent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
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
            Text("Loading apps from GitHub…", color = t.textSecondary, fontSize = 14.sp)
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