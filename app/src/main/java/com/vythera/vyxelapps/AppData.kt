package com.vythera.vyxelapps

import android.app.Application
import kotlinx.coroutines.isActive
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File

enum class AppPlatform(val label: String, val emoji: String) {
    ALL("All", "⬡"), ANDROID("Android", "🤖"),
    WINDOWS("Windows", "🪟"), LINUX("Linux", "🐧"), TV("TV", "📺")
}

data class GitHubRepo(
    val id: Long = 0, val name: String = "", val full_name: String = "",
    val description: String? = null, val stargazers_count: Int = 0,
    val forks_count: Int = 0, val html_url: String = "",
    val owner: RepoOwner = RepoOwner(), val language: String? = null,
    val updated_at: String = ""
)
data class RepoOwner(val login: String = "", val avatar_url: String = "")
data class SearchResponse(val items: List<GitHubRepo> = emptyList())
data class Release(
    val tag_name: String = "", val name: String = "",
    val assets: List<ReleaseAsset> = emptyList(),
    val published_at: String = "",
    val body: String = ""
)
data class ReleaseAsset(
    val name: String = "", val browser_download_url: String = "",
    val size: Long = 0, val content_type: String = ""
)

data class InstallState(
    val isLoadingRelease : Boolean = false,
    val release          : Release? = null,
    val apkAsset         : ReleaseAsset? = null,
    val smartInstall      : SmartInstallResult? = null,   // ← new
    val trustScore        : TrustScore?      = null,
    val downloadProgress : Float? = null,
    val isInstalled      : Boolean = false,
    val packageName      : String? = null,
    val error            : String? = null,
    val downloadId       : Long? = null,
    val repo             : GitHubRepo?  = null
)

data class UserProfile(
    val name     : String = "",
    val age      : String = "",
    val email    : String = "",
    val photoUri : String = "",
    val coverUri : String = ""
)
data class HistoryItem(val repo: GitHubRepo, val viewedAt: Long = System.currentTimeMillis())
data class AppSettings(
    val language    : String  = "English",
    val githubToken : String  = "",
    val sortBy      : String  = "Stars",
    val fontName    : String  = "Default",
    val themeMode   : String  = "System",   // "Light", "Dark", "System"
    val amoledBlack : Boolean = false
)

data class InstallHistoryEntry(
    val repoId      : Long,
    val repoName    : String,
    val ownerLogin  : String,
    val tagName     : String,
    val apkPath     : String,
    val installedAt : Long = System.currentTimeMillis()
)

data class UpdateInfo(
    val repoId       : Long,
    val repoName     : String,
    val currentTag   : String,
    val latestTag    : String,
    val changelog    : String
)

data class ReadmeResponse(
    val content  : String = "",
    val encoding : String = ""
)

data class UiState(
    // ── Categories ────────────────────────────────────────────────────────

    val refreshToken: Int = 0,
    val isSetupDone : Boolean? = null,
    val trending      : List<GitHubRepo> = emptyList(),
    val media         : List<GitHubRepo> = emptyList(),
    val tools         : List<GitHubRepo> = emptyList(),
    val games         : List<GitHubRepo> = emptyList(),
    val browsers      : List<GitHubRepo> = emptyList(),
    val productivity  : List<GitHubRepo> = emptyList(),
    val security      : List<GitHubRepo> = emptyList(),
    val devtools      : List<GitHubRepo> = emptyList(),
    val photoVideo    : List<GitHubRepo> = emptyList(),
    val music         : List<GitHubRepo> = emptyList(),
    val finance       : List<GitHubRepo> = emptyList(),
    val education     : List<GitHubRepo> = emptyList(),
    val fitness       : List<GitHubRepo> = emptyList(),
    val artDesign     : List<GitHubRepo> = emptyList(),
    val news          : List<GitHubRepo> = emptyList(),
    val social        : List<GitHubRepo> = emptyList(),
    val cloudStorage  : List<GitHubRepo> = emptyList(),
    val cooking       : List<GitHubRepo> = emptyList(),
    // ── Platform / Search / SeeAll ────────────────────────────────────────
    val platformApps       : List<GitHubRepo> = emptyList(),
    val searchResults      : List<GitHubRepo> = emptyList(),
    val recommendations    : List<GitHubRepo> = emptyList(),
    val seeAllTitle        : String           = "",
    val seeAllQuery        : String           = "",
    val seeAllApps         : List<GitHubRepo> = emptyList(),
    val seeAllPage         : Int              = 1,
    val isLoadingSeeAll    : Boolean          = false,
    // ── Loading ───────────────────────────────────────────────────────────
    val isLoading          : Boolean          = false,
    val isLoadingMore      : Boolean          = false,
    val error              : String?          = null,
    // ── Preferences ───────────────────────────────────────────────────────
    val searchQuery        : String                  = "",
    val platform           : AppPlatform             = AppPlatform.ALL,
    val trendingPage       : Int                     = 1,
    val installStates      : Map<Long, InstallState> = emptyMap(),
    val profile            : UserProfile             = UserProfile(),
    val history            : List<HistoryItem>       = emptyList(),
    val settings           : AppSettings             = AppSettings(),
    val themeName          : ThemeName               = ThemeName.DARK,
    val accentColor        : Color?                  = null,
    val useMonet           : Boolean                 = false,
    val categoryViewCounts : Map<String, Int>        = emptyMap(),
    val translatedDescriptions : Map<Long, String>   = emptyMap(),
    val isTranslating          : Map<Long, Boolean>  = emptyMap(),
    val favourites     : List<GitHubRepo> = emptyList(),
    val githubUsername : String           = "",
    val installHistory     : List<InstallHistoryEntry> = emptyList(),
    val ignoredVersions    : Set<String>               = emptySet(),
    val updates            : List<UpdateInfo>          = emptyList(),
    val screenshots        : Map<Long, List<String>>   = emptyMap(),
    val isCheckingUpdates  : Boolean                   = false,
    val compareTargetRepo  : GitHubRepo?               = null,
    val isSearching : Boolean = false,
)

