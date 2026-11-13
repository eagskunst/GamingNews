package com.eagskunst.emmanuel.gamingnews.ui.gamedetail

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameDetails
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameLinkType
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRating
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.nextRelease
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesUiEvent
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReminderOffsetSheet
import kotlinx.coroutines.withTimeoutOrNull
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun GameDetailScreen(
    viewModel: GameDetailViewModel,
    onBack: () -> Unit,
    onShare: (name: String, url: String) -> Unit,
    onOpenGame: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val permissionDeniedMessage = stringResource(R.string.notification_permission_rationale)
    val undoLabel = stringResource(R.string.action_undo)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onPermissionResult
    )

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ReleasesUiEvent.RequestNotificationPermission ->
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                is ReleasesUiEvent.ShowPermissionDenied ->
                    snackbarHostState.showSnackbar(permissionDeniedMessage)
                is ReleasesUiEvent.ShowReminderRemoved -> {
                    // Same cap as the Releases bell: an action snackbar never times out alone.
                    val result = withTimeoutOrNull(UNDO_SNACKBAR_TIMEOUT_MS) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.reminder_removed, event.game.name),
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Long
                        )
                    }
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoUnfollow(event.game)
                }
            }
        }
    }

    GameDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onShare = onShare,
        onOpenGame = onOpenGame,
        onRetry = viewModel::retry,
        onFollowClick = viewModel::onFollowClick
    )

    val pendingRelease = uiState.pendingRelease
    if (uiState.showOffsetPicker && pendingRelease != null) {
        ReminderOffsetSheet(
            release = pendingRelease,
            onConfirm = viewModel::onOffsetsConfirmed,
            onDismiss = viewModel::onPickerDismissed
        )
    }
}

