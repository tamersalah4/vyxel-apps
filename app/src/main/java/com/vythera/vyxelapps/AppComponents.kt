package com.vythera.vyxelapps

import android.content.Intent
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(24.dp),
        color  = Color.Black.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Image(
                painter            = painterResource(R.drawable.skpic),
                contentDescription = "Logo",
                modifier           = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Vyxel Apps",
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color      = Color.Black.copy(alpha = 0.5f),
                        offset     = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DOCK ITEM
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DockItem(
    icon    : ImageVector,
    label   : String,
    active  : Boolean,
    t       : AppThemeColors,
    onClick : () -> Unit
) {
    val inactiveColor = t.dockForeground.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) t.accent.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                label,
                tint     = if (active) t.accent else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize   = 9.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color      = if (active) t.accent else inactiveColor
            )
            Spacer(Modifier.height(2.dp))
            // Active indicator pill
            Box(
                Modifier
                    .size(width = if (active) 12.dp else 0.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (active) t.accent else Color.Transparent)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO BANNER  — unchanged logic, improved visual shape
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(apps: List<GitHubRepo>, onAppClick: (GitHubRepo) -> Unit) {
    if (apps.isEmpty()) return
    val t       = LocalTheme.current
    val context = LocalContext.current

    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val bannerApps = remember(apps) { apps.shuffled().take(7) }
    val pagerState = rememberPagerState(pageCount = { bannerApps.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            val next = (pagerState.currentPage + 1) % bannerApps.size
            pagerState.animateScrollToPage(next, animationSpec = tween(1400, easing = EaseInOutCubic))
        }
    }

    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            val repo = bannerApps[page]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.60f)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 2.dp,
                            bottomEnd = 32.dp
                        )
                    )   // M3 extraLarge shape
                    .clickable { onAppClick(repo) }
            ) {
                // Base surface colour (no vivid gradients — uses theme surface)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(t.bgSurfaceHigh)
                )
                // Owner avatar as subtle hero image
                AsyncImage(
                    model = remember(repo.owner.avatar_url) {
                        ImageRequest.Builder(context)
                            .data(repo.owner.avatar_url.ifEmpty { null })
                            .crossfade(400)
                            .build()
                    },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Scrim for text legibility
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    t.bgPrimary.copy(alpha = 0.5f),
                                    t.bgPrimary
                                ),
                                startY = 300f
                            )
                        )

                )
                // Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    // "FEATURED" badge — M3 primaryContainer style
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = t.accentContainer
                    ) {
                        Text(
                            "FEATURED",
                            fontSize = 10.sp,
                            color = t.onAccentContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        repo.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (!repo.description.isNullOrEmpty()) {
                        Text(
                            repo.description,
                            fontSize = 12.sp,
                            color = Color.White.copy(0.75f),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(bannerApps.size) { i ->
                val selected = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(4.dp)
                        .width(if (selected) 24.dp else 6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (selected) t.accent else t.borderVariant)
                )
            }
        }
    }
}



