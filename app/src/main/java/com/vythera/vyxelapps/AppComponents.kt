package com.vythera.vyxelapps

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────
// TOP BAR — minimal: logo + name right-aligned, filter button far right
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun TopBar(
    selectedPlatform : AppPlatform,
    onPlatformSelect : (AppPlatform) -> Unit
) {
    val t = LocalTheme.current
    var showFilter by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.bgSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // App name + logo (left/center)
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter            = painterResource(R.drawable.skpic),
                contentDescription = "Logo",
                modifier           = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp))
            )
            Text(
                "Vyxel Apps",
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = t.textPrimary
            )
        }

        // Filter chip
        Box {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(t.bgSurfaceAlt)
                    .border(1.dp, t.border, RoundedCornerShape(20.dp))
                    .clickable { showFilter = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "${selectedPlatform.emoji} ${selectedPlatform.label}",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = t.textPrimary
                    )
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = t.textSecondary, modifier = Modifier.size(16.dp))
                }
            }

            DropdownMenu(
                expanded         = showFilter,
                onDismissRequest = { showFilter = false },
                containerColor   = t.bgSurface
            ) {
                AppPlatform.values().forEach { platform ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(platform.emoji, fontSize = 16.sp)
                                Text(
                                    platform.label,
                                    fontSize   = 14.sp,
                                    color      = if (platform == selectedPlatform) t.accent else t.textPrimary,
                                    fontWeight = if (platform == selectedPlatform) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = { onPlatformSelect(platform); showFilter = false },
                        leadingIcon = if (platform == selectedPlatform) {{
                            Icon(Icons.Rounded.Check, null, tint = t.accent, modifier = Modifier.size(16.dp))
                        }} else null
                    )
                }
            }
        }
    }
}