@Composable
fun GameDetailContent(
    uiState: GameDetailUiState,
    onBack: () -> Unit,
    onShare: (name: String, url: String) -> Unit,
    onOpenGame: (Long) -> Unit,
    onRetry: () -> Unit,
    onFollowClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val details = uiState.details
    val showFollowBar = details != null && uiState.pendingRelease != null
    Scaffold(
        // The header draws under the status bar itself (hero image or padded buttons).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showFollowBar) {
                FollowBar(isFollowed = uiState.isFollowed, onClick = onFollowClick)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // The Follow bar pads for the system navigation bar; without it the content must.
                .then(if (showFollowBar) Modifier else Modifier.navigationBarsPadding())
        ) {
            when {
                details != null -> DetailBody(
                    details = details,
                    loadImages = uiState.loadImages,
                    todayEpochDay = uiState.todayEpochDay,
                    onBack = onBack,
                    onShare = onShare,
                    onOpenGame = onOpenGame
                )
                else -> {
                    HeaderButtons(onBack = onBack, onShare = null)
                    if (uiState.hasError) {
                        ErrorState(onRetry = onRetry, modifier = Modifier.align(Alignment.Center))
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.game_detail_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun FollowBar(isFollowed: Boolean, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        val modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 48.dp)
            .testTag("game_detail_follow")
        if (isFollowed) {
            FilledTonalButton(onClick = onClick, modifier = modifier) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                Text(
                    text = stringResource(R.string.game_detail_following),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            Button(onClick = onClick, modifier = modifier) {
                Icon(Icons.Outlined.Notifications, contentDescription = null)
                Text(
                    text = stringResource(R.string.game_detail_follow),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBody(
    details: GameDetails,
    loadImages: Boolean,
    todayEpochDay: Long,
    onBack: () -> Unit,
    onShare: (name: String, url: String) -> Unit,
    onOpenGame: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val shareAction = details.gameUrl?.let { url -> { onShare(details.name, url) } }
    val heroUrl = details.screenshots.firstOrNull()?.takeIf { loadImages }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box {
            if (heroUrl != null) {
                AsyncImage(
                    model = heroUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            if (heroUrl != null) {
                // Status bar icons follow the theme, not the image; a scrim keeps them legible.
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
            HeaderButtons(onBack = onBack, onShare = shareAction)
        }
        TitleRow(details = details, loadImages = loadImages)
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            TAB_TITLES.forEachIndexed { index, titleRes ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(stringResource(titleRes)) }
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            when (selectedTab) {
                0 -> AboutTab(details, todayEpochDay)
                1 -> MediaTab(details, loadImages)
                else -> RelatedTab(details, loadImages, onOpenGame)
            }
        }
    }
}

@Composable
private fun HeaderButtons(onBack: () -> Unit, onShare: (() -> Unit)?) {
    // Opaque surface buttons stay legible over any hero image and in every theme.
    val colors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        FilledTonalIconButton(onClick = onBack, colors = colors) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (onShare != null) {
            FilledTonalIconButton(onClick = onShare, colors = colors) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.game_detail_share_cd)
                )
            }
        }
    }
}

@Composable
private fun TitleRow(details: GameDetails, loadImages: Boolean) {
    val subtitle = listOfNotNull(
        details.developers.firstOrNull(),
        details.platformReleases.firstOrNull()?.dateLabel()
    ).joinToString(" · ")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (loadImages && details.coverUrl != null) {
            AsyncImage(
                model = details.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(width = 60.dp, height = 80.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = details.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// region About

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutTab(details: GameDetails, todayEpochDay: Long) {
    StatTiles(details, todayEpochDay)
    details.summary?.let { Summary(it) }
    if (details.genres.isNotEmpty()) {
        Section(title = stringResource(R.string.game_detail_genres)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                details.genres.forEach { genre ->
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
    if (details.platformReleases.isNotEmpty()) PlatformsSection(details.platformReleases)
    DetailRows(details)
}

/**
 * Scores once a game has them; before that, a countdown to the next release plus IGDB hype.
 * A released game without ratings and nothing upcoming shows no tiles at all.
 */
@Composable
private fun StatTiles(details: GameDetails, todayEpochDay: Long) {
    val numberFormat = remember { NumberFormat.getIntegerInstance() }
    val tiles = buildList<Pair<String, String>> {
        if (details.userRating != null || details.criticRating != null) {
            details.userRating?.let { add(it.tile(R.plurals.game_detail_user_score_label, numberFormat)) }
            details.criticRating?.let { add(it.tile(R.plurals.game_detail_critic_score_label, numberFormat)) }
        } else {
            val next = details.nextRelease(todayEpochDay) ?: return@buildList
            add(next.countdownTile(todayEpochDay))
            details.hypes?.let { hypes ->
                add(
                    numberFormat.format(hypes) to
                        pluralStringResource(R.plurals.game_detail_hyped_label, hypes)
                )
            }
        }
    }
    if (tiles.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        tiles.forEachIndexed { index, (value, label) ->
            val (container, content) = if (index == 0) {
                MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
            }
            StatTile(value, label, container, content, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun GameRating.tile(labelRes: Int, numberFormat: NumberFormat): Pair<String, String> =
    score.toString() to pluralStringResource(labelRes, count, numberFormat.format(count))

@Composable
private fun PlatformRelease.countdownTile(todayEpochDay: Long): Pair<String, String> {
    val daysLeft = ((epochDay ?: todayEpochDay) - todayEpochDay).toInt()
    if (!hasExactDate || releaseDate == null) {
        return dateLabel() to stringResource(R.string.game_detail_expected_release)
    }
    // The tile is narrow: month and day only, the full date is in the platform list.
    val shortDate = remember(releaseDate) {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMd")
        SimpleDateFormat(pattern, Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(releaseDate)
    }
    return if (daysLeft == 0) {
        stringResource(R.string.game_detail_today) to
            stringResource(R.string.game_detail_release_day, shortDate)
    } else {
        pluralStringResource(R.plurals.game_detail_days_until, daysLeft, daysLeft) to
            stringResource(R.string.game_detail_until_release, shortDate)
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Summary(summary: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    // Only offer the toggle when the collapsed text actually got cut off.
    var overflows by remember { mutableStateOf(false) }
    Column {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else SUMMARY_COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
        )
        if (overflows) {
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    stringResource(
                        if (expanded) R.string.game_detail_show_less else R.string.game_detail_read_more
                    )
                )
            }
        }
    }
}

@Composable
private fun PlatformsSection(releases: List<PlatformRelease>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { expanded = !expanded }
                .heightIn(min = 48.dp)
                .testTag("game_detail_platforms_toggle")
        ) {
            SectionTitle(stringResource(R.string.game_detail_available_on), Modifier.weight(1f))
            Text(
                text = pluralStringResource(R.plurals.game_detail_platform_count, releases.size, releases.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (expanded) {
            releases.forEachIndexed { index, release ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp)
                ) {
                    Text(
                        text = release.platformName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = release.dateLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = if (release.releaseDate == null) FontStyle.Italic else FontStyle.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = releases.joinToString { it.platformName },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRows(details: GameDetails) {
    val rows = listOf(
        R.string.game_detail_developer to details.developers.joinToString(),
        R.string.game_detail_publisher to details.publishers.joinToString(),
        R.string.game_detail_franchise to details.franchise.orEmpty(),
        R.string.game_detail_modes to details.modes.joinToString(),
        R.string.game_detail_age_rating to details.ageRatings.joinToString(" · ")
    ).filter { it.second.isNotEmpty() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { (labelRes, value) ->
            Row {
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(112.dp)
                )
                Text(text = value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// endregion

// region Media

@Composable
private fun MediaTab(details: GameDetails, loadImages: Boolean) {
    val uriHandler = LocalUriHandler.current
    if (details.screenshots.isEmpty() && details.videos.isEmpty()) {
        EmptyNote(stringResource(R.string.game_detail_no_media))
        return
    }
    if (details.screenshots.isNotEmpty()) {
        Section(title = stringResource(R.string.game_detail_screenshots)) {
            if (!loadImages) {
                EmptyNote(stringResource(R.string.game_detail_images_disabled, details.name))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    details.screenshots.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { columnIndex, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = stringResource(
                                        R.string.game_detail_screenshot_cd,
                                        rowIndex * 2 + columnIndex + 1,
                                        details.name
                                    ),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(16f / 9f)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                            // Keeps a lone last screenshot at half width.
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
    if (details.videos.isNotEmpty()) {
        Section(title = stringResource(R.string.game_detail_videos)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                details.videos.forEach { video ->
                    LinkRow(
                        label = video.name,
                        leadingIcon = Icons.Filled.PlayArrow,
                        onClick = { runCatching { uriHandler.openUri(video.url) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkRow(label: String, leadingIcon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp)
        ) {
            Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// endregion

// region Related

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedTab(details: GameDetails, loadImages: Boolean, onOpenGame: (Long) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val hasLinks = details.links.isNotEmpty() || details.gameUrl != null
    if (details.similarGames.isEmpty() && details.expansions.isEmpty() && details.dlcs.isEmpty() && !hasLinks) {
        EmptyNote(stringResource(R.string.game_detail_no_related))
        return
    }
    if (details.similarGames.isNotEmpty()) {
        Section(title = stringResource(R.string.game_detail_similar_games)) {
            if (loadImages) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(details.similarGames, key = { it.id }) { game ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .width(104.dp)
                                .clickable { onOpenGame(game.id) }
                        ) {
                            AsyncImage(
                                model = game.coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3f / 4f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Text(
                                text = game.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // Images off: the same games as plain tappable rows.
                Column {
                    details.similarGames.forEach { game ->
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenGame(game.id) }
                                .padding(vertical = 14.dp)
                        )
                    }
                }
            }
        }
    }
    if (details.expansions.isNotEmpty() || details.dlcs.isNotEmpty()) {
        ExpansionsSection(details.expansions, details.dlcs)
    }
    if (hasLinks) {
        Section(title = stringResource(R.string.game_detail_links)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                details.links.forEach { link ->
                    OutlinedButton(onClick = { runCatching { uriHandler.openUri(link.url) } }) {
                        Text(link.type.label())
                    }
                }
                details.gameUrl?.let { url ->
                    OutlinedButton(onClick = { runCatching { uriHandler.openUri(url) } }) {
                        Text("IGDB")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpansionsSection(expansions: List<String>, dlcs: List<String>) {
    var showDlcs by rememberSaveable { mutableStateOf(false) }
    Section(title = stringResource(R.string.game_detail_expansions)) {
        Column {
            expansions.forEach { name ->
                AddOnRow(name = name, badge = stringResource(R.string.game_detail_expansion_badge))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            // Small DLC packs can run long; with no expansions above them they just show.
            if (showDlcs || expansions.isEmpty()) {
                dlcs.forEach { name ->
                    AddOnRow(name = name, badge = null)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            if (dlcs.isNotEmpty() && expansions.isNotEmpty()) {
                TextButton(
                    onClick = { showDlcs = !showDlcs },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (showDlcs) {
                            stringResource(R.string.game_detail_hide_dlc)
                        } else {
                            pluralStringResource(R.plurals.game_detail_show_dlc, dlcs.size, dlcs.size)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddOnRow(name: String, badge: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GameLinkType.label(): String = when (this) {
    GameLinkType.OFFICIAL -> stringResource(R.string.game_detail_link_official)
    GameLinkType.STEAM -> "Steam"
    GameLinkType.GOG -> "GOG"
    GameLinkType.EPIC -> "Epic"
    GameLinkType.PLAYSTATION -> "PlayStation"
    GameLinkType.XBOX -> "Xbox"
    GameLinkType.NINTENDO -> "Nintendo"
}

// endregion

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title)
        content()
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Day-precision dates in the user's locale, IGDB's own label for approximate ones. */
@Composable
private fun PlatformRelease.dateLabel(): String {
    val date = releaseDate ?: return stringResource(R.string.game_detail_unknown_date)
    if (!hasExactDate) return human ?: stringResource(R.string.game_detail_unknown_date)
    return remember(date) { formatUtcDay(date) }
}

// IGDB dates are midnight-UTC timestamps; formatting in UTC keeps the calendar day intact.
private fun formatUtcDay(date: Date): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(date)

private val TAB_TITLES = listOf(
    R.string.game_detail_tab_about,
    R.string.game_detail_tab_media,
    R.string.game_detail_tab_related
)

private const val SUMMARY_COLLAPSED_LINES = 4

/** How long the unfollow/Undo snackbar stays up before dismissing itself. */
private const val UNDO_SNACKBAR_TIMEOUT_MS = 3_000L