// ─────────────────────────────────────────────────────────────────────────────
// M3 TAG  — small surface chip, used for language / platform labels
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun M3Tag(
    label    : String,
    emoji    : String? = null,
    modifier : Modifier = Modifier
) {
    val t = LocalTheme.current
    Surface(
        shape  = RoundedCornerShape(6.dp),
        color  = t.bgSurfaceHigh,
        border = BorderStroke(0.5.dp, t.borderVariant),
        modifier = modifier
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (emoji != null) Text(emoji, fontSize = 10.sp)
            Text(
                label,
                fontSize   = 10.sp,
                color      = t.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APP CARD  — M3 Expressive unified surface (no vivid gradient)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppCard(repo: GitHubRepo, isInstalled: Boolean = false, onClick: () -> Unit) {
    val t       = LocalTheme.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "cardScale"
    )
    val platforms = remember(repo.id) { detectPlatformLabels(repo) }
    val imageModel = remember(repo.owner.avatar_url) {
        ImageRequest.Builder(context)
            .data(repo.owner.avatar_url.ifEmpty { null })
            .crossfade(300)
            .build()
    }
    Column(
        modifier = Modifier.width(140.dp), // Netflix poster width
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2. The Poster Tile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp) // Poster aspect ratio (approx 2:3)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(16.dp))
                .background(t.bgSurfaceHigh)
                .clickable(interactionSource = interactionSource, indication = null) { onClick() }
        ) {
            // Full-size background image
            AsyncImage(
                model              = imageModel,
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop,
                error              = painterResource(R.drawable.ic_android_logo),
                placeholder        = painterResource(R.drawable.ic_android_logo)
            )

            // Scrim: Darkens top and bottom slightly so icons/stars are visible
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(0.5f),
                                Color.Transparent,
                                Color.Black.copy(0.5f)
                            )
                        )
                    )
            )

            // 3. Stars on Top Left (hidden when 0)
            if (repo.stargazers_count > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.Star,
                        null,
                        tint = StarGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        formatStars(repo.stargazers_count),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Source badge
            SourceBadge(
                source   = repo.source,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            )

            // 4. Platform Icons/Emojis on Bottom Right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(1.0f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (platform in platforms) {
                    // If the platform has a custom logo, show the Icon
                    if (platform.iconRes != null) {
                        Icon(
                            painter = painterResource(platform.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp), // Matches the star size
                            tint = Color.Unspecified         // Keeps original colors
                        )
                    } else {
                        // Otherwise, fallback to the emoji text
                        Text(platform.emoji, fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // 5. App Name (External)
        Text(
            repo.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = t.textPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// APP LIST TILE — M3 surface card style (no per-tile accent color)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppListTile(
    repo        : GitHubRepo,
    isInstalled : Boolean = false,
    onClick     : () -> Unit
) {
    val t                 = LocalTheme.current
    val context           = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.98f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "tileScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(t.bgSurface)
            .border(0.5.dp, t.borderVariant, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Circular avatar
        AsyncImage(
            model              = remember(repo.owner.avatar_url) {
                ImageRequest.Builder(context)
                    .data(repo.owner.avatar_url.ifEmpty { null })
                    .crossfade(200)
                    .build()
            },
            contentDescription = null,
            modifier           = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(t.bgSurfaceAlt),
            contentScale       = ContentScale.Crop,
            error              = painterResource(R.drawable.ic_android_logo),
            placeholder        = painterResource(R.drawable.ic_android_logo)
        )

        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "@${repo.owner.login}",
                fontSize = 11.sp,
                color    = t.textSecondary.copy(alpha = 0.7f)
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
                if (repo.stargazers_count > 0) {
                    Icon(
                        Icons.Rounded.Star,
                        null,
                        tint     = StarGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        formatStars(repo.stargazers_count),
                        fontSize = 11.sp,
                        color    = t.textSecondary
                    )
                }
                if (!repo.language.isNullOrEmpty()) {
                    Text("·", fontSize = 11.sp, color = t.textSecondary)
                    Text(repo.language, fontSize = 11.sp, color = t.textSecondary)
                }
                if (isInstalled) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GreenOk.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "Installed",
                            fontSize   = 10.sp,
                            color      = GreenOk,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                if (repo.source != null && repo.source != AppSource.GITHUB) {
                    SourceBadge(source = repo.source)
                }
            }
        }

        Icon(
            Icons.Rounded.ChevronRight,
            null,
            tint     = t.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APP ROW — horizontal scroll of AppCards
// ─────────────────────────────────────────────────────────────────────────────
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

    val displayList  = remember(apps, refreshToken) { apps.shuffled() }
    val useInfinite  = displayList.size >= 4
    // Keep virtual count small (≤2000) so LazyRow position maths stay fast
    val virtualCount = if (useInfinite) (displayList.size * 80).coerceAtMost(2_000) else displayList.size
    val startIndex   = remember(displayList.size, useInfinite) {
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
            fontWeight = FontWeight.SemiBold,
            color      = t.textPrimary,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            state                 = rowState,
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = virtualCount,
                key   = { idx -> "${displayList[idx % displayList.size].id}_$idx" }
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

// ─────────────────────────────────────────────────────────────────────────────
// PLATFORM GRID — for filtered views
// ─────────────────────────────────────────────────────────────────────────────
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
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(platform.emoji, fontSize = 20.sp)
            Text(
                "${platform.label} Apps",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = t.textPrimary
            )
        }
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            apps.forEach { repo ->
                AppListTile(
                    repo        = repo,
                    isInstalled = installed.contains(repo.id),
                    onClick     = { onAppClick(repo) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEARCH SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query                : String,
    results              : List<GitHubRepo>,
    platform             : AppPlatform,
    selectedSubCategories: Set<String>,
    installed            : Set<Long>        = emptySet(),
    suggestions          : List<GitHubRepo> = emptyList(),
    isSearching          : Boolean          = false,
    onQueryChange        : (String) -> Unit,
    onPlatformChange     : (AppPlatform) -> Unit,
    onSubCategoryToggle  : (String) -> Unit,
    onAppClick           : (GitHubRepo) -> Unit,
    isFilterMenuOpen     : Boolean,
    activeSubMenuPlatform: AppPlatform?,
    onToggleFilterMenu   : (Boolean) -> Unit,
    onSetSubMenuPlatform : (AppPlatform?) -> Unit
) {
    val t = LocalTheme.current
    val s = LocalStrings.current
    var selectedSource by remember { mutableStateOf<AppSource?>(null) }
    val displayResults = when {
        selectedSource == null -> results
        // GitHub repos loaded from the API have source=null (Gson ignores Kotlin defaults)
        selectedSource == AppSource.GITHUB -> results.filter { it.source == null || it.source == AppSource.GITHUB }
        else -> results.filter { it.source == selectedSource }
    }

    // ✅ Single root Column for the whole screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bgPrimary)
    ) {

        // ── TOP: Search bar ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.bgSurface)
                .statusBarsPadding()
        ) {
            TextField(
                value         = query,
                onValueChange = onQueryChange,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(28.dp)),
                placeholder   = { Text(s.searchHint, color = t.textSecondary) },
                singleLine    = true,
                leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = t.textSecondary) },
                trailingIcon  = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Rounded.Clear, null, tint = t.textSecondary)
                            }
                        }

                        // ✅ Box anchors both DropdownMenus to the filter button
                        Box {
                            IconButton(onClick = { onToggleFilterMenu(true) }) {
                                Icon(
                                    painter           = painterResource(R.drawable.ic_filter_logo),
                                    contentDescription = "Toggle Filters",
                                    modifier           = Modifier.size(22.dp),
                                    tint               = if (platform != AppPlatform.ALL || selectedSubCategories.isNotEmpty())
                                        t.accent else t.textPrimary
                                )
                            }

                            // ── Level 1: Platform picker ─────────────────────
                            DropdownMenu(
                                expanded         = isFilterMenuOpen,
                                onDismissRequest = { onToggleFilterMenu(false) },
                                containerColor   = t.bgSurfaceHigh
                            ) {
                                listOf(AppPlatform.ANDROID, AppPlatform.WINDOWS, AppPlatform.LINUX, AppPlatform.IOS, AppPlatform.TV).forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (p.iconRes != null) {
                                                    Icon(
                                                        painter           = painterResource(p.iconRes),
                                                        contentDescription = null,
                                                        modifier           = Modifier
                                                            .size(20.dp)
                                                            .padding(end = 8.dp),
                                                        tint               = Color.Unspecified
                                                    )
                                                } else {
                                                    Text(p.emoji, modifier = Modifier.padding(end = 8.dp))
                                                }
                                                Text(
                                                    p.label,
                                                    color      = t.textPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(Modifier.weight(1f))
                                                Icon(
                                                    Icons.Rounded.ChevronRight,
                                                    null,
                                                    tint     = t.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onPlatformChange(p)
                                            onToggleFilterMenu(false)
                                        }
                                    )
                                }
                            }

                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = t.bgSurfaceAlt,
                    unfocusedContainerColor = t.bgSurfaceAlt,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = t.textPrimary,
                    unfocusedTextColor      = t.textPrimary
                )
            )
            // Source filter chips
            val sourcePairs = remember {
                listOf<Pair<AppSource?, String>>(
                    null               to "All Sources",
                    AppSource.GITHUB   to "GitHub",
                    AppSource.FDROID   to "F-Droid",
                    AppSource.GITLAB   to "GitLab",
                    AppSource.CODEBERG to "Codeberg",
                    AppSource.FLATHUB  to "Flathub"
                )
            }
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items = sourcePairs, key = { it.second }) { pair ->
                    val (src, label) = pair
                    val isSelected = selectedSource == src
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedSource = src },
                        shape    = RoundedCornerShape(20.dp),
                        color    = if (isSelected) t.accent.copy(alpha = 0.18f) else t.bgSurfaceAlt,
                        border   = BorderStroke(0.5.dp, if (isSelected) t.accent else t.border)
                    ) {
                        Text(
                            label,
                            fontSize   = 12.sp,
                            color      = if (isSelected) t.accent else t.textSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ── BOTTOM: Results list ─────────────────────────────────────────────
        LazyColumn(
            contentPadding      = PaddingValues(
                start  = 16.dp, end    = 16.dp,
                top    = 16.dp, bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (query.isBlank() && platform == AppPlatform.ALL) {
                if (suggestions.isNotEmpty()) {
                    item {
                        Text(
                            s.trendingApps,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = t.textSecondary,
                            modifier   = Modifier.padding(bottom = 4.dp, top = 8.dp)
                        )
                    }
                    items(items = suggestions) { repo ->
                        AppListTile(
                            repo        = repo,
                            isInstalled = installed.contains(repo.id),
                            onClick     = { onAppClick(repo) }
                        )
                    }
                }
            } else {
                if (isSearching && displayResults.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color       = t.accent,
                                    modifier    = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(s.searching, color = t.textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(items = displayResults) { repo ->
                        AppListTile(
                            repo        = repo,
                            isInstalled = installed.contains(repo.id),
                            onClick     = { onAppClick(repo) }
                        )
                    }
                    if (displayResults.isNotEmpty() && isSearching) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color       = t.accent,
                                        modifier    = Modifier.size(14.dp),
                                        strokeWidth = 1.5.dp
                                    )
                                    Text(s.fetchingMore, fontSize = 12.sp, color = t.textSecondary)
                                }
                            }
                        }
                    }
                    if (displayResults.isEmpty() && !isSearching) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.SearchOff,
                                        null,
                                        tint     = t.textSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    val filterText = if (platform != AppPlatform.ALL) " — ${platform.label}" else ""
                                    Text(
                                        "${s.noResultsFound}$filterText",
                                        color     = t.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        s.tryDifferent,
                                        fontSize = 12.sp,
                                        color    = t.textSecondary.copy(0.6f)
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



// ─────────────────────────────────────────────────────────────────────────────
// INSTALLED SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun InstalledScreen(
    installHistory    : List<InstallHistoryEntry>,
    installStates     : Map<Long, InstallState>,
    updates           : List<UpdateInfo>        = emptyList(),
    onAppClick        : (GitHubRepo) -> Unit,
    onCheckUpdates    : () -> Unit              = {},
    isCheckingUpdates : Boolean                 = false
) {
    val t = LocalTheme.current
    val s = LocalStrings.current

    // One card per app — latest install entry wins
    val entries = remember(installHistory) {
        installHistory
            .groupBy { it.repoId }
            .mapValues { (_, v) -> v.maxByOrNull { it.installedAt }!! }
            .values
            .sortedBy { it.repoName }
    }

    val updateCount = updates.count { u -> entries.any { it.repoId == u.repoId } }

    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.bgSurface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(s.navInstalled, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    Text(
                        when {
                            entries.isEmpty()   -> "No apps installed through Vyxel yet"
                            updateCount > 0     -> "$updateCount update${if (updateCount != 1) "s" else ""} available"
                            else                -> "${entries.size} app${if (entries.size != 1) "s" else ""} installed"
                        },
                        fontSize = 12.sp,
                        color    = if (updateCount > 0) t.accent else t.textSecondary
                    )
                }
                IconButton(onClick = { if (!isCheckingUpdates) onCheckUpdates() }) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = t.accent, strokeWidth = 2.dp)
                    } else {
                        BadgedBox(
                            badge = {
                                if (updateCount > 0) Badge(containerColor = t.accent) {
                                    Text("$updateCount", fontSize = 9.sp, color = t.bgPrimary)
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.SystemUpdate, "Check updates", tint = t.accent)
                        }
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.InstallMobile, null, tint = t.accent, modifier = Modifier.size(64.dp))
                    Text(s.nothingInstalled, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = t.textPrimary)
                    Text(
                        s.nothingInstalledDesc,
                        color = t.textSecondary, fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (updateCount > 0) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            color    = t.accent.copy(alpha = 0.12f),
                            border   = BorderStroke(1.dp, t.accent.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.NewReleases, null, tint = t.accent, modifier = Modifier.size(18.dp))
                                    Text(
                                        "$updateCount update${if (updateCount != 1) "s" else ""} available",
                                        color = t.accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                                    )
                                }
                                if (isCheckingUpdates) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = t.accent, strokeWidth = 2.dp)
                                } else {
                                    TextButton(onClick = onCheckUpdates) {
                                        Text("Refresh", color = t.accent, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                items(items = entries, key = { it.repoId }) { entry ->
                    val iState  = installStates[entry.repoId]
                    val repo    = iState?.repo ?: GitHubRepo(
                        id        = entry.repoId,
                        name      = entry.repoName,
                        full_name = "${entry.ownerLogin}/${entry.repoName}",
                        owner     = RepoOwner(login = entry.ownerLogin)
                    )
                    val update  = updates.firstOrNull { it.repoId == entry.repoId }
                    val isStillInstalled = iState?.isInstalled == true

                    InstalledAppCard(
                        repo             = repo,
                        entry            = entry,
                        isInstalled      = isStillInstalled,
                        update           = update,
                        onClick          = { onAppClick(repo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstalledAppCard(
    repo        : GitHubRepo,
    entry       : InstallHistoryEntry,
    isInstalled : Boolean,
    update      : UpdateInfo?,
    onClick     : () -> Unit
) {
    val t = LocalTheme.current
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(14.dp),
        color    = t.bgSurface,
        border   = if (update != null) BorderStroke(1.dp, t.accent.copy(alpha = 0.5f))
                   else BorderStroke(0.5.dp, t.borderVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model              = repo.owner.avatar_url.ifEmpty { null },
                    contentDescription = null,
                    modifier           = Modifier.size(40.dp).clip(CircleShape),
                    error              = painterResource(R.drawable.ic_android_logo),
                    placeholder        = painterResource(R.drawable.ic_android_logo)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(repo.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = t.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(repo.owner.login, fontSize = 12.sp, color = t.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                // Status chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isInstalled) Color(0xFF1DB954).copy(alpha = 0.15f) else t.bgSurfaceAlt
                ) {
                    Text(
                        if (isInstalled) s.installedStatus else s.removedStatus,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isInstalled) Color(0xFF1DB954) else t.textSecondary,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("v${entry.tagName.trimStart('v', 'V')}", fontSize = 11.sp, color = t.textSecondary)
                if (update != null) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = t.accent, modifier = Modifier.size(12.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = t.accent.copy(alpha = 0.15f)) {
                        Text(
                            update.latestTag,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = t.accent,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                val fmt = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                Text(fmt.format(java.util.Date(entry.installedAt)), fontSize = 11.sp, color = t.textSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APP DETAIL SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppDetailScreen(
    repo             : GitHubRepo,
    installState     : InstallState,
    isFavourite      : Boolean,
    translatedDesc   : String?,
    isTranslating    : Boolean,
    state            : UiState,
    screenshots      : List<String>  = emptyList(),
    onInstall        : () -> Unit,
    onDownloadOnly   : () -> Unit,
    onUninstall      : () -> Unit,
    onCancelDownload : () -> Unit,
    onTranslate      : () -> Unit,
    onToggleFavourite: () -> Unit,
    onIgnoreVersion  : () -> Unit    = {},
    onCompare        : () -> Unit    = {},
    onBack           : () -> Unit
) {
    val t       = LocalTheme.current
    val context = LocalContext.current
    val s = LocalStrings.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(t.bgPrimary)) {
        // Top bar
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(t.bgSurface)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = t.textPrimary)
            }
            Text(
                repo.name,
                fontSize  = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = t.textPrimary,
                modifier   = Modifier.weight(1f),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            IconButton(onClick = onToggleFavourite) {
                Icon(
                    if (isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    "Favourite",
                    tint = if (isFavourite) RedDanger else t.textSecondary
                )
            }
        }

        LazyColumn(
            contentPadding      = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Hero (subtle — no vivid gradient)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(Modifier
                        .fillMaxSize()
                        .background(t.bgSurfaceHigh))
                    AsyncImage(
                        model              = repo.owner.avatar_url,
                        contentDescription = null,
                        modifier           = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = 0.20f },
                        contentScale       = ContentScale.Crop
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, t.bgPrimary),
                                    startY = 80f
                                )
                            )
                    )
                }
            }

            // App info
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("@${repo.owner.login}", fontSize = 12.sp, color = t.accent)
                    Text(
                        repo.name,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = t.textPrimary
                    )
                    Spacer(Modifier.height(6.dp))

                    // Stats row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                formatStars(repo.stargazers_count),
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = t.textPrimary
                            )
                            Text(s.stars, fontSize = 11.sp, color = t.textSecondary)
                        }
                        Box(Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(t.borderVariant))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                formatStars(repo.forks_count),
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = t.textPrimary
                            )
                            Text(s.forks, fontSize = 11.sp, color = t.textSecondary)
                        }
                        Box(Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(t.borderVariant))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                repo.language ?: "—",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = t.textPrimary
                            )
                            Text(s.codingLanguage, fontSize = 11.sp, color = t.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (installState.isInstalled) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = GreenOk.copy(0.15f)
                        ) {
                            Text(
                                s.installedBadge,
                                fontSize   = 12.sp,
                                color      = GreenOk,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Description
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.borderVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(s.about, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = t.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    val displayedDesc = translatedDesc ?: (repo.description ?: s.noDescAvailable)
                    Text(displayedDesc, fontSize = 14.sp, color = t.textSecondary, lineHeight = 22.sp)
                    Spacer(Modifier.height(8.dp))
                    if (repo.description != null && state.settings.language != "English") {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(enabled = !isTranslating) { onTranslate() },
                            shape = RoundedCornerShape(20.dp),
                            color = t.accentContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (isTranslating) CircularProgressIndicator(
                                    Modifier.size(12.dp), color = t.accent, strokeWidth = 1.5.dp
                                )
                                else Icon(
                                    Icons.Rounded.Translate,
                                    null,
                                    tint     = t.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    if (translatedDesc != null) s.translatedRedo
                                    else "${s.translateTo} ${state.settings.language}",
                                    fontSize   = 12.sp,
                                    color      = t.accent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Screenshots
            if (screenshots.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        HorizontalDivider(
                            color    = t.borderVariant,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            s.screenshots,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = t.textPrimary,
                            modifier   = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        ScreenshotsPager(urls = screenshots)
                    }
                }
            }

            // Release info
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.borderVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.release,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = t.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))

                    when {
                        installState.isLoadingRelease -> {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    Modifier.size(16.dp),
                                    color       = t.accent,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    s.checkingRelease,
                                    fontSize = 13.sp,
                                    color    = t.textSecondary
                                )
                            }
                        }
                        installState.error != null -> Text(
                            installState.error,
                            fontSize = 13.sp,
                            color    = RedDanger
                        )
                        installState.release != null -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = t.bgSurface,
                                border = BorderStroke(0.5.dp, t.borderVariant)
                            ) {
                                Row(
                                    modifier              = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            installState.release.tag_name.ifBlank { "Latest" },
                                            fontSize   = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = t.textPrimary
                                        )
                                        Text(s.stableRelease, fontSize = 11.sp, color = t.textSecondary)
                                    }
                                    if (installState.apkAsset != null) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                formatBytes(installState.apkAsset.size),
                                                fontSize   = 12.sp,
                                                color      = t.accent,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text("APK", fontSize = 11.sp, color = t.textSecondary)
                                        }
                                    }
                                }
                            }

                            if (installState.release.body.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    s.whatsNew,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = t.textPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    installState.release.body.take(500) +
                                            if (installState.release.body.length > 500) "…" else "",
                                    fontSize   = 12.sp,
                                    color      = t.textSecondary,
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onIgnoreVersion() },
                                shape = RoundedCornerShape(8.dp),
                                color = t.bgSurfaceAlt
                            ) {
                                Row(
                                    modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.VisibilityOff,
                                        null,
                                        tint     = t.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(s.skipVersion, fontSize = 11.sp, color = t.textSecondary)
                                }
                            }

                            if (installState.apkAsset == null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    s.noApkFound,
                                    fontSize = 12.sp,
                                    color    = t.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Download progress
            if (installState.downloadProgress != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(s.downloading, fontSize = 13.sp, color = t.textSecondary)
                            Text(
                                "${(installState.downloadProgress * 100).toInt()}%",
                                fontSize   = 13.sp,
                                color      = t.accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LinearProgressIndicator(
                                progress  = { installState.downloadProgress },
                                modifier  = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp)),
                                color      = t.accent,
                                trackColor = t.bgSurfaceAlt
                            )
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RedDanger.copy(0.12f))
                                    .clickable { onCancelDownload() },
                                Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    "Cancel",
                                    tint     = RedDanger,
                                    modifier = Modifier.size(18.dp)
                                )
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
                    HorizontalDivider(color = t.borderVariant)
                    Spacer(Modifier.height(4.dp))

                    val canInstall = installState.apkAsset != null &&
                            installState.downloadProgress == null &&
                            !installState.isInstalled

                    if (canInstall) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick  = onDownloadOnly,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.5.dp, t.accent)
                            ) {
                                Icon(
                                    Icons.Rounded.Download,
                                    null,
                                    tint     = t.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(s.download, color = t.accent, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick  = onInstall,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = t.accent)
                            ) {
                                Icon(Icons.Rounded.InstallMobile, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.install, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val viewUrl = when {
                        repo.html_url.isNotBlank() -> repo.html_url
                        repo.source == AppSource.FDROID   -> "https://f-droid.org/packages/${repo.full_name}/"
                        repo.source == AppSource.FLATHUB  -> "https://flathub.org/apps/${repo.full_name}"
                        repo.source == AppSource.WINGET   -> "https://winget.run/pkg/${repo.full_name}"
                        else -> ""
                    }
                    if (viewUrl.isNotBlank()) {
                        val sourceLabel = when (repo.source) {
                            AppSource.GITLAB   -> "GitLab"
                            AppSource.CODEBERG -> "Codeberg"
                            AppSource.FDROID   -> "F-Droid"
                            AppSource.IZZY     -> "IzzyOnDroid"
                            AppSource.FLATHUB  -> "Flathub"
                            AppSource.WINGET   -> "Winget"
                            else               -> "GitHub"
                        }
                        OutlinedButton(
                            onClick  = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewUrl)))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape    = RoundedCornerShape(12.dp),
                            border   = BorderStroke(1.5.dp, t.borderVariant)
                        ) {
                            Icon(
                                Icons.Rounded.OpenInBrowser,
                                null,
                                tint     = t.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("View on $sourceLabel", color = t.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick  = onCompare,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.5.dp, t.borderVariant)
                    ) {
                        Icon(
                            Icons.Rounded.CompareArrows,
                            null,
                            tint     = t.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Compare with another app", color = t.textPrimary, fontWeight = FontWeight.Medium)
                    }

                    if (installState.isInstalled) {
                        Button(
                            onClick  = onUninstall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = RedDanger)
                        ) {
                            Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s.uninstall, fontWeight = FontWeight.Bold)
                        }
                        if (installState.apkAsset != null) {
                            OutlinedButton(
                                onClick  = onInstall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.5.dp, t.accent)
                            ) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    null,
                                    tint     = t.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Reinstall / Update", color = t.accent, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // About author
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.borderVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "About the Author",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = t.textPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape  = RoundedCornerShape(14.dp),
                        color  = t.bgSurface,
                        border = BorderStroke(0.5.dp, t.borderVariant)
                    ) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model              = repo.owner.avatar_url,
                                contentDescription = null,
                                modifier           = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentScale       = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    repo.owner.login,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = t.textPrimary
                                )
                                val devLabel = when (repo.source) {
                                    AppSource.GITLAB   -> "GitLab Developer"
                                    AppSource.CODEBERG -> "Codeberg Developer"
                                    AppSource.FDROID   -> "F-Droid Developer"
                                    AppSource.IZZY     -> "IzzyOnDroid Developer"
                                    AppSource.FLATHUB  -> "Flathub Developer"
                                    AppSource.WINGET   -> "Winget Developer"
                                    else               -> "GitHub Developer"
                                }
                                Text(devLabel, fontSize = 11.sp, color = t.textSecondary)
                            }
                            IconButton(onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/${repo.owner.login}"))
                                )
                            }) {
                                Icon(Icons.Rounded.OpenInBrowser, null, tint = t.accent)
                            }
                        }
                    }
                }
            }

            // Trust Score
            installState.trustScore?.let { trust ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.borderVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Trust Score",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = t.textPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        TrustScoreBar(trust)
                    }
                }
            }

            // Smart Install recommendation
            installState.smartInstall?.let { smart ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (smart.isOptimal) GreenOk.copy(0.08f) else StarGold.copy(0.08f),
                            border = BorderStroke(
                                0.5.dp,
                                if (smart.isOptimal) GreenOk.copy(0.3f) else StarGold.copy(0.3f)
                            )
                        ) {
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Verified,
                                    null,
                                    tint     = if (smart.isOptimal) GreenOk else StarGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        "Best package for your device",
                                        fontSize = 11.sp,
                                        color    = t.textSecondary
                                    )
                                    Text(
                                        smart.reason,
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = if (smart.isOptimal) GreenOk else StarGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Developer Mode
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = t.borderVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Developer Mode",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = t.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape  = RoundedCornerShape(14.dp),
                        color  = t.bgSurface,
                        border = BorderStroke(0.5.dp, t.borderVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val baseUrl = when {
                                repo.html_url.isNotBlank() -> repo.html_url
                                repo.source == AppSource.FDROID  -> "https://f-droid.org/packages/${repo.full_name}/"
                                repo.source == AppSource.FLATHUB -> "https://flathub.org/apps/${repo.full_name}"
                                else -> ""
                            }
                            listOf(
                                Triple(Icons.Rounded.Code,        "View Source Code", baseUrl),
                                Triple(Icons.Rounded.BugReport,   "Open Issues",      if (baseUrl.isNotBlank()) "$baseUrl/issues" else ""),
                                Triple(Icons.Rounded.Description, "License",          if (baseUrl.isNotBlank()) "$baseUrl/blob/main/LICENSE" else ""),
                                Triple(Icons.Rounded.History,     "Release History",  if (baseUrl.isNotBlank()) "$baseUrl/releases" else "")
                            ).forEachIndexed { i, (icon, label, url) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (url.isNotBlank()) Modifier.clickable {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            } else Modifier
                                        )
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        icon,
                                        null,
                                        tint     = t.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        label,
                                        fontSize = 14.sp,
                                        color    = t.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Rounded.OpenInBrowser,
                                        null,
                                        tint     = t.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                if (i < 3) HorizontalDivider(
                                    color    = t.borderVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEE ALL SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SeeAllScreen(
    title     : String,
    apps      : List<GitHubRepo>,
    installed : Set<Long>    = emptySet(),
    isLoading : Boolean,
    onLoadMore: () -> Unit,
    onAppClick: (GitHubRepo) -> Unit,
    onBack    : () -> Unit
) {
    val t = LocalTheme.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(t.bgPrimary)) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(t.bgSurface)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = t.textPrimary)
            }
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
        }

        when {
            apps.isEmpty() && isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = t.accent)
                }
            }
            apps.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Text("Could not load apps", color = t.textSecondary, fontSize = 14.sp)
                        Button(
                            onClick = onLoadMore,
                            colors  = ButtonDefaults.buttonColors(containerColor = t.accent)
                        ) { Text("Retry") }
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
                            Box(
                                modifier        = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color       = t.accent,
                                    modifier    = Modifier.size(28.dp)
                                )
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