@Composable
fun DockItem(icon: ImageVector, label: String, active: Boolean, t: AppThemeColors, onClick: () -> Unit) {
    val inactiveColor = t.dockForeground.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) t.accent.copy(0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, label,
                tint     = if (active) t.accent else inactiveColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.height(1.dp))
            Text(
                label,
                fontSize   = 9.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color      = if (active) t.accent else inactiveColor
            )
            if (active) {
                Spacer(Modifier.height(1.dp))
                Box(
                    Modifier
                        .size(width = 12.dp, height = 2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(t.accent)
                )
            } else {
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// HERO BANNER
// ─────────────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(apps: List<GitHubRepo>, onAppClick: (GitHubRepo) -> Unit) {
    if (apps.isEmpty()) return
    val t          = LocalTheme.current
    val bannerApps = remember(apps) { apps.shuffled().take(7) }
    val pagerState = rememberPagerState(pageCount = { bannerApps.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            val next = (pagerState.currentPage + 1) % bannerApps.size
            pagerState.animateScrollToPage(next, animationSpec = tween(1400, easing = EaseInOutCubic))
        }
    }

    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { page ->
            val repo      = bannerApps[page]
            val cardColor = CardColors[abs(repo.name.hashCode()) % CardColors.size]

            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)).clickable { onAppClick(repo) }) {
                Box(Modifier.fillMaxSize().background(cardColor))
                AsyncImage(model = repo.owner.avatar_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.1f), Color.Black.copy(0.92f)), startY = 60f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(t.accent).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("FEATURED", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(repo.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(repo.description ?: "", fontSize = 12.sp, color = Color.White.copy(0.78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("★ ${formatStars(repo.stargazers_count)}", fontSize = 12.sp, color = StarGold, fontWeight = FontWeight.SemiBold)
                        if (!repo.language.isNullOrEmpty()) {
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.18f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(repo.language, fontSize = 11.sp, color = Color.White.copy(0.9f))
                            }
                        }
                    }
                }
            }
        }

        // Dots
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.Center) {
            repeat(bannerApps.size) { i ->
                val selected = i == pagerState.currentPage
                Box(modifier = Modifier.padding(horizontal = 3.dp).height(4.dp).width(if (selected) 20.dp else 4.dp).clip(RoundedCornerShape(2.dp)).background(if (selected) t.accent else t.border))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// APP LIST TILE — main tile format (left photo + right text)
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun AppListTile(
    repo        : GitHubRepo,
    isInstalled : Boolean = false,
    onClick     : () -> Unit
) {
    val t         = LocalTheme.current
    val tileColor = CardColors[abs(repo.name.hashCode()) % CardColors.size]
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.98f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "tileScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(tileColor.copy(alpha = 0.10f))
            .border(1.dp, tileColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Small circular avatar
        AsyncImage(
            model              = repo.owner.avatar_url,
            contentDescription = null,
            modifier           = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tileColor.copy(alpha = 0.3f)),
            contentScale = ContentScale.Crop
        )

        // Text block
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "@${repo.owner.login}",
                fontSize = 11.sp,
                color    = t.textSecondary.copy(alpha = 0.65f)
            )
            Text(
                repo.name,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = t.textPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (!repo.description.isNullOrEmpty()) {
                Text(
                    repo.description,
                    fontSize   = 12.sp,
                    color      = t.textSecondary,
                    maxLines   = 2,
                    lineHeight = 16.sp,
                    overflow   = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("★ ${formatStars(repo.stargazers_count)}", fontSize = 11.sp, color = StarGold)
                if (!repo.language.isNullOrEmpty()) {
                    Text("·", fontSize = 11.sp, color = t.textSecondary)
                    Text(repo.language, fontSize = 11.sp, color = t.textSecondary)
                }
                if (isInstalled) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GreenOk.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("Installed", fontSize = 10.sp, color = GreenOk, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Icon(
            Icons.Rounded.ChevronRight, null,
            tint     = tileColor.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// APP ROW — horizontal scroll of list tiles
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun AppRow(
    title        : String,
    apps         : List<GitHubRepo>,
    installed    : Set<Long> = emptySet(),
    refreshToken : Int       = 0,
    onAppClick   : (GitHubRepo) -> Unit
) {
    val t = LocalTheme.current
    if (apps.isEmpty()) return

    val displayList = remember(apps, refreshToken) { apps.shuffled() }

    // Only go infinite if there are enough items to avoid obvious repetition
    val useInfinite = displayList.size >= 4
    val virtualCount = if (useInfinite) Int.MAX_VALUE / 2 else displayList.size

    // Start near the middle, aligned to a clean cycle boundary so item 0 shows first
    val startIndex = remember(displayList.size, useInfinite) {
        if (useInfinite) {
            val mid = virtualCount / 2
            mid - (mid % displayList.size)
        } else 0
    }

    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            title,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = t.textPrimary,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            state                 = rowState,
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = virtualCount,
                key   = { idx -> "${displayList[idx % displayList.size].id}_$idx" }   // ✅ added here
            ) { index ->
                val repo = displayList[index % displayList.size]
                AppCard(
                    repo        = repo,
                    isInstalled = installed.contains(repo.id),
                    onClick     = { onAppClick(repo) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PLATFORM GRID — for filtered views
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun PlatformGrid(
    platform  : AppPlatform,
    apps      : List<GitHubRepo>,
    installed : Set<Long> = emptySet(),
    onAppClick: (GitHubRepo) -> Unit
) {
    val t = LocalTheme.current
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(platform.emoji, fontSize = 20.sp)
            Text("${platform.label} Apps", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            apps.forEach { repo ->
                AppListTile(repo = repo, isInstalled = installed.contains(repo.id), onClick = { onAppClick(repo) })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SEARCH SCREEN
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun SearchScreen(
    query         : String,
    results       : List<GitHubRepo>,
    installed     : Set<Long>      = emptySet(),
    suggestions   : List<GitHubRepo> = emptyList(),
    isSearching   : Boolean        = false,
    onQueryChange : (String) -> Unit,
    onAppClick    : (GitHubRepo) -> Unit
) {
    val t = LocalTheme.current
    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(t.bgSurface)
                .statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TextField(
                value = query, onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                placeholder = { Text("Search apps on GitHub…", color = t.textSecondary) },
                singleLine  = true,
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = t.textSecondary) },
                trailingIcon = if (query.isNotEmpty()) {{
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Clear, null, tint = t.textSecondary)
                    }
                }} else null,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = t.bgSurfaceAlt, unfocusedContainerColor = t.bgSurfaceAlt,
                    focusedIndicatorColor   = t.accent,       unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = t.textPrimary,  unfocusedTextColor      = t.textPrimary
                )
            )
        }

        LazyColumn(
            contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (query.isBlank()) {
                if (suggestions.isNotEmpty()) {
                    item {
                        Text("Trending", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = t.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(items = suggestions) { repo ->
                        AppListTile(repo = repo, isInstalled = installed.contains(repo.id),
                            onClick = { onAppClick(repo) })
                    }
                }
            } else {
                if (isSearching && results.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CircularProgressIndicator(color = t.accent, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                                Text("Searching for \"$query\"…", color = t.textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(items = results) { repo ->
                        AppListTile(repo = repo, isInstalled = installed.contains(repo.id),
                            onClick = { onAppClick(repo) })
                    }
                    if (results.isNotEmpty() && isSearching) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(color = t.accent, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                                    Text("Finding more results…", fontSize = 12.sp, color = t.textSecondary)
                                }
                            }
                        }
                    }
                    if (results.isEmpty() && !isSearching) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.SearchOff, null, tint = t.textSecondary, modifier = Modifier.size(48.dp))
                                    Text("No results for \"$query\"", color = t.textSecondary)
                                    Text("Try a shorter or different term", fontSize = 12.sp, color = t.textSecondary.copy(0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// INSTALLED SCREEN
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun InstalledScreen(
    installStates : Map<Long, InstallState>,
    onAppClick    : (GitHubRepo) -> Unit
) {
    val t = LocalTheme.current
    val installedApps = remember(installStates) {
        installStates.values.filter { it.isInstalled && it.repo != null }.mapNotNull { it.repo }.distinctBy { it.id }
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {
        Column(modifier = Modifier.fillMaxWidth().background(t.bgSurface).statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("Installed", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
        }

        if (installedApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.InstallMobile, null, tint = t.accent, modifier = Modifier.size(64.dp))
                    Text("No apps installed yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = t.textPrimary)
                    Text("Apps you install via Vyxel appear here", color = t.textSecondary, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(installedApps) { repo ->
                    AppListTile(repo = repo, isInstalled = true, onClick = { onAppClick(repo) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// APP DETAIL SCREEN — full screen
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun AppDetailScreen(
    repo             : GitHubRepo,
    installState     : InstallState,
    isFavourite      : Boolean,
    translatedDesc   : String?,
    isTranslating    : Boolean,
    state            : UiState,
    screenshots      : List<String>      = emptyList(),
    onInstall        : () -> Unit,
    onDownloadOnly   : () -> Unit,
    onUninstall      : () -> Unit,
    onCancelDownload : () -> Unit,
    onTranslate      : () -> Unit,
    onToggleFavourite: () -> Unit,
    onIgnoreVersion  : () -> Unit        = {},
    onCompare        : () -> Unit        = {},
    onBack           : () -> Unit
) {
    val t         = LocalTheme.current
    val context   = LocalContext.current
    val cardColor = CardColors[abs(repo.name.hashCode()) % CardColors.size]

    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {
        // Header with back + favourite
        Row(
            modifier = Modifier.fillMaxWidth().background(t.bgSurface).statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = t.textPrimary)
            }
            Text(repo.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.textPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = onToggleFavourite) {
                Icon(if (isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Favourite", tint = if (isFavourite) RedDanger else t.textSecondary)
            }
        }

        LazyColumn(
            contentPadding      = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // App hero image
            item {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    Box(Modifier.fillMaxSize().background(cardColor))
                    AsyncImage(model = repo.owner.avatar_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, t.bgPrimary), startY = 120f)))
                }
            }

            // App info
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("@${repo.owner.login}", fontSize = 12.sp, color = t.accent)
                    Text(repo.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
                    Spacer(Modifier.height(6.dp))

                    // Stats row
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatStars(repo.stargazers_count), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                            Text("Stars", fontSize = 11.sp, color = t.textSecondary)
                        }
                        Box(Modifier.width(1.dp).height(30.dp).background(t.border))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatStars(repo.forks_count), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                            Text("Forks", fontSize = 11.sp, color = t.textSecondary)
                        }
                        Box(Modifier.width(1.dp).height(30.dp).background(t.border))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(repo.language ?: "—", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                            Text("Language", fontSize = 11.sp, color = t.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (installState.isInstalled) {
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(GreenOk.copy(0.15f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("✓  Installed", fontSize = 12.sp, color = GreenOk, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Description
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.border)
                    Spacer(Modifier.height(12.dp))
                    Text("About", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    val displayedDesc = translatedDesc ?: (repo.description ?: "No description available.")
                    Text(displayedDesc, fontSize = 14.sp, color = t.textSecondary, lineHeight = 22.sp)
                    Spacer(Modifier.height(8.dp))
                    if (repo.description != null && state.settings.language != "English") {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(t.accent.copy(0.08f))
                                .clickable(enabled = !isTranslating) { onTranslate() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isTranslating) CircularProgressIndicator(Modifier.size(12.dp), color = t.accent, strokeWidth = 1.5.dp)
                            else Icon(Icons.Rounded.Translate, null, tint = t.accent, modifier = Modifier.size(13.dp))
                            Text(if (translatedDesc != null) "Translated · Redo" else "Translate to ${state.settings.language}", fontSize = 12.sp, color = t.accent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Screenshots
            if (screenshots.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        HorizontalDivider(color = t.border, modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Screenshots", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = t.textPrimary, modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(10.dp))
                        ScreenshotsPager(urls = screenshots)
                    }
                }
            }

            // Release info
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.border)
                    Spacer(Modifier.height(12.dp))
                    Text("Release", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    Spacer(Modifier.height(8.dp))

                    when {
                        installState.isLoadingRelease -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(Modifier.size(16.dp), color = t.accent, strokeWidth = 2.dp)
                                Text("Checking latest release…", fontSize = 13.sp, color = t.textSecondary)
                            }
                        }
                        installState.error != null -> Text(installState.error, fontSize = 13.sp, color = RedDanger)
                        installState.release != null -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(t.bgSurface).padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(installState.release.tag_name.ifBlank { "Latest" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                                    Text("Stable release", fontSize = 11.sp, color = t.textSecondary)
                                }
                                if (installState.apkAsset != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatBytes(installState.apkAsset.size), fontSize = 12.sp, color = t.accent, fontWeight = FontWeight.SemiBold)
                                        Text("APK", fontSize = 11.sp, color = t.textSecondary)
                                    }
                                }
                            }

                            // Changelog
                            if (installState.release.body.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text("What's new", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    installState.release.body.take(500) + if (installState.release.body.length > 500) "…" else "",
                                    fontSize = 12.sp, color = t.textSecondary, lineHeight = 18.sp
                                )
                            }

                            // Ignore this version
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(t.bgSurfaceAlt)
                                    .clickable { onIgnoreVersion() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.VisibilityOff, null, tint = t.textSecondary, modifier = Modifier.size(14.dp))
                                Text("Skip this version", fontSize = 11.sp, color = t.textSecondary)
                            }

                            if (installState.apkAsset == null) {
                                Spacer(Modifier.height(8.dp))
                                Text("No APK found in this release.", fontSize = 12.sp, color = t.textSecondary)
                            }
                        }
                    }
                }
            }

            // Download progress
            if (installState.downloadProgress != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Downloading…", fontSize = 13.sp, color = t.textSecondary)
                            Text("${(installState.downloadProgress * 100).toInt()}%", fontSize = 13.sp, color = t.accent, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LinearProgressIndicator(progress = { installState.downloadProgress }, modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)), color = t.accent, trackColor = t.bgSurfaceAlt)
                            Box(Modifier.size(36.dp).clip(CircleShape).background(RedDanger.copy(0.12f)).clickable { onCancelDownload() }, Alignment.Center) {
                                Icon(Icons.Rounded.Close, "Cancel", tint = RedDanger, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Column(
                    modifier            = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = t.border)
                    Spacer(Modifier.height(4.dp))

                    val canInstall = installState.apkAsset != null && installState.downloadProgress == null && !installState.isInstalled

                    if (canInstall) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onDownloadOnly, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.5.dp, t.accent)) {
                                Icon(Icons.Rounded.Download, null, tint = t.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Download", color = t.accent, fontWeight = FontWeight.SemiBold)
                            }
                            Button(onClick = onInstall, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = t.accent)) {
                                Icon(Icons.Rounded.InstallMobile, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Install", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // View on GitHub
                    OutlinedButton(
                        onClick  = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.html_url))) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.5.dp, t.border)
                    )

                    {
                        Icon(Icons.Rounded.OpenInBrowser, null, tint = t.textPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View on GitHub", color = t.textPrimary, fontWeight = FontWeight.Medium)
                    }
                    // Compare button
                    OutlinedButton(
                        onClick  = onCompare,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.5.dp, t.border)
                    ) {
                        Icon(Icons.Rounded.CompareArrows, null, tint = t.textPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Compare with another app", color = t.textPrimary, fontWeight = FontWeight.Medium)
                    }

                    if (installState.isInstalled) {
                        Button(onClick = onUninstall, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedDanger)) {
                            Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Uninstall", fontWeight = FontWeight.Bold)
                        }
                        if (installState.apkAsset != null) {
                            OutlinedButton(onClick = onInstall, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.5.dp, t.accent)) {
                                Icon(Icons.Rounded.Refresh, null, tint = t.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Reinstall / Update", color = t.accent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // About author
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.border)
                    Spacer(Modifier.height(12.dp))
                    Text("About the Author", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(t.bgSurface).padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        AsyncImage(model = repo.owner.avatar_url, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(repo.owner.login, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                            Text("GitHub Developer", fontSize = 11.sp, color = t.textSecondary)
                        }
                        IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${repo.owner.login}"))) }) {
                            Icon(Icons.Rounded.OpenInBrowser, null, tint = t.accent)
                        }
                    }
                }
            }

            // ── Trust Score ───────────────────────────────────────────────────────
            installState.trustScore?.let { trust ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.border)
                        Spacer(Modifier.height(12.dp))
                        Text("Trust Score", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        TrustScoreBar(trust)
                    }
                }
            }

// ── Smart Install recommendation ──────────────────────────────────────
            installState.smartInstall?.let { smart ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (smart.isOptimal) GreenOk.copy(0.08f) else StarGold.copy(0.08f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Verified, null,
                                tint = if (smart.isOptimal) GreenOk else StarGold,
                                modifier = Modifier.size(20.dp))
                            Column {
                                Text("Best package for your device", fontSize = 11.sp, color = t.textSecondary)
                                Text(smart.reason, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (smart.isOptimal) GreenOk else StarGold)
                            }
                        }
                    }
                }
            }

// ── Developer Mode ────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.border)
                    Spacer(Modifier.height(12.dp))
                    Text("Developer Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(t.bgSurface)) {
                        listOf(
                            Triple(Icons.Rounded.Code,        "View Source Code",  repo.html_url),
                            Triple(Icons.Rounded.BugReport,   "Open Issues",       "${repo.html_url}/issues"),
                            Triple(Icons.Rounded.Description, "License",           "${repo.html_url}/blob/main/LICENSE"),
                            Triple(Icons.Rounded.History,     "Release History",   "${repo.html_url}/releases")
                        ).forEachIndexed { i, (icon, label, url) ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(icon, null, tint = t.accent, modifier = Modifier.size(18.dp))
                                Text(label, fontSize = 14.sp, color = t.textPrimary, modifier = Modifier.weight(1f))
                                Icon(Icons.Rounded.OpenInBrowser, null, tint = t.textSecondary, modifier = Modifier.size(14.dp))
                            }
                            if (i < 3) HorizontalDivider(color = t.border, modifier = Modifier.padding(horizontal = 14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SEE ALL SCREEN
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun SeeAllScreen(
    title     : String,
    apps      : List<GitHubRepo>,
    installed : Set<Long>     = emptySet(),
    isLoading : Boolean,
    onLoadMore: () -> Unit,
    onAppClick: (GitHubRepo) -> Unit,
    onBack    : () -> Unit
) {
    val t = LocalTheme.current

    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {
        Row(
            modifier = Modifier
                .fillMaxWidth().background(t.bgSurface)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = t.textPrimary)
            }
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
        }

        when {
            apps.isEmpty() && isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = t.accent)
                }
            }
            apps.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("⚠️", fontSize = 40.sp)
                        Text("Could not load apps", color = t.textSecondary, fontSize = 14.sp)
                        Button(onClick = onLoadMore, colors = ButtonDefaults.buttonColors(containerColor = t.accent)) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = apps) { repo ->
                        AppListTile(
                            repo        = repo,
                            isInstalled = installed.contains(repo.id),
                            onClick     = { onAppClick(repo) }
                        )
                    }
                    item {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = t.accent, modifier = Modifier.size(28.dp))
                            }
                        } else {
                            LaunchedEffect(Unit) { onLoadMore() }
                            Spacer(Modifier.height(110.dp))
                        }
                    }
                }
            }
        }
    }
}

fun formatStars(count: Int): String = if (count >= 1000) "${count / 1000}.${(count % 1000) / 100}k" else count.toString()
fun formatBytes(bytes: Long): String = when { bytes >= 1_000_000 -> "${bytes / 1_000_000} MB"; bytes >= 1_000 -> "${bytes / 1_000} KB"; else -> "$bytes B" }

// ─────────────────────────────────────────────────────────────────────────
// FLOATING NAV BAR
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun FloatingNavBar(
    selectedTab : AppTab,
    theme       : AppThemeColors,
    onTabSelect : (AppTab) -> Unit
) {
    data class NavItem(val tab: AppTab, val icon: ImageVector, val label: String)
    val items = listOf(
        NavItem(AppTab.HOME,      Icons.Rounded.Home,          "Home"),
        NavItem(AppTab.SEARCH,    Icons.Rounded.Search,        "Search"),
        NavItem(AppTab.INSTALLED, Icons.Rounded.InstallMobile, "Installed"),
        NavItem(AppTab.PROFILE,   Icons.Rounded.Person,        "Profile"),
        NavItem(AppTab.SETTINGS,  Icons.Rounded.Settings,      "Settings")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(0.84f)
            .clip(RoundedCornerShape(28.dp))
            .background(theme.dockBg)
            .border(1.dp, theme.border.copy(0.4f), RoundedCornerShape(28.dp))
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        items.forEach { (tab, icon, label) ->
            DockItem(
                icon    = icon,
                label   = label,
                active  = selectedTab == tab,
                t       = theme,
                onClick = { onTabSelect(tab) }
            )
        }
    }
}

fun detectPlatformLabels(repo: GitHubRepo): List<Pair<String, String>> {
    val text = "${repo.name} ${repo.description ?: ""}".lowercase()
    val platforms = mutableListOf<Pair<String, String>>()
    if ("android" in text || repo.language == "Kotlin" || repo.language == "Java")
        platforms.add("🤖" to "Android")
    if ("windows" in text)   platforms.add("🪟" to "Windows")
    if ("linux" in text)     platforms.add("🐧" to "Linux")
    if ("macos" in text || "mac os" in text) platforms.add("🍎" to "macOS")
    if ("ios" in text)       platforms.add("📱" to "iOS")
    if ("cross-platform" in text || "multiplatform" in text || "flutter" in text)
        platforms.add("🌐" to "Cross")
    if ("tv" in text || "television" in text) platforms.add("📺" to "TV")
    return platforms.distinct()
}
@Composable
fun AppCard(repo: GitHubRepo, isInstalled: Boolean = false, onClick: () -> Unit) {
    val cardColor         = CardColors[abs(repo.name.hashCode()) % CardColors.size]
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "cardScale"
    )

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(240.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Box(Modifier.fillMaxSize().background(cardColor.copy(alpha = 0.85f)))
        AsyncImage(
            model = repo.owner.avatar_url, contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.18f },
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(0.65f)),
                startY = 80f
            )
        ))
        // Small circular logo
        Box(
            modifier = Modifier.padding(8.dp).size(28.dp).clip(CircleShape)
                .background(Color.White.copy(0.25f))
                .border(1.dp, Color.White.copy(0.35f), CircleShape)
                .align(Alignment.TopStart)
        ) {
            AsyncImage(model = repo.owner.avatar_url, contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop)
        }
        // Text — pushed up from bottom with more room
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, end = 10.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                repo.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (!repo.description.isNullOrEmpty()) {
                Text(
                    repo.description, fontSize = 13.sp, color = Color.White.copy(0.88f),
                    maxLines = 3, lineHeight = 17.sp, overflow = TextOverflow.Ellipsis
                )
            }
            Text("★  ${formatStars(repo.stargazers_count)}", fontSize = 12.sp, color = StarGold)

            // ── Platform + Installed badges ──────────────────────────────
            val platformLabels = detectPlatformLabels(repo)
            if (platformLabels.isNotEmpty() || isInstalled) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    platformLabels.forEach { (emoji, label) ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("$emoji $label", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (isInstalled) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GreenOk.copy(alpha = 0.30f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("✓ Installed", fontSize = 10.sp, color = GreenOk, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }   // ← closes Column (BottomStart)
    }       // ← closes root Box
}           // ← closes AppCard function

// ── Trust Score bar ────────────

// ── Trust Score bar ────────────────────────────────────────────────────────
@Composable
fun TrustScoreBar(trust: TrustScore) {
    // ✅ use safeColor — no more crash
    val color = trust.safeColor
    val t = LocalTheme.current
    val textColor = if (t.isDark) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(t.bgSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Vyxel Trust Score", fontSize = 12.sp, color = if (t.isDark) Color.White else Color.Black)
                // ✅ label uses textColor instead of color
                Text(trust.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(0.15f))   // keep tint
                    .border(2.dp, color, CircleShape), // keep border color
                contentAlignment = Alignment.Center
            ) {
                // ✅ score number uses textColor instead of color
                Text(
                    "${trust.score}",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = textColor
                )
            }
        }

        // progress bar stays as color — decorative, fine
        LinearProgressIndicator(
            progress   = { trust.score / 100f },
            modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color      = color,
            trackColor = t.bgSurfaceAlt
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TrustChip(label = if (trust.daysSinceUpdate < 90) "Active" else "Inactive", ok = trust.daysSinceUpdate < 90)
            TrustChip(label = if (trust.stars >= 100) "${formatStars(trust.stars)} stars" else "Low stars", ok = trust.stars >= 100)
            TrustChip(label = if (trust.releaseCount > 0) "${trust.releaseCount} releases" else "No releases", ok = trust.releaseCount > 0)
        }
    }
}

