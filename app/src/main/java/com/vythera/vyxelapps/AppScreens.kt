package com.vythera.vyxelapps

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File

private fun saveImageLocally(context: Context, uri: android.net.Uri, filename: String): String? {
    return try {
        val file = File(context.filesDir, "$filename.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (_: Exception) { null }
}

@Composable
fun SubScreenHeader(title: String, onBack: () -> Unit) {
    val t = LocalTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.bgSurface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = t.textPrimary)
        }
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROFILE SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    profile        : UserProfile,
    history        : List<HistoryItem>,
    favourites     : List<GitHubRepo>,
    installHistory : List<InstallHistoryEntry> = emptyList(),
    updates        : List<UpdateInfo>          = emptyList(),
    onSave         : (UserProfile) -> Unit,
    onAppClick     : (GitHubRepo) -> Unit,
    onCheckUpdates : () -> Unit                = {},
    onRollback     : (InstallHistoryEntry) -> Unit = {}
) {
    val t       = LocalTheme.current
    val context = LocalContext.current
    val s = LocalStrings.current

    var name     by remember { mutableStateOf(profile.name)     }
    var email    by remember { mutableStateOf(profile.email)    }
    var photoUri by remember { mutableStateOf(profile.photoUri) }
    var coverUri by remember { mutableStateOf(profile.coverUri) }
    var editing  by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { saveImageLocally(context, it, "profile_avatar")?.let { p -> photoUri = p } }
    }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { saveImageLocally(context, it, "profile_cover")?.let { p -> coverUri = p } }
    }

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
                Text(s.profileTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                TextButton(onClick = {
                    if (editing) {
                        onSave(UserProfile(name, "", email, photoUri, coverUri))
                        editing = false
                    } else editing = true
                }) {
                    Text(
                        if (editing) s.save else s.edit,
                        color      = t.accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 110.dp)) {

            // Cover + Avatar
            item {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    if (coverUri.isNotEmpty()) {
                        AsyncImage(
                            model              = coverUri,
                            contentDescription = null,
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop
                        )
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                    } else {
                        // M3 tonal gradient — uses accent, not random vivid colors
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.linearGradient(
                                    listOf(t.accentContainer, t.bgSurfaceHigh)
                                )
                            )
                        )
                    }
                    if (editing) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    coverPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(0.45f)
                        ) {
                            Text(
                                "Change Cover",
                                fontSize = 12.sp,
                                color    = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    // Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 20.dp)
                            .offset(y = 36.dp)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(t.bgSurface)
                            .border(3.dp, t.bgSurface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotEmpty()) {
                            AsyncImage(
                                model              = photoUri,
                                contentDescription = null,
                                modifier           = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale       = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Person,
                                null,
                                tint     = t.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        if (editing) {
                            Box(
                                Modifier.fillMaxSize().clip(CircleShape)
                                    .background(Color.Black.copy(0.4f))
                                    .clickable {
                                        avatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(44.dp))
            }

            // Name + Email
            item {
                Column(
                    modifier            = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (editing) {
                        TextField(
                            value            = name,
                            onValueChange    = { name = it },
                            modifier         = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                            placeholder      = { Text(s.displayName, color = t.textSecondary) },
                            singleLine       = true,
                            colors           = TextFieldDefaults.colors(
                                focusedContainerColor   = t.bgSurfaceAlt,
                                unfocusedContainerColor = t.bgSurfaceAlt,
                                focusedIndicatorColor   = t.accent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor        = t.textPrimary,
                                unfocusedTextColor      = t.textPrimary
                            )
                        )
                        TextField(
                            value            = email,
                            onValueChange    = { email = it },
                            modifier         = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                            placeholder      = { Text(s.emailAddress, color = t.textSecondary) },
                            singleLine       = true,
                            colors           = TextFieldDefaults.colors(
                                focusedContainerColor   = t.bgSurfaceAlt,
                                unfocusedContainerColor = t.bgSurfaceAlt,
                                focusedIndicatorColor   = t.accent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor        = t.textPrimary,
                                unfocusedTextColor      = t.textPrimary
                            )
                        )
                    } else {
                        Text(
                            name.ifEmpty { s.guestUser },
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = t.textPrimary
                        )
                        if (email.isNotEmpty())
                            Text(email, fontSize = 13.sp, color = t.textSecondary)
                    }
                }
            }

            // Support & Feedback
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    HorizontalDivider(color = t.borderVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.supportFeedbackTitle,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = t.textPrimary
                    )
                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        color  = t.bgSurface,
                        border = BorderStroke(0.5.dp, t.borderVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Send Feedback
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:narzo9990@gmail.com")
                                            putExtra(Intent.EXTRA_SUBJECT, "Vyxel Apps - Feedback")
                                            putExtra(Intent.EXTRA_TEXT, "Hi,\n\n")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    }
                                    .padding(14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Rounded.Email, null, tint = t.accent, modifier = Modifier.size(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.sendFeedback, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = t.textPrimary)
                                    Text(s.sendFeedbackDesc, fontSize = 11.sp, color = t.textSecondary)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = t.textSecondary.copy(0.4f), modifier = Modifier.size(16.dp))
                            }
                            HorizontalDivider(color = t.borderVariant, modifier = Modifier.padding(horizontal = 14.dp))

                            // Support the Project
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NikhilKain/vyxel-apps")))
                                    }
                                    .padding(14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Rounded.Favorite, null, tint = RedDanger, modifier = Modifier.size(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.supportProject, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = t.textPrimary)
                                    Text(s.supportProjectDesc, fontSize = 11.sp, color = t.textSecondary)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = t.textSecondary.copy(0.4f), modifier = Modifier.size(16.dp))
                            }
                            HorizontalDivider(color = t.borderVariant, modifier = Modifier.padding(horizontal = 14.dp))

                            // Share Vyxel
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Check out Vyxel Apps")
                                            putExtra(Intent.EXTRA_TEXT,
                                                "Vyxel Apps — open-source GitHub-powered Android app store.\nhttps://github.com/NikhilKain/vyxel-apps")
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share Vyxel Apps"))
                                    }
                                    .padding(14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Rounded.Share, null, tint = t.accent, modifier = Modifier.size(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.shareVyxel, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = t.textPrimary)
                                    Text(s.shareVyxelDesc, fontSize = 11.sp, color = t.textSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Favourites
            if (favourites.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.borderVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(s.favourites, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = t.textPrimary)
                    }
                }
                items(favourites.take(5)) { repo ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        AppListTile(repo = repo, onClick = { onAppClick(repo) })
                    }
                }
            }

            // Updates Available
            if (updates.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.borderVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "${s.updatesAvailable} (${updates.size})",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = t.textPrimary
                            )
                            TextButton(onClick = onCheckUpdates) {
                                Text(s.refresh, color = t.accent, fontSize = 12.sp)
                            }
                        }
                    }
                }
                items(updates) { upd ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Surface(
                            shape  = RoundedCornerShape(12.dp),
                            color  = t.bgSurface,
                            border = BorderStroke(0.5.dp, t.borderVariant)
                        ) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Update, null, tint = t.accent, modifier = Modifier.size(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(upd.repoName, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = t.textPrimary)
                                    Text("${upd.currentTag} → ${upd.latestTag}", fontSize = 11.sp, color = t.textSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Check for Updates button
            if (installHistory.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Button(
                            onClick  = onCheckUpdates,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = t.accent)
                        ) {
                            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s.checkForUpdates, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Install History
            if (installHistory.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.borderVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(s.installHistory, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = t.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(s.installHistoryHint, fontSize = 11.sp, color = t.textSecondary)
                    }
                }
                items(installHistory.sortedByDescending { it.installedAt }.take(15)) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onRollback(entry) },
                            shape  = RoundedCornerShape(12.dp),
                            color  = t.bgSurface,
                            border = BorderStroke(0.5.dp, t.borderVariant)
                        ) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.History, null, tint = t.accent, modifier = Modifier.size(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.repoName, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = t.textPrimary)
                                    Text(entry.tagName, fontSize = 11.sp, color = t.textSecondary)
                                    Text(formatTimeAgo(entry.installedAt), fontSize = 10.sp, color = t.textSecondary.copy(0.7f))
                                }
                                Icon(Icons.Rounded.RestartAlt, null, tint = t.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Recently Viewed
            if (history.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.borderVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(s.recentlyViewed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = t.textPrimary)
                    }
                }
                items(history.take(5)) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        AppListTile(repo = item.repo, onClick = { onAppClick(item.repo) })
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS SCREEN  — M3 Expressive surfaces
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    settings          : AppSettings,
    currentAccent     : Color?                    = null,
    useMonet          : Boolean                   = false,
    customTheme       : CustomThemeData           = CustomThemeData(),
    onSave            : (AppSettings) -> Unit,
    onAccentSelect    : (Color?) -> Unit          = {},
    onMonetToggle     : (Boolean) -> Unit         = {},
    onCustomThemeSave : (CustomThemeData) -> Unit = {}
) {
    val t       = LocalTheme.current
    val context = LocalContext.current
    val s = LocalStrings.current

    var themeMode           by remember { mutableStateOf(settings.themeMode)   }
    var amoled              by remember { mutableStateOf(settings.amoledBlack) }
    var fontName            by remember { mutableStateOf(settings.fontName)    }
    var language            by remember { mutableStateOf(settings.language)    }
    var fontExpanded        by remember { mutableStateOf(false)                }
    var customEditorExpanded by remember { mutableStateOf(false) }

    val fonts = listOf("Samsung One UI", "Serif", "Monospace", "Cursive")

    LaunchedEffect(themeMode, amoled, fontName, language) {
        val mapped = when (fontName) { "Samsung One UI" -> "Default"; "Sans Serif" -> "SansSerif"; else -> fontName }
        onSave(AppSettings(language, settings.githubToken, settings.sortBy, mapped, themeMode, amoled))
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.bgSurface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(s.settingsTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
        }

        Column(
            modifier            = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Appearance label
            Text(
                s.appearance,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = t.textSecondary,
                letterSpacing = 1.sp
            )

            // Theme mode — dropdown selector
            val themeOptions = listOf(
                "Light"   to "☀️  Light",
                "Dark"    to "🌙  Dark",
                "AMOLED"  to "🖤  AMOLED",
                "Minimal" to "◾  Minimal",
                "Sunset"  to "🌅  Sunset",
                "Custom"  to "🎨  Custom",
                "System"  to "📱  Follow System"
            )
            val selectedThemeLabel = themeOptions.firstOrNull { it.first == themeMode }?.second ?: themeMode
            var themeDropExpanded by remember { mutableStateOf(false) }

            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { themeDropExpanded = true },
                    shape  = RoundedCornerShape(16.dp),
                    color  = t.bgSurface,
                    border = BorderStroke(0.5.dp, t.borderVariant)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(selectedThemeLabel, fontSize = 14.sp, color = t.textPrimary, fontWeight = FontWeight.Medium)
                        Icon(
                            if (themeDropExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            null, tint = t.textSecondary
                        )
                    }
                }
                DropdownMenu(
                    expanded         = themeDropExpanded,
                    onDismissRequest = { themeDropExpanded = false },
                    containerColor   = t.bgSurface
                ) {
                    themeOptions.forEach { (mode, label) ->
                        val selected = themeMode == mode
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        label,
                                        color      = if (selected) t.accent else t.textPrimary,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize   = 14.sp
                                    )
                                    if (selected) Icon(Icons.Rounded.Check, null, tint = t.accent, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                themeMode = mode
                                themeDropExpanded = false
                                // Opening Custom always re-opens the editor
                                if (mode == "Custom") customEditorExpanded = true
                            }
                        )
                    }
                }
            }

            // Custom theme — collapsed pill or full editor
            if (themeMode == "Custom") {
                if (customEditorExpanded) {
                    CustomThemeEditor(
                        initial   = customTheme,
                        onSave    = onCustomThemeSave,
                        onApplied = { customEditorExpanded = false }
                    )
                } else {
                    // Compact "applied" row shown after editor collapses
                    Surface(
                        shape  = RoundedCornerShape(16.dp),
                        color  = t.bgSurface,
                        border = BorderStroke(1.dp, t.accent.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Mini colour swatches from the applied theme
                                val ap = customTheme.toAppThemeColors()
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(ap.bgPrimary, ap.bgSurface, ap.accent, ap.accentContainer, ap.textPrimary).forEach { c ->
                                        Box(
                                            Modifier.size(14.dp).clip(CircleShape)
                                                .background(c)
                                                .border(0.5.dp, t.borderVariant, CircleShape)
                                        )
                                    }
                                }
                                Column {
                                    Text("Custom Theme", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = t.accent)
                                    Text("Applied", fontSize = 11.sp, color = t.textSecondary)
                                }
                            }
                            TextButton(onClick = { customEditorExpanded = true }) {
                                Icon(Icons.Rounded.Edit, null, tint = t.accent, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit", color = t.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Dynamic Colors (Monet / Material You)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                Surface(
                    shape  = RoundedCornerShape(16.dp),
                    color  = t.bgSurface,
                    border = BorderStroke(0.5.dp, t.borderVariant)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.materialYou, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = t.textPrimary)
                            Text(
                                s.materialYouDesc,
                                fontSize = 12.sp, color = t.textSecondary, lineHeight = 17.sp
                            )
                        }
                        Switch(
                            checked         = useMonet,
                            onCheckedChange = { onMonetToggle(it) },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = t.accent
                            )
                        )
                    }
                }
            }

            // Theme Color
            Text(
                s.themeColor,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = t.textSecondary,
                letterSpacing = 1.sp
            )

            val accentOptions: List<Color?> = listOf(
                null,
                Color(0xFF6C63FF), Color(0xFFFF6B35), Color(0xFF2196F3),
                Color(0xFF1DB954), Color(0xFFE91E63), Color(0xFF00BCD4),
                Color(0xFFFF9800), Color(0xFFF44336), Color(0xFF9C27B0)
            )

            Row(
                modifier              = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                accentOptions.forEach { option ->
                    val isSelected = !useMonet && currentAccent == option
                    val bgMod      = if (option != null) Modifier.background(option, CircleShape)
                    else Modifier.background(
                        Brush.linearGradient(listOf(Color(0xFF6C63FF), Color(0xFFFF6B35))),
                        CircleShape
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .then(bgMod)
                            .border(
                                if (isSelected) 3.dp else 1.5.dp,
                                if (isSelected) Color.White else t.borderVariant,
                                CircleShape
                            )
                            .clickable {
                                if (option == null) onMonetToggle(true)
                                else onAccentSelect(option)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // AMOLED
            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = t.bgSurface,
                border = BorderStroke(0.5.dp, t.borderVariant)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.amoledOverride, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = t.textPrimary)
                        Text(s.amoledOverrideDesc, fontSize = 12.sp, color = t.textSecondary, lineHeight = 17.sp)
                    }
                    Switch(
                        checked         = amoled,
                        onCheckedChange = { amoled = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = t.accent
                        )
                    )
                }
            }

            // Font
            Text(
                s.fontSection,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = t.textSecondary,
                letterSpacing = 1.sp
            )

            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = t.bgSurface,
                border = BorderStroke(0.5.dp, t.borderVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { fontExpanded = !fontExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(fontName, fontSize = 14.sp, color = t.textPrimary)
                        Icon(
                            if (fontExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            null,
                            tint = t.textSecondary
                        )
                    }
                    AnimatedVisibility(visible = fontExpanded) {
                        Column {
                            fonts.forEach { f ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (fontName == f) t.accentContainer.copy(0.3f) else Color.Transparent)
                                        .clickable { fontName = f; fontExpanded = false }
                                        .padding(horizontal = 20.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        f,
                                        fontSize   = 14.sp,
                                        color      = t.textPrimary,
                                        fontWeight = if (fontName == f) FontWeight.SemiBold else FontWeight.Normal,
                                        fontFamily = fontFamilyFor(if (f == "Samsung One UI") "Default" else f)
                                    )
                                    if (fontName == f) Icon(Icons.Rounded.Check, null, tint = t.accent, modifier = Modifier.size(16.dp))
                                }
                                if (f != fonts.last()) HorizontalDivider(color = t.borderVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            // Language
            Text(
                s.languageSection,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = t.textSecondary,
                letterSpacing = 1.sp
            )

            val supportedLanguages = listOf(
                "English", "Hindi", "Spanish", "French", "German", "Japanese",
                "Portuguese", "Italian", "Russian", "Chinese", "Korean",
                "Arabic", "Dutch", "Turkish", "Polish", "Swedish"
            )
            var langExpanded by remember { mutableStateOf(false) }

            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = t.bgSurface,
                border = BorderStroke(0.5.dp, t.borderVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { langExpanded = !langExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(language, fontSize = 14.sp, color = t.textPrimary)
                        Icon(
                            if (langExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            null,
                            tint = t.textSecondary
                        )
                    }
                    AnimatedVisibility(visible = langExpanded) {
                        Column {
                            supportedLanguages.forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (language == lang) t.accentContainer.copy(0.3f) else Color.Transparent)
                                        .clickable {
                                            language = lang
                                            langExpanded = false
                                            val mapped = when (fontName) { "Samsung One UI" -> "Default"; "Sans Serif" -> "SansSerif"; else -> fontName }
                                            onSave(AppSettings(lang, settings.githubToken, settings.sortBy, mapped, themeMode, amoled))
                                        }
                                        .padding(horizontal = 20.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        lang,
                                        fontSize   = 14.sp,
                                        color      = t.textPrimary,
                                        fontWeight = if (language == lang) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    if (language == lang) Icon(Icons.Rounded.Check, null, tint = t.accent, modifier = Modifier.size(16.dp))
                                }
                                if (lang != supportedLanguages.last()) HorizontalDivider(color = t.borderVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            // GitHub Token
            Text(
                s.githubSection,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = t.textSecondary,
                letterSpacing = 1.sp
            )

            var githubTokenInput by remember { mutableStateOf(settings.githubToken) }
            var isEditingToken   by remember { mutableStateOf(settings.githubToken.isEmpty()) }

            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = t.bgSurface,
                border = BorderStroke(0.5.dp, t.borderVariant)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Lock, null, tint = t.accent, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                s.personalAccessToken,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color      = t.textPrimary
                            )
                            Text(
                                s.tokenRequired,
                                fontSize = 11.sp,
                                color    = t.textSecondary
                            )
                        }
                    }

                    if (isEditingToken) {
                        TextField(
                            value         = githubTokenInput,
                            onValueChange = { githubTokenInput = it },
                            modifier      = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                            placeholder   = { Text("ghp_xxxxxxxxxxxx", color = t.textSecondary, fontSize = 13.sp) },
                            singleLine    = true,
                            visualTransformation = if (githubTokenInput.length > 4)
                                PasswordVisualTransformation() else VisualTransformation.None,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor   = t.bgSurfaceAlt,
                                unfocusedContainerColor = t.bgSurfaceAlt,
                                focusedIndicatorColor   = t.accent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor        = t.textPrimary,
                                unfocusedTextColor      = t.textPrimary
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick  = {
                                    val mapped = when (fontName) {
                                        "Samsung One UI" -> "Default"
                                        "Sans Serif"     -> "SansSerif"
                                        else             -> fontName
                                    }
                                    onSave(AppSettings(language, githubTokenInput.trim(), settings.sortBy, mapped, themeMode, amoled))
                                    isEditingToken = false
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = t.accent)
                            ) {
                                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.saveToken, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            if (settings.githubToken.isNotEmpty()) {
                                OutlinedButton(
                                    onClick  = { githubTokenInput = settings.githubToken; isEditingToken = false },
                                    shape    = RoundedCornerShape(10.dp),
                                    border   = BorderStroke(1.dp, t.borderVariant),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text("Cancel", color = t.textSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        // Saved — read-only display with pencil to re-edit
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(t.bgSurfaceAlt)
                                .padding(start = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "••••••••••••••••••••",
                                color    = t.textSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )
                            IconButton(onClick = { isEditingToken = true }) {
                                Icon(Icons.Rounded.Edit, null, tint = t.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // About
            Text(
                s.aboutSection,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = t.textSecondary,
                letterSpacing = 1.sp
            )

            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = t.bgSurface,
                border = BorderStroke(0.5.dp, t.borderVariant)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s.versionLabel, fontSize = 14.sp, color = t.textPrimary)
                    Text("1.0.1",   fontSize = 14.sp, color = t.textSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CUSTOM THEME EDITOR
// ─────────────────────────────────────────────────────────────────────────────

private val THEME_PRESETS = listOf(
    "Dark"    to { DarkTheme.toCustomThemeData() },
    "Light"   to { LightTheme.toCustomThemeData() },
    "AMOLED"  to { AmoledTheme.toCustomThemeData() },
    "Sunset"  to { SunsetTheme.toCustomThemeData() },
    "Minimal" to { MinimalTheme.toCustomThemeData() }
)

@Composable
private fun ColorField(
    label    : String,
    hexValue : String,
    onChange : (String) -> Unit
) {
    val t            = LocalTheme.current
    val color        = hexToColor(hexValue, fallback = t.accent)
    var showSliders  by remember { mutableStateOf(false) }

    // Extract R/G/B from hex string
    val hexClean = hexValue.trimStart('#').padStart(6, '0').take(6)
    val rInt = hexClean.substring(0, 2).toIntOrNull(16) ?: 0
    val gInt = hexClean.substring(2, 4).toIntOrNull(16) ?: 0
    val bInt = hexClean.substring(4, 6).toIntOrNull(16) ?: 0

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                label,
                fontSize  = 12.sp,
                color     = t.textSecondary,
                modifier  = Modifier.width(112.dp),
                maxLines  = 1
            )
            TextField(
                value         = hexValue,
                onValueChange = { v ->
                    val clean = v.trimStart().let { if (it.startsWith("#")) it else "#$it" }
                    if (clean.length <= 7) onChange(clean)
                },
                modifier    = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)),
                singleLine  = true,
                placeholder = { Text("#RRGGBB", color = t.textSecondary, fontSize = 12.sp) },
                colors      = TextFieldDefaults.colors(
                    focusedContainerColor   = t.bgSurfaceAlt,
                    unfocusedContainerColor = t.bgSurfaceAlt,
                    focusedTextColor        = t.textPrimary,
                    unfocusedTextColor      = t.textPrimary,
                    focusedIndicatorColor   = t.accent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = t.accent
                )
            )
            // Tapping the swatch toggles RGB sliders
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .border(
                        width  = if (showSliders) 2.dp else 1.dp,
                        color  = if (showSliders) t.accent else t.borderVariant,
                        shape  = RoundedCornerShape(6.dp)
                    )
                    .clickable { showSliders = !showSliders }
            )
        }

        // RGB slider panel — toggled by clicking the color swatch
        AnimatedVisibility(visible = showSliders) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(start = 122.dp, top = 6.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                data class Channel(val name: String, val value: Int, val trackColor: Color)
                listOf(
                    Channel("R", rInt, Color(1f, 0.2f, 0.2f)),
                    Channel("G", gInt, Color(0.2f, 0.8f, 0.3f)),
                    Channel("B", bInt, Color(0.3f, 0.5f, 1f))
                ).forEach { ch ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            ch.name,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color      = ch.trackColor,
                            modifier   = Modifier.width(10.dp)
                        )
                        Slider(
                            value          = ch.value.toFloat(),
                            onValueChange  = { v ->
                                val nv = v.toInt()
                                onChange("#%02X%02X%02X".format(
                                    if (ch.name == "R") nv else rInt,
                                    if (ch.name == "G") nv else gInt,
                                    if (ch.name == "B") nv else bInt
                                ))
                            },
                            valueRange     = 0f..255f,
                            steps          = 0,
                            modifier       = Modifier.weight(1f).height(28.dp),
                            colors         = SliderDefaults.colors(
                                thumbColor         = ch.trackColor,
                                activeTrackColor   = ch.trackColor,
                                inactiveTrackColor = t.bgSurfaceHigh
                            )
                        )
                        Text(
                            "%3d".format(ch.value),
                            fontSize  = 10.sp,
                            color     = t.textSecondary,
                            modifier  = Modifier.width(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveThemePreview(p: AppThemeColors) {
    Surface(
        shape  = RoundedCornerShape(14.dp),
        color  = p.bgPrimary,
        border = BorderStroke(0.5.dp, p.borderVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.bgSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(p.accent))
                    Text("Vyxel Apps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                }
                Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(p.accentContainer))
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // ── App tile card ─────────────────────────────────────────
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = p.bgSurface,
                    border = BorderStroke(0.5.dp, p.borderVariant)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(p.bgSurfaceHigh))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(Modifier.fillMaxWidth(0.55f).height(9.dp).clip(RoundedCornerShape(3.dp)).background(p.textPrimary))
                            Box(Modifier.fillMaxWidth(0.85f).height(7.dp).clip(RoundedCornerShape(3.dp)).background(p.textSecondary.copy(0.45f)))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.width(28.dp).height(14.dp).clip(RoundedCornerShape(7.dp)).background(p.accent.copy(0.2f)).border(0.5.dp, p.accent.copy(0.5f), RoundedCornerShape(7.dp)))
                                Box(Modifier.width(36.dp).height(14.dp).clip(RoundedCornerShape(7.dp)).background(p.accentContainer.copy(0.3f)).border(0.5.dp, p.accentContainer.copy(0.6f), RoundedCornerShape(7.dp)))
                            }
                        }
                        Box(Modifier.size(22.dp).clip(CircleShape).background(p.accent.copy(0.15f)).border(1.dp, p.accent.copy(0.4f), CircleShape))
                    }
                }
                // ── Action button ─────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp))
                            .background(p.bgSurfaceAlt)
                            .border(1.dp, p.borderVariant, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.width(40.dp).height(7.dp).clip(RoundedCornerShape(3.dp)).background(p.textSecondary.copy(0.5f)))
                    }
                    Box(
                        Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(p.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.width(40.dp).height(7.dp).clip(RoundedCornerShape(3.dp)).background(p.onAccentContainer.copy(0.8f)))
                    }
                }
                // ── Nav dock strip ────────────────────────────────────────
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = p.dockBg,
                    border = BorderStroke(0.5.dp, p.borderVariant.copy(0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(p.accent.copy(0.2f)))
                        listOf(0.35f, 0.35f, 0.35f, 0.35f).forEach { alpha ->
                            Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(p.dockForeground.copy(alpha)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomThemeEditor(
    initial   : CustomThemeData,
    onSave    : (CustomThemeData) -> Unit,
    onApplied : () -> Unit = {}
) {
    val t = LocalTheme.current
    var d       by remember(initial) { mutableStateOf(initial) }
    var applied by remember { mutableStateOf(false) }

    LaunchedEffect(applied) {
        if (applied) { kotlinx.coroutines.delay(650); onApplied() }
    }

    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = t.bgSurface,
        border = BorderStroke(0.5.dp, t.borderVariant)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Custom Theme Editor",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = t.textPrimary
            )

            // ── Load preset ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("LOAD PRESET", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = t.textSecondary, letterSpacing = 1.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    THEME_PRESETS.forEach { (name, factory) ->
                        OutlinedButton(
                            onClick      = { d = factory() },
                            shape        = RoundedCornerShape(20.dp),
                            border       = BorderStroke(1.dp, t.borderVariant),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(name, fontSize = 12.sp, color = t.textPrimary)
                        }
                    }
                }
            }

            HorizontalDivider(color = t.borderVariant)

            // ── Backgrounds ───────────────────────────────────────────────
            Text("BACKGROUNDS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)
            ColorField("Background",   d.bgPrimary)    { d = d.copy(bgPrimary    = it) }
            ColorField("Surface",      d.bgSurface)    { d = d.copy(bgSurface    = it) }
            ColorField("Surface Alt",  d.bgSurfaceAlt) { d = d.copy(bgSurfaceAlt = it) }
            ColorField("Surface High", d.bgSurfaceHigh){ d = d.copy(bgSurfaceHigh= it) }

            HorizontalDivider(color = t.borderVariant)

            // ── Text ──────────────────────────────────────────────────────
            Text("TEXT", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)
            ColorField("Primary text",   d.textPrimary)   { d = d.copy(textPrimary   = it) }
            ColorField("Secondary text", d.textSecondary) { d = d.copy(textSecondary = it) }

            HorizontalDivider(color = t.borderVariant)

            // ── Accent / Primary ──────────────────────────────────────────
            Text("ACCENT / PRIMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)
            ColorField("Accent",       d.accent)           { d = d.copy(accent           = it) }
            ColorField("Accent alt",   d.accentAlt)        { d = d.copy(accentAlt        = it) }
            ColorField("Container",    d.accentContainer)  { d = d.copy(accentContainer  = it) }
            ColorField("On container", d.onAccentContainer){ d = d.copy(onAccentContainer= it) }

            HorizontalDivider(color = t.borderVariant)

            // ── Borders ───────────────────────────────────────────────────
            Text("BORDERS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)
            ColorField("Border",     d.border)       { d = d.copy(border        = it) }
            ColorField("Border dim", d.borderVariant){ d = d.copy(borderVariant = it) }

            HorizontalDivider(color = t.borderVariant)

            // ── Appearance mode (dark / light status bar) ─────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Appearance", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = t.textPrimary)
                    Text("Controls status-bar icon colour", fontSize = 11.sp, color = t.textSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to "Dark", false to "Light").forEach { (dark, label) ->
                        val selected = d.isDark == dark
                        Surface(
                            shape  = RoundedCornerShape(20.dp),
                            color  = if (selected) t.accent.copy(0.18f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (selected) t.accent else t.borderVariant),
                            modifier = Modifier.clickable { d = d.copy(isDark = dark) }
                        ) {
                            Text(
                                label,
                                fontSize  = 12.sp,
                                color     = if (selected) t.accent else t.textSecondary,
                                fontWeight= if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier  = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ── Live preview ──────────────────────────────────────────────
            val preview = d.toAppThemeColors()
            LiveThemePreview(preview)

            // ── Apply ─────────────────────────────────────────────────────
            Button(
                onClick  = { if (!applied) { onSave(d); applied = true } },
                enabled  = !applied,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = t.accent,
                    disabledContainerColor = t.bgSurfaceAlt,
                    disabledContentColor   = t.textSecondary
                )
            ) {
                Icon(
                    if (applied) Icons.Rounded.Check else Icons.Rounded.Palette,
                    null, modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (applied) "Applied ✓" else "Apply Custom Theme", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GITHUB TOKEN ONBOARDING  (shown after splash when no token is saved)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GitHubTokenOnboarding(
    onSave : (String) -> Unit,
    onSkip : () -> Unit
) {
    val t          = LocalTheme.current
    var tokenInput by remember { mutableStateOf("") }

    Box(
        modifier         = Modifier.fillMaxSize().background(t.bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(20.dp)
        ) {
            // Icon
            Box(
                modifier         = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(t.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    null,
                    tint     = t.accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                "GitHub Search",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = t.textPrimary,
                textAlign  = TextAlign.Center
            )

            Text(
                "To search apps directly on GitHub, add your Personal Access Token.\n\nYou can skip this and add it later in Settings.",
                fontSize  = 14.sp,
                color     = t.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            // How to generate a token
            Surface(
                shape  = RoundedCornerShape(14.dp),
                color  = t.bgSurface,
                border = BorderStroke(0.5.dp, t.borderVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Info, null, tint = t.accent, modifier = Modifier.size(15.dp))
                        Text(
                            "How to generate a token",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = t.textPrimary
                        )
                    }
                    val steps = listOf(
                        "Open github.com → Sign in to your account",
                        "Go to Settings → Developer settings → Personal access tokens → Tokens (classic)",
                        "Click \"Generate new token (classic)\"",
                        "Add a note (e.g. \"Vyxel Apps\") and set an expiration",
                        "Under Scopes, check \"public_repo\" (no other scopes needed)",
                        "Click \"Generate token\" and copy it immediately"
                    )
                    steps.forEachIndexed { i, step ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.Top
                        ) {
                            Box(
                                modifier         = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(t.accentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${i + 1}",
                                    fontSize   = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = t.accent
                                )
                            }
                            Text(
                                step,
                                fontSize   = 12.sp,
                                color      = t.textSecondary,
                                lineHeight = 18.sp,
                                modifier   = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            TextField(
                value         = tokenInput,
                onValueChange = { tokenInput = it },
                modifier      = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                placeholder   = { Text("ghp_xxxxxxxxxxxx", color = t.textSecondary) },
                singleLine    = true,
                visualTransformation = if (tokenInput.length > 4)
                    PasswordVisualTransformation() else VisualTransformation.None,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = t.bgSurfaceAlt,
                    unfocusedContainerColor = t.bgSurfaceAlt,
                    focusedIndicatorColor   = t.accent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = t.textPrimary,
                    unfocusedTextColor      = t.textPrimary
                )
            )

            Button(
                onClick  = {
                    if (tokenInput.isNotBlank()) onSave(tokenInput.trim()) else onSkip()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = t.accent)
            ) {
                Text(
                    if (tokenInput.isNotBlank()) "Save & Continue" else "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }

            TextButton(onClick = onSkip) {
                Text("Skip for now", color = t.textSecondary, fontSize = 14.sp)
            }
        }
    }
}

fun formatTimeAgo(ts: Long): String {
    val d = System.currentTimeMillis() - ts
    return when {
        d < 60_000     -> "Just now"
        d < 3_600_000  -> "${d / 60_000}m ago"
        d < 86_400_000 -> "${d / 3_600_000}h ago"
        else           -> "${d / 86_400_000}d ago"
    }
}