// ─────────────────────────────────────────────────────────────────────────────
// TRUST SCORE BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TrustScoreBar(trust: TrustScore) {
    val color     = trust.safeColor
    val t         = LocalTheme.current
    val textColor = t.textPrimary

    Surface(
        shape  = RoundedCornerShape(14.dp),
        color  = t.bgSurface,
        border = BorderStroke(0.5.dp, t.borderVariant)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Vyxel Trust Score",
                        fontSize = 12.sp,
                        color    = t.textSecondary
                    )
                    Text(
                        trust.label,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = textColor
                    )
                }
                Box(
                    modifier         = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(color.copy(0.12f))
                        .border(2.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${trust.score}",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = textColor
                    )
                }
            }

            LinearProgressIndicator(
                progress   = { trust.score / 100f },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color      = color,
                trackColor = t.bgSurfaceAlt
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrustChip(
                    label = if (trust.daysSinceUpdate < 90) "Active" else "Inactive",
                    ok    = trust.daysSinceUpdate < 90
                )
                TrustChip(
                    label = if (trust.stars >= 100) "${formatStars(trust.stars)} stars" else "Low stars",
                    ok    = trust.stars >= 100
                )
                TrustChip(
                    label = if (trust.releaseCount > 0) "${trust.releaseCount} releases" else "No releases",
                    ok    = trust.releaseCount > 0
                )
            }
        }
    }
}

