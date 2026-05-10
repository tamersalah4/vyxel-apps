package com.vythera.vyxelapps

import android.content.Context
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File

// enum class DrawerPage { PROFILE, HISTORY, INSTALLED, THEMES, SETTINGS, ABOUT }

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
        modifier = Modifier.fillMaxWidth().background(t.bgSurface)
            .statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = t.textPrimary)
        }
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PROFILE SCREEN
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    profile          : UserProfile,
    history          : List<HistoryItem>,
    favourites       : List<GitHubRepo>,
    installHistory   : List<InstallHistoryEntry> = emptyList(),
    updates          : List<UpdateInfo> = emptyList(),
    onSave           : (UserProfile) -> Unit,
    onAppClick       : (GitHubRepo) -> Unit,
    onCheckUpdates   : () -> Unit = {},
    onRollback       : (InstallHistoryEntry) -> Unit = {}
) {
    val t       = LocalTheme.current
    val context = LocalContext.current

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
        Column(modifier = Modifier.fillMaxWidth().background(t.bgSurface).statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
                TextButton(onClick = {
                    if (editing) { onSave(UserProfile(name, "", email, photoUri, coverUri)); editing = false }
                    else editing = true
                }) {
                    Text(if (editing) "Save" else "Edit", color = t.accent, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 110.dp)) {

            // ── Cover + Avatar ────────────────────────────────────────
            item {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    if (coverUri.isNotEmpty()) {
                        AsyncImage(model = coverUri, contentDescription = null,
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.35f)))
                    } else {
                        Box(Modifier.fillMaxSize().background(Brush.linearGradient(
                            if (t.isDark) listOf(Color(0xFF1A1060), Color(0xFF0D3875))
                            else listOf(t.accent, t.accentAlt)
                        )))
                    }
                    if (editing) {
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                            .clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(0.45f))
                            .clickable { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text("Change Cover", fontSize = 12.sp, color = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp)
                            .offset(y = 36.dp).size(72.dp).clip(CircleShape)
                            .background(t.bgSurface).border(3.dp, t.bgSurface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotEmpty()) {
                            AsyncImage(model = photoUri, contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Rounded.Person, null, tint = t.textSecondary, modifier = Modifier.size(36.dp))
                        }
                        if (editing) {
                            Box(Modifier.fillMaxSize().clip(CircleShape).background(Color.Black.copy(0.4f))
                                .clickable { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(44.dp))
            }

            // ── Name + Email ──────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (editing) {
                        TextField(value = name, onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                            placeholder = { Text("Display name", color = t.textSecondary) }, singleLine = true,
                            colors = TextFieldDefaults.colors(focusedContainerColor = t.bgSurfaceAlt,
                                unfocusedContainerColor = t.bgSurfaceAlt, focusedIndicatorColor = t.accent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = t.textPrimary, unfocusedTextColor = t.textPrimary))
                        TextField(value = email, onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                            placeholder = { Text("Email address", color = t.textSecondary) }, singleLine = true,
                            colors = TextFieldDefaults.colors(focusedContainerColor = t.bgSurfaceAlt,
                                unfocusedContainerColor = t.bgSurfaceAlt, focusedIndicatorColor = t.accent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = t.textPrimary, unfocusedTextColor = t.textPrimary))
                    } else {
                        Text(name.ifEmpty { "Guest User" }, fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
                        if (email.isNotEmpty()) Text(email, fontSize = 13.sp, color = t.textSecondary)
                    }
                }
            }

            // ── Support & Feedback (right below user info, always visible) ────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    HorizontalDivider(color = t.border)
                    Spacer(Modifier.height(12.dp))
                    Text("Support & Feedback", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    Spacer(Modifier.height(10.dp))

                    // Send Feedback (email)
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(t.bgSurface).clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:narzo9990@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Vyxel Apps - Feedback")
                                putExtra(Intent.EXTRA_TEXT, "Hi,\n\n")
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Email, null, tint = t.accent, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Send Feedback", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = t.textPrimary)
                            Text("Suggest features or report bugs", fontSize = 11.sp, color = t.textSecondary)
                        }
                        Icon(Icons.Rounded.OpenInBrowser, null, tint = t.textSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(8.dp))

                    // Support the Project
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(t.bgSurface).clickable {
                            context.startActivity(Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/NikhilKain/vyxel-apps")
                            ))
                        }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Favorite, null, tint = RedDanger, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Support the Project", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = t.textPrimary)
                            Text("Star on GitHub or contribute", fontSize = 11.sp, color = t.textSecondary)
                        }
                        Icon(Icons.Rounded.OpenInBrowser, null, tint = t.textSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(8.dp))

                    // Share Vyxel
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(t.bgSurface).clickable {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Check out Vyxel Apps")
                                putExtra(Intent.EXTRA_TEXT,
                                    "Vyxel Apps — open-source GitHub-powered Android app store.\nhttps://github.com/NikhilKain/vyxel-apps")
                            }
                            context.startActivity(Intent.createChooser(share, "Share Vyxel Apps"))
                        }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Share, null, tint = t.accent, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Share Vyxel", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = t.textPrimary)
                            Text("Tell your friends about it", fontSize = 11.sp, color = t.textSecondary)
                        }
                    }
                }
            }

            // ── Favourites ────────────────────────────────────────────
            if (favourites.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.border)
                        Spacer(Modifier.height(12.dp))
                        Text("Favourites", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                    }
                }
                items(favourites.take(5)) { repo ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        AppListTile(repo = repo, onClick = { onAppClick(repo) })
                    }
                }
            }

            // ── Updates Available ─────────────────────────────────────
            if (updates.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.border)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Updates Available (${updates.size})",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                            TextButton(onClick = onCheckUpdates) {
                                Text("Refresh", color = t.accent, fontSize = 12.sp)
                            }
                        }
                    }
                }
                items(updates) { upd ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(t.bgSurface).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.Update, null, tint = t.accent, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(upd.repoName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = t.textPrimary)
                                Text("${upd.currentTag} → ${upd.latestTag}", fontSize = 11.sp, color = t.textSecondary)
                            }
                        }
                    }
                }
            }

            // ── Check for Updates button ──────────────────────────────
            if (installHistory.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Button(onClick = onCheckUpdates,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = t.accent)) {
                            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Check for Updates", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Install History with rollback ─────────────────────────
            if (installHistory.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.border)
                        Spacer(Modifier.height(12.dp))
                        Text("Install History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap any version to reinstall it", fontSize = 11.sp, color = t.textSecondary)
                    }
                }
                items(installHistory.sortedByDescending { it.installedAt }.take(15)) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(t.bgSurface).clickable { onRollback(entry) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.History, null, tint = t.accent, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.repoName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = t.textPrimary)
                                Text(entry.tagName, fontSize = 11.sp, color = t.textSecondary)
                                Text(formatTimeAgo(entry.installedAt), fontSize = 10.sp, color = t.textSecondary.copy(0.7f))
                            }
                            Icon(Icons.Rounded.RestartAlt, null, tint = t.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Recently Viewed ───────────────────────────────────────
            if (history.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HorizontalDivider(color = t.border)
                        Spacer(Modifier.height(12.dp))
                        Text("Recently Viewed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.textPrimary)
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

// ─────────────────────────────────────────────────────────────────────────
// SETTINGS SCREEN
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    settings       : AppSettings,
    currentAccent  : Color?            = null,
    useMonet       : Boolean           = false,
    onSave         : (AppSettings) -> Unit,
    onAccentSelect : (Color?) -> Unit  = {},
    onMonetToggle  : (Boolean) -> Unit = {}
) {
    val t       = LocalTheme.current
    val context = LocalContext.current

    var themeMode    by remember { mutableStateOf(settings.themeMode)    }
    var amoled       by remember { mutableStateOf(settings.amoledBlack)  }
    var fontName     by remember { mutableStateOf(settings.fontName)     }
    var language     by remember { mutableStateOf(settings.language)     }
    var token        by remember { mutableStateOf(settings.githubToken)  }
    var showToken    by remember { mutableStateOf(false)                 }
    var fontExpanded by remember { mutableStateOf(false)                 }
    var langExpanded by remember { mutableStateOf(false)                 }

    val fonts     = listOf("Samsung One UI", "Serif", "Monospace", "Cursive")
    val languages = listOf("English", "Hindi", "Spanish", "French", "German", "Japanese")

    LaunchedEffect(themeMode, amoled, fontName, language) {
        val mapped = if (fontName == "Samsung One UI") "Default" else fontName
        onSave(AppSettings(language, token, settings.sortBy, mapped, themeMode, amoled))
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bgPrimary)) {
        Column(modifier = Modifier.fillMaxWidth().background(t.bgSurface)
            .statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = t.textPrimary)
        }

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Appearance ────────────────────────────────────────────
            Text("APPEARANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(t.bgSurface).padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                listOf("Light" to "☀️  Light", "Dark" to "🌙  Dark", "System" to "📱  Follow System")
                    .forEach { (mode, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(if (themeMode == mode) t.accent.copy(0.1f) else Color.Transparent)
                                .clickable { themeMode = mode }.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 14.sp, color = t.textPrimary,
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal)
                            if (themeMode == mode) {
                                Box(Modifier.size(20.dp).clip(CircleShape).background(t.accent),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Check, null, tint = Color.White,
                                        modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
            }

            // ── Dynamic Colors ────────────────────────────────────────
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(t.bgSurface).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Colors", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = t.textPrimary)
                        Text("Use system colors (Monet)", fontSize = 12.sp, color = t.textSecondary)
                    }
                    Switch(checked = useMonet, onCheckedChange = { onMonetToggle(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                            checkedTrackColor = t.accent))
                }
            }

            // ── Theme Color ───────────────────────────────────────────
            Text("THEME COLOR", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)

            val accentOptions: List<Color?> = listOf(null, Color(0xFF6C63FF), Color(0xFFFF6B35),
                Color(0xFF2196F3), Color(0xFF1DB954), Color(0xFFE91E63), Color(0xFF00BCD4),
                Color(0xFFFF9800), Color(0xFFF44336), Color(0xFF9C27B0))

            Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                accentOptions.forEach { option ->
                    val isSelected = !useMonet && currentAccent == option
                    val bgMod = if (option != null) Modifier.background(option, CircleShape)
                    else Modifier.background(Brush.linearGradient(
                        listOf(Color(0xFF6C63FF), Color(0xFFFF6B35))), CircleShape)
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).then(bgMod)
                            .border(if (isSelected) 3.dp else 1.5.dp,
                                if (isSelected) Color.White else t.border, CircleShape)
                            .clickable {
                                if (option == null) onMonetToggle(true)
                                else onAccentSelect(option)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Rounded.Check, null, tint = Color.White,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }

            // ── AMOLED ────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(t.bgSurface).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AMOLED Black", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = t.textPrimary)
                    Text("Pure black for OLED displays", fontSize = 12.sp, color = t.textSecondary)
                }
                Switch(checked = amoled, onCheckedChange = { amoled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                        checkedTrackColor = t.accent))
            }

            // ── Font ──────────────────────────────────────────────────
            Text("FONT", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(t.bgSurface)) {
                Row(modifier = Modifier.fillMaxWidth().clickable { fontExpanded = !fontExpanded }
                    .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(fontName, fontSize = 14.sp, color = t.textPrimary)
                    Icon(if (fontExpanded) Icons.Rounded.KeyboardArrowUp
                    else Icons.Rounded.KeyboardArrowDown, null, tint = t.textSecondary)
                }
                AnimatedVisibility(visible = fontExpanded) {
                    Column {
                        fonts.forEach { f ->
                            Row(modifier = Modifier.fillMaxWidth()
                                .background(if (fontName == f) t.accent.copy(0.08f) else Color.Transparent)
                                .clickable { fontName = f; fontExpanded = false }
                                .padding(horizontal = 20.dp, vertical = 13.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(f, fontSize = 14.sp, color = t.textPrimary,
                                    fontWeight = if (fontName == f) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = fontFamilyFor(if (f == "Samsung One UI") "Default" else f))
                                if (fontName == f) Icon(Icons.Rounded.Check, null, tint = t.accent,
                                    modifier = Modifier.size(16.dp))
                            }
                            if (f != fonts.last()) HorizontalDivider(color = t.border.copy(0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // ── GitHub Token ──────────────────────────────────────────
            Text("GITHUB TOKEN", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)

            var tokenLocked  by remember { mutableStateOf(token.isNotEmpty()) }
            var savedRecently by remember { mutableStateOf(false) }

            LaunchedEffect(savedRecently) {
                if (savedRecently) { kotlinx.coroutines.delay(1800); savedRecently = false }
            }

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(t.bgSurface).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Free token = 5,000 req/hour. Without = 60/hour.",
                    fontSize = 12.sp, color = t.textSecondary)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = token, onValueChange = { token = it },
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)),
                        placeholder = { Text("ghp_xxxxxxxxxxxx", color = t.textSecondary, fontSize = 13.sp) },
                        singleLine = true,
                        enabled = !tokenLocked,
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showToken = !showToken }) {
                                Icon(if (showToken) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    null, tint = t.textSecondary)
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = t.bgSurfaceAlt, unfocusedContainerColor = t.bgSurfaceAlt,
                            disabledContainerColor = t.bgSurfaceAlt,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = t.textPrimary, unfocusedTextColor = t.textPrimary,
                            disabledTextColor = t.textSecondary
                        )
                    )
                    if (tokenLocked) {
                        IconButton(onClick = { tokenLocked = false }) {
                            Icon(Icons.Rounded.Edit, "Edit", tint = t.accent)
                        }
                    }
                }

                Button(
                    onClick = {
                        val mapped = if (fontName == "Samsung One UI") "Default" else fontName
                        onSave(AppSettings(language, token, settings.sortBy, mapped, themeMode, amoled))
                        savedRecently = true
                        tokenLocked   = true
                    },
                    enabled = !tokenLocked,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = t.accent,
                        disabledContainerColor = t.bgSurfaceAlt,
                        disabledContentColor = t.textSecondary
                    )
                ) {
                    Icon(if (savedRecently) Icons.Rounded.Check else Icons.Rounded.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (savedRecently) "Saved!" else if (tokenLocked) "Saved" else "Save Token", fontWeight = FontWeight.Bold)
                }
            }

            // ── About ─────────────────────────────────────────────────
            Text("ABOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = t.textSecondary, letterSpacing = 1.sp)

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(t.bgSurface)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Version", fontSize = 14.sp, color = t.textPrimary)
                    Text("1.0.0", fontSize = 14.sp, color = t.textSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SETUP SCREEN
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun SetupScreen(onComplete: (String) -> Unit, onSkip: () -> Unit) {
    var token by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            androidx.compose.foundation.Image(painterResource(R.drawable.skpic), null,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(24.dp)))
            Text("Welcome to Vyxel Apps", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White)
            Text("Powered by GitHub Open Source", fontSize = 14.sp, color = Color(0xFF888888))

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF111111)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Key, null, tint = Color(0xFF6C63FF), modifier = Modifier.size(20.dp))
                    Text("GitHub Token Recommended", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = Color.White)
                }
                Text("Without a token the app is limited to 60 requests/hour. A free token gives 5,000/hour.",
                    fontSize = 13.sp, color = Color(0xFF999999), lineHeight = 20.sp)
                HorizontalDivider(color = Color(0xFF222222))
                Text("How to get a free token:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF777777))
                listOf("1. Go to github.com → sign in",
                    "2. Profile photo → Settings",
                    "3. Developer settings → Personal access tokens",
                    "4. Tokens (classic) → Generate new token",
                    "5. Give any name, no scopes needed",
                    "6. Copy and paste below"
                ).forEach { Text(it, fontSize = 12.sp, color = Color(0xFF666666)) }
            }

            TextField(
                value = token, onValueChange = { token = it },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                placeholder = { Text("ghp_...", color = Color(0xFF444444), fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Key, null, tint = Color(0xFF6C63FF)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF141414), unfocusedContainerColor = Color(0xFF141414),
                    focusedIndicatorColor = Color(0xFF6C63FF), unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color(0xFFCCCCCC))
            )

            Button(onClick = { onComplete(token) }, modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))) {
                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Token & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onSkip) {
                Text("Skip for now  (limited to 60 req/hour)", color = Color(0xFF555555), fontSize = 13.sp)
            }
            Spacer(Modifier.height(24.dp))
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