// ── Translation ───────────────────────────────────────────────────────────
data class MyMemoryResponse(val responseData: MyMemoryData, val responseStatus: Int = 0)
data class MyMemoryData(val translatedText: String = "")
interface TranslationService {
    @GET("get")
    suspend fun translate(@Query("q") text: String, @Query("langpair") langPair: String): MyMemoryResponse
}
object TranslationClient {
    val service: TranslationService = Retrofit.Builder()
        .baseUrl("https://api.mymemory.translated.net/")
        .addConverterFactory(GsonConverterFactory.create()).build()
        .create(TranslationService::class.java)
}

// ── GitHub API ────────────────────────────────────────────────────────────
interface GitHubService {

    @GET("repos/{owner}/{repo}/readme")
    suspend fun getReadme(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): ReadmeResponse

    @GET("user/starred")
    suspend fun getStarredRepos(
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): List<GitHubRepo>

    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 10
    ): List<Release>

    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String, @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 20, @Query("page") page: Int = 1
    ): SearchResponse

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(@Path("owner") owner: String, @Path("repo") repo: String): Release
}

object RetrofitClient {
    var authToken: String = ""
    private val httpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .connectionPool(okhttp3.ConnectionPool(3, 5, java.util.concurrent.TimeUnit.MINUTES))
        .addInterceptor { chain ->
            val req = chain.request().newBuilder().apply {
                if (authToken.isNotEmpty()) addHeader("Authorization", "Bearer $authToken")
                addHeader("Accept", "application/vnd.github+json")
            }.build()
            chain.proceed(req)
        }.build()

    val service: GitHubService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubService::class.java)
}