@Composable
fun TrustChip(label: String, ok: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (ok) GreenOk.copy(0.12f) else RedDanger.copy(0.12f)
    ) {
        Text(
            "${if (ok) "✓" else "✗"} $label",
            fontSize   = 10.sp,
            color      = if (ok) GreenOk else RedDanger,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SOURCES ROW  — horizontal browsable source tiles
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SourcesRow(
    gitlabCount   : Int,
    codebergCount : Int,
    fdroidCount   : Int,
    flathubCount  : Int,
    wingetCount   : Int,
    onSourceClick : (AppSource) -> Unit
) {
    val t = LocalTheme.current
    val sources = listOf(
        Triple(AppSource.GITHUB,   "🐙", -1),
        Triple(AppSource.FDROID,   "🤖", fdroidCount),
        Triple(AppSource.GITLAB,   "🦊", gitlabCount),
        Triple(AppSource.CODEBERG, "🌲", codebergCount),
        Triple(AppSource.FLATHUB,  "📥", flathubCount),
        Triple(AppSource.WINGET,   "🪟", wingetCount)
    )

    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            "Browse by Source",
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = t.textPrimary,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = sources) { (source, emoji, count) ->
                Surface(
                    modifier = Modifier
                        .width(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSourceClick(source) },
                    shape  = RoundedCornerShape(16.dp),
                    color  = Color(source.colorHex).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, Color(source.colorHex).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier            = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (source) {
                            AppSource.FDROID -> Image(
                                painter            = painterResource(R.drawable.ic_android_logo1),
                                contentDescription = null,
                                modifier           = Modifier.size(32.dp)
                            )
                            AppSource.GITHUB -> Text("🐙", fontSize = 28.sp)
                            else             -> Text(emoji, fontSize = 28.sp)
                        }
                        Text(
                            source.label,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = t.textPrimary
                        )
                        Text(
                            when {
                                count < 0  -> "Open Source"
                                count == 0 -> "Browse"
                                else       -> "$count apps"
                            },
                            fontSize = 10.sp,
                            color    = t.textSecondary
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE BADGE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SourceBadge(source: AppSource?, modifier: Modifier = Modifier) {
    val s = source ?: return
    if (s == AppSource.GITHUB) return
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(4.dp),
        color    = Color(s.colorHex)
    ) {
        Text(
            s.label,
            fontSize   = 8.sp,
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FULL-SCREEN IMAGE VIEWER
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    urls         : List<String>,
    initialIndex : Int,
    onDismiss    : () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties       = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows  = false
        )
    ) {
        val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { urls.size })
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
        ) {
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model              = urls[page],
                    contentDescription = null,
                    modifier           = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentScale       = ContentScale.Fit
                )
            }

            // Page indicator
            Row(
                modifier              = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(urls.size) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(4.dp)
                            .width(if (i == pagerState.currentPage) 20.dp else 6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i == pagerState.currentPage) Color.White else Color.White.copy(0.4f))
                    )
                }
            }

            // Close button
            Surface(
                modifier  = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .clip(CircleShape)
                    .clickable { onDismiss() },
                shape = CircleShape,
                color = Color.White.copy(0.15f)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    "Close",
                    tint     = Color.White,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COLLECTIONS ROW  — M3 Expressive: icon + title + subtitle tiles
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CollectionsRow(onCollectionClick: (AppCollection) -> Unit) {
    val t = LocalTheme.current
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            "Curated Collections",
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = t.textPrimary,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = COLLECTIONS) { col ->
                // 1. Root Column to hold both Tile and Text
                Column(
                    modifier = Modifier.width(110.dp), // Set a fixed width for the whole item
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 2. The Tile (Surface) - Now only contains the Emoji
                    Surface(
                        modifier = Modifier
                            .size(90.dp) // Square tile size
                            .clickable { onCollectionClick(col) },
                        shape  = RoundedCornerShape(24.dp),
                        color  = t.bgSurfaceAlt,
                        border = BorderStroke(0.5.dp, t.accent.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                col.emoji,
                                fontSize = 48.sp, // Large emoji to fill the tile
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Labels placed below the tile
                    Text(
                        col.title,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = t.textPrimary,
                        maxLines   = 1,
                        textAlign  = TextAlign.Center
                    )
                    Text(
                        col.subtitle,
                        fontSize   = 10.sp,
                        color      = t.textSecondary,
                        maxLines   = 1,
                        lineHeight = 14.sp,
                        textAlign  = TextAlign.Center,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
            }
    } }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCREENSHOTS PAGER
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ScreenshotsPager(urls: List<String>) {
    if (urls.isEmpty()) return
    val t          = LocalTheme.current
    val pagerState = rememberPagerState(pageCount = { urls.size })
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    Column {
        HorizontalPager(
            state       = pagerState,
            modifier    = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 16.dp),
            pageSpacing = 8.dp
        ) { page ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fullscreenIndex = page },
                shape    = RoundedCornerShape(14.dp),
                color    = t.bgSurface
            ) {
                AsyncImage(
                    model              = urls[page],
                    contentDescription = null,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Fit
                )
            }
        }
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(urls.size) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(4.dp)
                        .width(if (i == pagerState.currentPage) 16.dp else 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i == pagerState.currentPage) t.accent else t.borderVariant)
                )
            }
        }
    }

    fullscreenIndex?.let { idx ->
        FullScreenImageViewer(
            urls         = urls,
            initialIndex = idx,
            onDismiss    = { fullscreenIndex = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FLOATING NAV BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FloatingNavBar(
    selectedTab  : VAppTab,
    theme        : AppThemeColors,
    onTabSelect  : (VAppTab) -> Unit,
    updateCount  : Int = 0
) {
    val s = LocalStrings.current
    data class NavItem(val tab: VAppTab, val icon: ImageVector, val label: String)
    val items = listOf(
        NavItem(VAppTab.HOME,      Icons.Rounded.Home,          s.navHome),
        NavItem(VAppTab.SEARCH,    Icons.Rounded.Search,        s.navSearch),
        NavItem(VAppTab.INSTALLED, Icons.Rounded.InstallMobile, s.navInstalled),
        NavItem(VAppTab.PROFILE,   Icons.Rounded.Person,        s.navProfile),
        NavItem(VAppTab.SETTINGS,  Icons.Rounded.Settings,      s.navSettings)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(0.88f),
        shape    = RoundedCornerShape(28.dp),
        color    = theme.dockBg,
        border   = BorderStroke(0.5.dp, theme.borderVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            items.forEach { (tab, icon, label) ->
                val badge = if (tab == VAppTab.INSTALLED && updateCount > 0) updateCount else 0
                BadgedBox(
                    badge = {
                        if (badge > 0) Badge(containerColor = theme.accent) {
                            Text("$badge", fontSize = 8.sp, color = theme.bgPrimary)
                        }
                    }
                ) {
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
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
fun detectPlatformLabels(repo: GitHubRepo): List<AppPlatform> {
    val text      = "${repo.name} ${repo.description ?: ""}".lowercase()
    val platforms = mutableListOf<AppPlatform>()

    // Check keywords and add the actual AppPlatform enum objects
    if ("android" in text || repo.language == "Kotlin" || repo.language == "Java")
        platforms.add(AppPlatform.ANDROID)
    if ("windows" in text)
        platforms.add(AppPlatform.WINDOWS)
    if ("linux" in text)
        platforms.add(AppPlatform.LINUX)
    if ("macos" in text || "mac os" in text)
        platforms.add(AppPlatform.IOS) // Assuming you use IOS for Apple/Mac for now
    if ("tv" in text)
        platforms.add(AppPlatform.TV)

    return platforms.distinct()
}


fun formatStars(count: Int): String =
    if (count >= 1000) "${count / 1000}.${(count % 1000) / 100}k" else count.toString()

fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000 -> "${bytes / 1_000_000} MB"
    bytes >= 1_000     -> "${bytes / 1_000} KB"
    else               -> "$bytes B"
}