@Composable
fun TrustChip(label: String, ok: Boolean) {
    val t = LocalTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (ok) GreenOk.copy(0.12f) else RedDanger.copy(0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            "${if (ok) "✓" else "✗"} $label",
            fontSize   = 10.sp,
            color      = if (ok) GreenOk else RedDanger,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Collections row ────────────────────────────────────────────────────────
@Composable
fun CollectionsRow(onCollectionClick: (AppCollection) -> Unit) {
    val t = LocalTheme.current
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            "✨ Curated Collections",
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = t.textPrimary,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = COLLECTIONS) { col ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(t.bgSurface)
                        .border(1.dp, t.border, RoundedCornerShape(16.dp))
                        .clickable { onCollectionClick(col) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(col.emoji, fontSize = 26.sp)
                    Text(
                        col.title,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = t.textPrimary,
                        maxLines   = 1
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ScreenshotsPager(urls: List<String>) {
    if (urls.isEmpty()) return
    val t = LocalTheme.current
    val pagerState = rememberPagerState(pageCount = { urls.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp),
            pageSpacing = 8.dp
        ) { page ->
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(t.bgSurface)) {
                AsyncImage(
                    model = urls[page], contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(urls.size) { i ->
                Box(modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(4.dp)
                    .width(if (i == pagerState.currentPage) 16.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i == pagerState.currentPage) t.accent else t.border))
            }
        }
    }
}