// ── Preferences persistence ───────────────────────────────────────────────
class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("vyxel_prefs", Context.MODE_PRIVATE)
    private val gson  = Gson()

    fun saveSetupDone()  = prefs.edit().putBoolean("setup_done", true).apply()
    fun isSetupDone()    = prefs.getBoolean("setup_done", false)
    fun saveLanguageCode(code: String) = prefs.edit().putString("user_language_code", code).apply()

    fun saveAppSettings(s: AppSettings) = prefs.edit().putString("app_settings_v2", gson.toJson(s)).apply()
    fun loadAppSettings(): AppSettings  = fromJson("app_settings_v2") ?: AppSettings()

    fun saveProfile(p: UserProfile) = prefs.edit().putString("profile", gson.toJson(p)).apply()
    fun loadProfile(): UserProfile  = fromJson("profile") ?: UserProfile()

    fun saveSettings(s: AppSettings) = prefs.edit().putString("settings", gson.toJson(s)).apply()
    fun loadSettings(): AppSettings  = fromJson("settings") ?: AppSettings()

    fun saveTheme(t: ThemeName)  = prefs.edit().putString("theme", t.name).apply()
    fun loadTheme(): ThemeName   = try { ThemeName.valueOf(prefs.getString("theme", ThemeName.DARK.name)!!) } catch (_: Exception) { ThemeName.DARK }

    fun saveAccentColor(c: Color?) = prefs.edit().putString("accent", c?.value?.toString() ?: "").apply()
    fun loadAccentColor(): Color? {
        return try {
            val s = prefs.getString("accent", "") ?: ""
            if (s.isEmpty()) null else Color(s.toULong())
        } catch (_: Exception) { null }
    }
    fun saveUseMonet(v: Boolean)  = prefs.edit().putBoolean("monet", v).apply()
    fun loadUseMonet(): Boolean   = prefs.getBoolean("monet", false)

    fun saveHistory(h: List<HistoryItem>) = prefs.edit().putString("history", gson.toJson(h.take(50))).apply()
    fun loadHistory(): List<HistoryItem>  {
        val json = prefs.getString("history", null) ?: return emptyList()
        return try { gson.fromJson(json, object : TypeToken<List<HistoryItem>>() {}.type) ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    fun saveCategoryViews(m: Map<String, Int>) = prefs.edit().putString("catviews", gson.toJson(m)).apply()
    fun loadCategoryViews(): Map<String, Int>  {
        val json = prefs.getString("catviews", null) ?: return emptyMap()
        return try { gson.fromJson(json, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap() } catch (_: Exception) { emptyMap() }
    }

    private inline fun <reified T> fromJson(key: String): T? = try {
        val json = prefs.getString(key, null) ?: return null
        gson.fromJson(json, T::class.java)
    } catch (_: Exception) { null }

    fun saveInstallHistory(h: List<InstallHistoryEntry>) =
        prefs.edit().putString("install_history", gson.toJson(h.takeLast(60))).apply()

    fun loadInstallHistory(): List<InstallHistoryEntry> {
        val json = prefs.getString("install_history", null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<InstallHistoryEntry>>() {}.type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun saveIgnoredVersions(v: Set<String>) =
        prefs.edit().putStringSet("ignored_versions", v).apply()

    fun loadIgnoredVersions(): Set<String> =
        prefs.getStringSet("ignored_versions", emptySet()) ?: emptySet()
}

// ── Smart APK detection ───────────────────────────────────────────────────
data class SmartInstallResult(
    val asset       : ReleaseAsset,
    val reason      : String,
    val isOptimal   : Boolean = true
)

fun detectBestApk(assets: List<ReleaseAsset>): SmartInstallResult? {
    val apks = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
    if (apks.isEmpty()) return null
    if (apks.size == 1) return SmartInstallResult(apks.first(), "Only available package")

    val deviceAbis = android.os.Build.SUPPORTED_ABIS.toList()
    val abiMap = mapOf(
        "arm64-v8a"   to listOf("arm64-v8a", "arm64", "aarch64"),
        "armeabi-v7a" to listOf("armeabi-v7a", "armeabi", "armv7"),
        "x86_64"      to listOf("x86_64", "x64"),
        "x86"         to listOf("x86", "i686")
    )

    // 1. Universal is always safe
    apks.firstOrNull { it.name.contains("universal", ignoreCase = true) }
        ?.let { return SmartInstallResult(it, "Universal — works on all devices") }

    // 2. Match device ABI in priority order
    for (abi in deviceAbis) {
        val keywords = abiMap[abi] ?: continue
        for (kw in keywords) {
            apks.firstOrNull { it.name.contains(kw, ignoreCase = true) }
                ?.let { return SmartInstallResult(it, "Optimised for $abi (your device)") }
        }
    }

    // 3. Fallback to largest APK (usually most complete)
    val fallback = apks.maxByOrNull { it.size } ?: apks.first()
    return SmartInstallResult(fallback, "Default package", isOptimal = false)
}

// ── Trust Score ────────────────────────────────────────────────────────────
data class TrustScore(
    val score           : Int,
    val daysSinceUpdate : Int,
    val releaseCount    : Int,
    val forks           : Int,
    val stars           : Int
) {
    val label: String get() = when {
        score >= 85 -> "Highly Trusted"
        score >= 65 -> "Trusted"
        score >= 45 -> "Moderate"
        score >= 25 -> "Low Trust"
        else        -> "Unverified"
    }
    val safeColor: androidx.compose.ui.graphics.Color get() = when {
        score >= 85 -> androidx.compose.ui.graphics.Color(0xFF1DB954.toInt())
        score >= 65 -> androidx.compose.ui.graphics.Color(0xFF4CAF50.toInt())
        score >= 45 -> androidx.compose.ui.graphics.Color(0xFFFF9800.toInt())
        score >= 25 -> androidx.compose.ui.graphics.Color(0xFFFF5722.toInt())
        else        -> androidx.compose.ui.graphics.Color(0xFF9E9E9E.toInt())
    }
}

fun calculateTrustScore(repo: GitHubRepo, releaseCount: Int): TrustScore {
    var score = 0

    // Stars (0–30)
    score += when {
        repo.stargazers_count >= 10_000 -> 30
        repo.stargazers_count >= 1_000  -> 24
        repo.stargazers_count >= 500    -> 18
        repo.stargazers_count >= 100    -> 12
        repo.stargazers_count >= 10     -> 6
        else                            -> 0
    }

    // Last updated (0–25)
    val days = try {
        val sdf     = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        val updated = sdf.parse(repo.updated_at)?.time ?: 0L
        ((System.currentTimeMillis() - updated) / 86_400_000L).toInt()
    } catch (_: Exception) { 999 }
    score += when {
        days < 7   -> 25
        days < 30  -> 20
        days < 90  -> 14
        days < 180 -> 8
        days < 365 -> 3
        else       -> 0
    }

    // Forks (0–20)
    score += when {
        repo.forks_count >= 1_000 -> 20
        repo.forks_count >= 200   -> 15
        repo.forks_count >= 50    -> 10
        repo.forks_count >= 10    -> 5
        repo.forks_count >= 1     -> 2
        else                      -> 0
    }

    // Releases (0–20)
    score += when {
        releaseCount >= 10 -> 20
        releaseCount >= 5  -> 14
        releaseCount >= 2  -> 9
        releaseCount >= 1  -> 5
        else               -> 0
    }

    // Has description (0–5)
    if (!repo.description.isNullOrEmpty()) score += 5

    return TrustScore(minOf(100, score), days, releaseCount, repo.forks_count, repo.stargazers_count)
}

// ── Curated collections ────────────────────────────────────────────────────
data class AppCollection(val emoji: String, val title: String, val query: String)

val COLLECTIONS = listOf(
    AppCollection("🔒", "Privacy Essentials",    "topic:android privacy"),
    AppCollection("🎵", "Best Media Apps",        "topic:android media player"),
    AppCollection("🛠", "Root & Magisk Tools",    "topic:android magisk root"),
    AppCollection("📖", "Reading & E-Books",      "topic:android ebook reader"),
    AppCollection("🌐", "Browsers",               "topic:android browser"),
    AppCollection("💬", "Messaging",              "topic:android messaging privacy"),
    AppCollection("📸", "Camera & Gallery",       "topic:android camera"),
    AppCollection("🎮", "Emulators",              "topic:android emulator game"),
    AppCollection("🔧", "Dev Tools",              "topic:android developer-tools"),
    AppCollection("☁️", "Sync & Backup",          "topic:android backup sync")
)
// ── ViewModel ─────────────────────────────────────────────────────────────
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private var loadPage = 1

    fun openCollection(collection: AppCollection) {
        openSeeAll(collection.emoji + " " + collection.title, collection.query)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
            }
        }
        return dp[a.length][b.length]
    }

    private fun fuzzyMatch(text: String, query: String): Boolean {
        val t = text.lowercase()
        val q = query.lowercase()
        if (t.contains(q)) return true
        val words = t.split(" ", "-", "_", ".")
        val allowed = maxOf(1, q.length / 4)
        return words.any { levenshtein(it, q) <= allowed } ||
                levenshtein(t.take(q.length + 2), q) <= allowed
    }

    fun completeSetup(token: String) {
        if (token.isNotEmpty()) {
            val s = state.settings.copy(githubToken = token)
            updateSettings(s)
        }
        prefs.saveSetupDone()
        state = state.copy(isSetupDone = true)
        loadAll()
    }

    fun toggleFavourite(repo: GitHubRepo) {
        val fav = state.favourites
        val newFav = if (fav.any { it.id == repo.id }) fav.filter { it.id != repo.id }
        else listOf(repo) + fav
        state = state.copy(favourites = newFav)
        prefs.saveHistory(newFav.map { HistoryItem(it) }) // reuse history slot for simplicity
    }

    fun setGithubUsername(name: String) { state = state.copy(githubUsername = name) }
    private val ctx   = app.applicationContext
    private val prefs = PreferencesManager(ctx)
    private val downloadJobs = mutableMapOf<Long, kotlinx.coroutines.Job>()

    private var searchJob: kotlinx.coroutines.Job? = null

    private var loadJob     : kotlinx.coroutines.Job? = null
    private var platformJob : kotlinx.coroutines.Job? = null

    var state by mutableStateOf(UiState())
        private set

    init {
        val savedSettings = prefs.loadAppSettings()
        RetrofitClient.authToken = savedSettings.githubToken
        state = state.copy(
            isSetupDone        = prefs.isSetupDone(),
            settings           = savedSettings,
            profile            = prefs.loadProfile(),
            history            = prefs.loadHistory(),
            accentColor        = prefs.loadAccentColor(),
            useMonet           = prefs.loadUseMonet(),
            themeName          = prefs.loadTheme(),
            categoryViewCounts = prefs.loadCategoryViews(),
            installHistory  = prefs.loadInstallHistory(),
            ignoredVersions = prefs.loadIgnoredVersions()
        )
        if (prefs.isSetupDone()) loadAll()
    }

    private suspend fun <T> safeApi(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null
        }
    }

    private val cache    = java.util.concurrent.ConcurrentHashMap<String, Pair<List<GitHubRepo>, Long>>()
    private val CACHE_MS = 15 * 60 * 1000L

    private suspend fun searchCached(
        query   : String,
        perPage : Int = 20,
        page    : Int = 1
    ): SearchResponse? {
        val key    = "$query|$page|$perPage"
        val cached = cache[key]
        if (cached != null && System.currentTimeMillis() - cached.second < CACHE_MS) {
            return SearchResponse(cached.first)
        }
        val result = safeApi { RetrofitClient.service.searchRepos(query, perPage = perPage, page = page) }
            ?: return null
        val safeItems: List<GitHubRepo> = try { result.items } catch (_: Throwable) { null } ?: emptyList()
        cache[key] = safeItems to System.currentTimeMillis()
        return SearchResponse(safeItems)
    }

    fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            delay(80)
            loadPage = (loadPage % 8) + 1
            state = state.copy(isLoading = true, error = null, trendingPage = 1, refreshToken = state.refreshToken + 1)

            // Batch 1
            try {
                val t = searchCached("topic:android apk stars:>100",          perPage = 20, page = loadPage)
                val m = searchCached("topic:android media player stars:>50",  perPage = 20, page = loadPage)
                val u = searchCached("topic:android utility tool stars:>50",  perPage = 20, page = loadPage)
                val g = searchCached("topic:android game emulator stars:>50", perPage = 20, page = loadPage)
                if (!isActive) return@launch
                state = state.copy(
                    trending  = t?.items?.shuffled() ?: emptyList(),
                    media     = m?.items?.shuffled() ?: emptyList(),
                    tools     = u?.items?.shuffled() ?: emptyList(),
                    games     = g?.items?.shuffled() ?: emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                state = state.copy(isLoading = false,
                    error = "Could not load apps. Add a GitHub token in Settings.")
                return@launch
            }

            updateRecommendations()

            // Batch 2
            if (!isActive) return@launch
            delay(700)
            try {
                val br = searchCached("topic:android browser privacy stars:>50",    perPage = 20, page = loadPage)
                val pr = searchCached("topic:android productivity notes stars:>50", perPage = 20, page = loadPage)
                val se = searchCached("topic:android security stars:>50",           perPage = 20, page = loadPage)
                val de = searchCached("topic:android developer-tools stars:>50",    perPage = 20, page = loadPage)
                if (!isActive) return@launch
                state = state.copy(
                    browsers     = br?.items?.shuffled() ?: emptyList(),
                    productivity = pr?.items?.shuffled() ?: emptyList(),
                    security     = se?.items?.shuffled() ?: emptyList(),
                    devtools     = de?.items?.shuffled() ?: emptyList()
                )
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }

            // Batch 3
            if (!isActive) return@launch
            delay(900)
            try {
                val pv = searchCached("topic:android photo video editor stars:>100", perPage = 20, page = loadPage)
                val mu = searchCached("topic:android music audio stars:>100",         perPage = 20, page = loadPage)
                val fi = searchCached("topic:android finance banking stars:>100",     perPage = 20, page = loadPage)
                val ed = searchCached("topic:android education learning stars:>100",  perPage = 20, page = loadPage)
                if (!isActive) return@launch
                state = state.copy(
                    photoVideo = pv?.items?.shuffled() ?: emptyList(),
                    music      = mu?.items?.shuffled() ?: emptyList(),
                    finance    = fi?.items?.shuffled() ?: emptyList(),
                    education  = ed?.items?.shuffled() ?: emptyList()
                )
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }

            // Batch 4
            if (!isActive) return@launch
            delay(900)
            try {
                val ft = searchCached("topic:android fitness health workout stars:>100", perPage = 20, page = loadPage)
                val ar = searchCached("topic:android art design creative stars:>100",    perPage = 20, page = loadPage)
                val nw = searchCached("topic:android news reader stars:>100",             perPage = 20, page = loadPage)
                val sc = searchCached("topic:android social network stars:>100",          perPage = 20, page = loadPage)
                val cs = searchCached("topic:android cloud storage files stars:>100",     perPage = 20, page = loadPage)
                val ck = searchCached("topic:android cooking food recipe stars:>50",      perPage = 20, page = loadPage)
                if (!isActive) return@launch
                state = state.copy(
                    fitness      = ft?.items?.shuffled() ?: emptyList(),
                    artDesign    = ar?.items?.shuffled() ?: emptyList(),
                    news         = nw?.items?.shuffled() ?: emptyList(),
                    social       = sc?.items?.shuffled() ?: emptyList(),
                    cloudStorage = cs?.items?.shuffled() ?: emptyList(),
                    cooking      = ck?.items?.shuffled() ?: emptyList()
                )
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }

            updateRecommendations()
        }
    }

    // ── Recommendations ───────────────────────────────────────────────────
    private fun updateRecommendations() {
        val counts = state.categoryViewCounts
        val categoryMap = mapOf(
            "trending" to state.trending, "media" to state.media,
            "tools" to state.tools,       "games" to state.games,
            "browsers" to state.browsers, "productivity" to state.productivity,
            "photo" to state.photoVideo,  "music" to state.music,
            "finance" to state.finance,   "education" to state.education,
            "fitness" to state.fitness,   "artDesign" to state.artDesign
        )
        val recs = if (counts.isEmpty()) {
            (state.trending + state.media).shuffled().take(20)
        } else {
            counts.entries.sortedByDescending { it.value }.take(4)
                .flatMap { categoryMap[it.key]?.take(8) ?: emptyList() }
                .distinctBy { it.id }.shuffled().take(20)
        }
        state = state.copy(recommendations = recs)
    }

    fun trackCategory(category: String) {
        val counts = state.categoryViewCounts.toMutableMap()
        counts[category] = (counts[category] ?: 0) + 1
        state = state.copy(categoryViewCounts = counts)
        prefs.saveCategoryViews(counts)
        updateRecommendations()
    }

    // ── Infinite scroll ───────────────────────────────────────────────────
    fun loadMoreTrending() {
        if (state.isLoadingMore) return
        val next = state.trendingPage + 1
        viewModelScope.launch {
            state = state.copy(isLoadingMore = true)
            try {
                val more = RetrofitClient.service.searchRepos("topic:android apk stars:>500", perPage = 20, page = next)
                state = state.copy(trending = state.trending + more.items, trendingPage = next, isLoadingMore = false)
            } catch (_: Exception) { state = state.copy(isLoadingMore = false) }
        }
    }

    // ── Platform filter ───────────────────────────────────────────────────
    fun setPlatform(p: AppPlatform) {
        state = state.copy(platform = p)
        if (p != AppPlatform.ALL) loadPlatformApps(p)
    }
    private fun loadPlatformApps(platform: AppPlatform) {
        platformJob?.cancel()
        platformJob = viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, platformApps = emptyList())
            val q = when (platform) {
                AppPlatform.ANDROID -> "topic:android apk stars:>200"
                AppPlatform.WINDOWS -> "topic:windows stars:>200"
                AppPlatform.LINUX   -> "topic:linux stars:>200"
                AppPlatform.TV      -> "topic:android-tv stars:>30"
                else                -> return@launch
            }
            try {
                val r = RetrofitClient.service.searchRepos(q, perPage = 30)
                state = state.copy(platformApps = r.items, isLoading = false)
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error     = "Could not load ${platform.label} apps. Check your connection."
                )
            }
        }
    }

    // ── Search ────────────────────────────────────────────────────────────
    fun onSearch(q: String) {
        state = state.copy(searchQuery = q)
        if (q.isBlank()) {
            state = state.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        val allLoaded = (state.trending + state.media + state.tools + state.games +
                state.browsers + state.productivity + state.security + state.devtools +
                state.photoVideo + state.music + state.finance + state.education +
                state.fitness + state.artDesign + state.news + state.social +
                state.cloudStorage + state.cooking).distinctBy { it.id }

        val localMatches = allLoaded.filter { repo ->
            fuzzyMatch(repo.name, q) ||
                    fuzzyMatch(repo.owner.login, q) ||
                    (!repo.description.isNullOrEmpty() && fuzzyMatch(repo.description, q))
        }.sortedByDescending { it.stargazers_count }

        state = state.copy(searchResults = localMatches, isSearching = true)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(120)
            try {
                val seen    = localMatches.map { it.id }.toMutableSet()
                val results = localMatches.toMutableList()

                safeApi { RetrofitClient.service.searchRepos(q, perPage = 20) }
                    ?.items?.forEach { if (seen.add(it.id)) results.add(it) }

                safeApi { RetrofitClient.service.searchRepos("$q in:name", perPage = 10) }
                    ?.items?.forEach { if (seen.add(it.id)) results.add(it) }

                if (results.size < 5 && q.length >= 4) {
                    safeApi { RetrofitClient.service.searchRepos(q.dropLast(1), perPage = 10) }
                        ?.items?.forEach { if (seen.add(it.id)) results.add(it) }
                }

                state = state.copy(
                    searchResults = results.sortedByDescending { it.stargazers_count },
                    isSearching = false
                )
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    state = state.copy(isSearching = false)
                    if (localMatches.isEmpty()) state = state.copy(searchResults = emptyList())
                }
            }
        }
    }

    // ── See All ───────────────────────────────────────────────────────────
    fun openSeeAll(title: String, query: String) {
        state = state.copy(
            seeAllTitle     = title,
            seeAllQuery     = query,
            seeAllApps      = emptyList(),
            seeAllPage      = 1,
            isLoadingSeeAll = true
        )
        viewModelScope.launch {
            try {
                val r = RetrofitClient.service.searchRepos(query, perPage = 30, page = 1)
                state = state.copy(seeAllApps = r.items, isLoadingSeeAll = false)
            } catch (e: Exception) {
                state = state.copy(isLoadingSeeAll = false)
            }
        }
    }
    fun loadMoreSeeAll() {
        if (state.isLoadingSeeAll || state.seeAllQuery.isEmpty()) return
        val next = state.seeAllPage + 1
        viewModelScope.launch {
            state = state.copy(isLoadingSeeAll = true)
            try {
                val r = RetrofitClient.service.searchRepos(state.seeAllQuery, perPage = 30, page = next)
                state = state.copy(
                    seeAllApps      = state.seeAllApps + r.items,
                    seeAllPage      = next,
                    isLoadingSeeAll = false
                )
            } catch (_: Exception) {
                state = state.copy(isLoadingSeeAll = false)
            }
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────
    fun updateProfile(p: UserProfile) { state = state.copy(profile = p); prefs.saveProfile(p) }

    // ── History ───────────────────────────────────────────────────────────
    fun addToHistory(repo: GitHubRepo) {
        val filtered = state.history.filter { it.repo.id != repo.id }
        val newH = listOf(HistoryItem(repo)) + filtered
        state = state.copy(history = newH); prefs.saveHistory(newH)
    }
    fun clearHistory() { state = state.copy(history = emptyList()); prefs.saveHistory(emptyList()) }

    // ── Theme & settings ──────────────────────────────────────────────────
    fun setTheme(t: ThemeName)  { state = state.copy(themeName = t); prefs.saveTheme(t) }
    fun setAccentColor(c: Color?) { state = state.copy(accentColor = c, useMonet = false); prefs.saveAccentColor(c); prefs.saveUseMonet(false) }
    fun setUseMonet(v: Boolean)   { state = state.copy(useMonet = v, accentColor = null); prefs.saveUseMonet(v); prefs.saveAccentColor(null) }
    fun updateSettings(s: AppSettings) {
        state = state.copy(settings = s)
        RetrofitClient.authToken = s.githubToken
        prefs.saveSettings(s)
        prefs.saveAppSettings(s)
    }

    // ── Release & install ─────────────────────────────────────────────────
    fun fetchRelease(repo: GitHubRepo) {
        if (state.installStates[repo.id]?.release != null) return
        viewModelScope.launch {
            updateInstall(repo.id) { copy(isLoadingRelease = true, error = null) }
            try {
                var foundRelease : Release?     = null
                var foundApk     : ReleaseAsset? = null
                var releaseCount : Int           = 0

                // Try latest release first
                try {
                    val latest = RetrofitClient.service.getLatestRelease(repo.owner.login, repo.name)
                    foundRelease = latest
                    foundApk     = detectBestApk(latest.assets)?.asset
                    releaseCount = 1
                } catch (_: Exception) {}

                // Scan all releases if no APK yet
                if (foundApk == null) {
                    try {
                        val releases = RetrofitClient.service.getReleases(repo.owner.login, repo.name, 10)
                        releaseCount = releases.size
                        for (r in releases) {
                            if (foundRelease == null) foundRelease = r
                            val apk = detectBestApk(r.assets)?.asset
                            if (apk != null) { foundRelease = r; foundApk = apk; break }
                        }
                    } catch (_: Exception) {}
                }

                if (foundRelease != null) {
                    val smart = detectBestApk(foundRelease.assets)
                    val trust = calculateTrustScore(repo, releaseCount)
                    updateInstall(repo.id) {
                        copy(
                            isLoadingRelease = false,
                            release          = foundRelease,
                            apkAsset         = smart?.asset ?: foundApk,
                            smartInstall     = smart,
                            trustScore       = trust
                        )
                    }
                } else {
                    updateInstall(repo.id) {
                        copy(isLoadingRelease = false, error = "No releases found.")
                    }
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException)
                    updateInstall(repo.id) { copy(isLoadingRelease = false, error = "Could not load release info.") }
            }
        }
    }

    fun downloadAndInstall(repo: GitHubRepo, asset: ReleaseAsset) {
        val job = viewModelScope.launch {
            updateInstall(repo.id) { copy(downloadProgress = 0f, error = null, repo = repo) }
            val outFile = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${repo.name}_${asset.name}")
            try {
                val dm  = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val req = DownloadManager.Request(Uri.parse(asset.browser_download_url))
                    .setTitle("Installing ${repo.name}").setDescription("Vyxel Apps")
                    .setDestinationUri(Uri.fromFile(outFile))
                    .setAllowedOverMetered(true).setAllowedOverRoaming(true)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                val dlId = dm.enqueue(req)
                updateInstall(repo.id) { copy(downloadId = dlId) }
                while (true) {
                    val cur = dm.query(DownloadManager.Query().setFilterById(dlId))
                    if (!cur.moveToFirst()) { cur.close(); break }
                    val status = cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val done   = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total  = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    cur.close()
                    if (total > 0) updateInstall(repo.id) { copy(downloadProgress = done.toFloat() / total) }
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val pkg = try { @Suppress("DEPRECATION") ctx.packageManager.getPackageArchiveInfo(outFile.absolutePath, 0)?.packageName } catch (_: Exception) { null }
                            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", outFile)
                            ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/vnd.android.package-archive")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                            updateInstall(repo.id) { copy(downloadProgress = null, downloadId = null, packageName = pkg) }

                            val tag = state.installStates[repo.id]?.release?.tag_name ?: "unknown"
                            recordInstall(repo, tag, outFile.absolutePath)
                            if (pkg != null) {
                                viewModelScope.launch { repeat(30) { delay(2000); if (installed(pkg)) { updateInstall(repo.id) { copy(isInstalled = true) }; return@launch } } }
                            }
                            break
                        }
                        DownloadManager.STATUS_FAILED -> { updateInstall(repo.id) { copy(downloadProgress = null, downloadId = null, error = "Download failed.") }; break }
                        else -> delay(400)
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e  // ✅ always rethrow
                updateInstall(repo.id) { copy(downloadProgress = null, error = "Error: ${e.message}") }
            }
        }
        downloadJobs[repo.id] = job
    }

    fun downloadOnly(repo: GitHubRepo, asset: ReleaseAsset) {
        val job = viewModelScope.launch {
            updateInstall(repo.id) { copy(downloadProgress = 0f, error = null, repo = repo) }
            try {
                val dm  = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val req = DownloadManager.Request(Uri.parse(asset.browser_download_url))
                    .setTitle("Downloading ${repo.name}").setDescription("Saved to Downloads")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${repo.name}.apk")
                    .setAllowedOverMetered(true).setAllowedOverRoaming(true)
                val dlId = dm.enqueue(req)
                updateInstall(repo.id) { copy(downloadId = dlId) }
                while (true) {
                    val cur = dm.query(DownloadManager.Query().setFilterById(dlId))
                    if (!cur.moveToFirst()) { cur.close(); break }
                    val status = cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val done   = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total  = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    cur.close()
                    if (total > 0) updateInstall(repo.id) { copy(downloadProgress = done.toFloat() / total) }
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> { updateInstall(repo.id) { copy(downloadProgress = null, downloadId = null) }; break }
                        DownloadManager.STATUS_FAILED    -> { updateInstall(repo.id) { copy(downloadProgress = null, downloadId = null, error = "Download failed.") }; break }
                        else -> delay(400)
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e  // ✅ always rethrow
                updateInstall(repo.id) { copy(downloadProgress = null, error = "Error: ${e.message}") }
            }
        }
        downloadJobs[repo.id] = job
    }

    fun cancelDownload(repo: GitHubRepo) {
        val dlId = state.installStates[repo.id]?.downloadId
        // Clear UI state first so button disappears instantly
        updateInstall(repo.id) { copy(downloadProgress = null, downloadId = null, error = null) }
        // Cancel the coroutine
        downloadJobs[repo.id]?.cancel()
        downloadJobs.remove(repo.id)
        // Tell DownloadManager to stop
        if (dlId != null) {
            try {
                (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(dlId)
            } catch (_: Exception) {}
        }
    }

    fun uninstall(repo: GitHubRepo) {
        val installState = state.installStates[repo.id] ?: return
        val pkg = installState.packageName

        if (pkg.isNullOrEmpty()) {
            // Try to find by APK file
            val apkFile = ctx.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                ?.listFiles()?.firstOrNull { it.name.contains(repo.name, ignoreCase = true) }
            val detectedPkg = apkFile?.let {
                try { ctx.packageManager.getPackageArchiveInfo(it.absolutePath, 0)?.packageName }
                catch (_: Exception) { null }
            }
            if (detectedPkg != null) {
                updateInstall(repo.id) { copy(packageName = detectedPkg) }
                launchUninstall(repo.id, detectedPkg)
            }
            return
        }
        launchUninstall(repo.id, pkg)
    }

    private fun launchUninstall(repoId: Long, pkg: String) {
        try {
            ctx.startActivity(
                android.content.Intent(android.content.Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = android.net.Uri.parse("package:$pkg")
                    putExtra(android.content.Intent.EXTRA_RETURN_RESULT, false)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            viewModelScope.launch {
                repeat(40) {
                    delay(2000)
                    try {
                        ctx.packageManager.getPackageInfo(pkg, 0)
                    } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
                        updateInstall(repoId) { copy(isInstalled = false) }
                        return@launch
                    }
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                updateInstall(repoId) { copy(error = "Could not launch uninstaller: ${e.message}") }
            }
        }
    }

    fun refreshInstall(id: Long) {
        val pkg = state.installStates[id]?.packageName ?: return
        updateInstall(id) { copy(isInstalled = installed(pkg)) }
    }

    fun translateDescription(repo: GitHubRepo) {
        val desc = repo.description ?: return
        val lang = when (state.settings.language) {
            "Hindi"    -> "hi"
            "Spanish"  -> "es"
            "French"   -> "fr"
            "German"   -> "de"
            "Japanese" -> "ja"
            else       -> return
        }
        viewModelScope.launch {
            state = state.copy(isTranslating = state.isTranslating + (repo.id to true))
            try {
                val r = TranslationClient.service.translate(desc, "en|$lang")
                state = state.copy(
                    translatedDescriptions = state.translatedDescriptions + (repo.id to r.responseData.translatedText),
                    isTranslating = state.isTranslating + (repo.id to false)
                )
            } catch (_: Exception) {
                state = state.copy(isTranslating = state.isTranslating + (repo.id to false))
            }
        }
    }

    private fun installed(pkg: String) = try { ctx.packageManager.getPackageInfo(pkg, 0); true } catch (_: PackageManager.NameNotFoundException) { false }
    private fun updateInstall(id: Long, block: InstallState.() -> InstallState) {
        val cur = state.installStates[id] ?: InstallState()
        state = state.copy(installStates = state.installStates + (id to cur.block()))
    }

    // ── Screenshots fetch ─────────────────────────────────────────────────
    fun fetchScreenshots(repo: GitHubRepo) {
        if (state.screenshots.containsKey(repo.id)) return
        viewModelScope.launch {
            try {
                val readme = RetrofitClient.service.getReadme(repo.owner.login, repo.name)
                val md = if (readme.encoding == "base64") {
                    String(android.util.Base64.decode(readme.content.replace("\n", ""), android.util.Base64.DEFAULT))
                } else readme.content
                val regex = Regex("""!\[[^\]]*\]\(([^)]+\.(?:png|jpg|jpeg|gif|webp))[^)]*\)""", RegexOption.IGNORE_CASE)
                val urls = regex.findAll(md).map { it.groupValues[1] }
                    .map { url ->
                        if (url.startsWith("http")) url
                        else "https://raw.githubusercontent.com/${repo.full_name}/main/${url.trimStart('/', '.')}"
                    }.distinct().take(8).toList()
                state = state.copy(screenshots = state.screenshots + (repo.id to urls))
            } catch (_: Exception) {
                state = state.copy(screenshots = state.screenshots + (repo.id to emptyList()))
            }
        }
    }

    // ── Update checks ─────────────────────────────────────────────────────
    fun checkForUpdatesNow() {
        if (state.isCheckingUpdates) return
        viewModelScope.launch {
            state = state.copy(isCheckingUpdates = true)
            val history = state.installHistory.distinctBy { it.repoId }
            val updates = mutableListOf<UpdateInfo>()
            for (entry in history) {
                try {
                    val r = RetrofitClient.service.getLatestRelease(entry.ownerLogin, entry.repoName)
                    val key = "${entry.repoId}:${r.tag_name}"
                    if (r.tag_name != entry.tagName && key !in state.ignoredVersions) {
                        updates.add(UpdateInfo(entry.repoId, entry.repoName, entry.tagName, r.tag_name, r.body))
                    }
                } catch (_: Exception) {}
            }
            state = state.copy(updates = updates, isCheckingUpdates = false)
        }
    }

    fun ignoreVersion(repoId: Long, tag: String) {
        val key = "$repoId:$tag"
        val updated = state.ignoredVersions + key
        state = state.copy(
            ignoredVersions = updated,
            updates = state.updates.filter { "${it.repoId}:${it.latestTag}" != key }
        )
        prefs.saveIgnoredVersions(updated)
    }

    // ── Install history & rollback ────────────────────────────────────────
    fun recordInstall(repo: GitHubRepo, tagName: String, apkPath: String) {
        val entry = InstallHistoryEntry(
            repoId = repo.id, repoName = repo.name,
            ownerLogin = repo.owner.login, tagName = tagName, apkPath = apkPath
        )
        val sameRepo = state.installHistory.filter { it.repoId == repo.id } + entry
        val others   = state.installHistory.filter { it.repoId != repo.id }
        if (sameRepo.size > 3) {
            sameRepo.dropLast(3).forEach { try { java.io.File(it.apkPath).delete() } catch (_: Exception) {} }
        }
        val pruned = others + sameRepo.takeLast(3)
        state = state.copy(installHistory = pruned)
        prefs.saveInstallHistory(pruned)
    }

    fun rollbackTo(entry: InstallHistoryEntry) {
        try {
            val file = java.io.File(entry.apkPath)
            if (!file.exists()) {
                state = state.copy(error = "APK file not found for rollback")
                return
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                ctx, "${ctx.packageName}.provider", file
            )
            ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            state = state.copy(error = "Rollback failed: ${e.message}")
        }
    }

    // ── Starred repos sync ────────────────────────────────────────────────
    fun syncStarredRepos() {
        if (state.settings.githubToken.isEmpty()) {
            state = state.copy(error = "Sign in with GitHub token first")
            return
        }
        viewModelScope.launch {
            try {
                val starred = RetrofitClient.service.getStarredRepos(perPage = 50)
                val merged = (starred + state.favourites).distinctBy { it.id }
                state = state.copy(favourites = merged)
                prefs.saveHistory(merged.map { HistoryItem(it) })
            } catch (e: Exception) {
                state = state.copy(error = "Could not sync starred: ${e.message}")
            }
        }
    }

    // ── App comparison ────────────────────────────────────────────────────
    fun setCompareTarget(repo: GitHubRepo?) { state = state.copy(compareTargetRepo = repo) }
}