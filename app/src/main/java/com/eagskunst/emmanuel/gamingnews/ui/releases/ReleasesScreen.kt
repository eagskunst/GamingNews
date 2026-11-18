package com.eagskunst.emmanuel.gamingnews.ui.releases

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FollowedGame
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.PlatformSelectionNotice
import com.eagskunst.emmanuel.gamingnews.core.domain.model.isReleased
import com.eagskunst.emmanuel.gamingnews.ui.components.MainTopAppBar
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ReleasesScreen(
    viewModel: ReleasesViewModel,
    onSettingsClick: () -> Unit,
    onOpenGame: (Long) -> Unit,
    scrollToTopSignal: Int = 0
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Release dates are grouped on the UTC calendar so IGDB midnight timestamps don't shift
    // a day back under local time zones.
    val monthFormatter = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val groupedReleases = remember(uiState.releases) {
        uiState.releases.groupBy { monthFormatter.format(it.releaseDate).uppercase(Locale.getDefault()) }
    }
    // Saveable only: survives rotation, resets to Releases on a cold start.
    var selectedTab by rememberSaveable { mutableIntStateOf(RELEASES_TAB) }
    val showFollowed = selectedTab == FOLLOWED_TAB
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - LOAD_MORE_THRESHOLD
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val permissionDeniedMessage = stringResource(R.string.notification_permission_rationale)
    val undoLabel = stringResource(R.string.action_undo)

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ReleasesUiEvent.RequestNotificationPermission -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                is ReleasesUiEvent.ShowPermissionDenied -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(permissionDeniedMessage)
                    }
                }
                is ReleasesUiEvent.ShowReminderRemoved -> {
                    // A label-bearing snackbar defaults to Indefinite (no timeout, no swipe
                    // dismissal), so cap it with an explicit timeout and a timed duration.
                    val result = withTimeoutOrNull(UNDO_SNACKBAR_TIMEOUT_MS) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.reminder_removed, event.game.name),
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Long
                        )
                    }
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoUnfollow(event.game)
                    }
                }
            }
        }
    }

    LaunchedEffect(listState, viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                snapshotFlow { shouldLoadMore.value }
                    .distinctUntilChanged()
                    .collect(viewModel::onListEndVisibilityChanged)
            } finally {
                viewModel.onListEndVisibilityChanged(false)
            }
        }
    }
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }
    LaunchedEffect(uiState.selectedPlatformIds) {
        listState.scrollToItem(0)
    }

    Scaffold(
        // The outer Scaffold in MainActivity already reserves space for the bottom
        // navigation bar; only consume the top status bar inset here to avoid double padding.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column {
                MainTopAppBar(
                    title = stringResource(R.string.games_title),
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSettingsClick = onSettingsClick,
                    showSearch = !showFollowed
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    Tab(
                        selected = !showFollowed,
                        onClick = { selectedTab = RELEASES_TAB },
                        text = { Text(stringResource(R.string.nextReleases)) }
                    )
                    Tab(
                        selected = showFollowed,
                        onClick = { selectedTab = FOLLOWED_TAB },
                        text = { Text(stringResource(R.string.games_tab_followed)) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Scaffold lifts a visible snackbar above the FAB, leaving it floating over the
            // cards; hiding the FAB instead lets the snackbar anchor above the bottom nav.
            if (!showFollowed && snackbarHostState.currentSnackbarData == null) {
                FloatingActionButton(onClick = viewModel::refresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.releases_refresh_content_description)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (showFollowed) {
            FollowedGamesList(
                games = uiState.followedGames,
                onUnfollow = viewModel::onUnfollowClick,
                onOpenGame = onOpenGame,
                onBrowseReleases = { selectedTab = RELEASES_TAB },
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PlatformFilterRow(
                uiState = uiState,
                onSelectAll = viewModel::onSelectAllPlatforms,
                onToggle = viewModel::onPlatformToggle
            )

            uiState.catalogNotice?.let { notice ->
                CatalogNoticeRow(
                    notice = notice,
                    onDismiss = viewModel::dismissCatalogNotice
                )
            }

            if (uiState.unknownPlatformIds.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.releases_unknown_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            uiState.error?.let { error ->
                ErrorRow(
                    error = error,
                    onRetry = viewModel::retry
                )
            }

            if (uiState.releases.isNotEmpty()) {
                Text(
                    text = matchingReleasesLabel(uiState),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.releases.isNotEmpty() -> ReleaseList(
                        groupedReleases = groupedReleases,
                        isLoadingMore = uiState.isLoadingMore,
                        listState = listState,
                        followedGameIds = uiState.followedGameIds,
                        onBellClick = viewModel::onBellClick,
                        onOpenGame = onOpenGame
                    )
                    uiState.isBusy || (uiState.hasMorePages && uiState.error == null) -> {
                        SearchingState()
                    }
                    else -> EmptyState(
                        hasActiveFilters = uiState.hasActiveFilters,
                        onClearFilters = viewModel::clearFilters
                    )
                }
            }
        }
    }

    uiState.offsetPickerFor?.let { release ->
        ReminderOffsetSheet(
            release = release,
            onConfirm = viewModel::onOffsetsConfirmed,
            onDismiss = viewModel::onPickerDismissed
        )
    }
}

@Composable
private fun matchingReleasesLabel(uiState: ReleasesUiState): String {
    val resource = if (uiState.hasMorePages) {
        R.plurals.releases_matching_loaded
    } else {
        R.plurals.releases_matching_total
    }
    return pluralStringResource(resource, uiState.matchCount, uiState.matchCount)
}

@Composable
private fun PlatformFilterRow(
    uiState: ReleasesUiState,
    onSelectAll: () -> Unit,
    onToggle: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("platform_filter_row")
    ) {
        item(key = "all") {
            FilterChip(
                selected = uiState.selectedPlatformIds.isEmpty(),
                onClick = onSelectAll,
                label = { Text(stringResource(R.string.releases_filter_all)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
        items(uiState.platformOptions, key = { it.igdbId }) { platform ->
            FilterChip(
                selected = platform.igdbId in uiState.selectedPlatformIds,
                onClick = { onToggle(platform.igdbId) },
                label = { Text(platform.displayName) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
        items(uiState.unknownPlatformIds.sorted(), key = { "unknown_$it" }) { id ->
            FilterChip(
                selected = true,
                onClick = { onToggle(id) },
                label = { Text(stringResource(R.string.releases_unknown_platform_chip, id)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.releases_remove_unknown_cd),
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
}

@Composable
private fun CatalogNoticeRow(
    notice: PlatformSelectionNotice,
    onDismiss: () -> Unit
) {
    val message = when (notice) {
        PlatformSelectionNotice.SOME_RETIRED_REMOVED -> R.string.releases_retired_notice
        PlatformSelectionNotice.ALL_RETIRED_FALLBACK -> R.string.releases_all_retired_notice
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.releases_dismiss))
        }
    }
}

@Composable
private fun ErrorRow(
    error: ReleasesError,
    onRetry: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(
            text = stringResource(error.messageResource),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        if (error != ReleasesError.SELECTION_SAVE) {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun SearchingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.releases_searching),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun EmptyState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.releases_no_matches),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (hasActiveFilters) {
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.releases_clear_filters))
            }
        }
    }
}

@Composable
private fun ReleaseList(
    groupedReleases: Map<String, List<GameRelease>>,
    isLoadingMore: Boolean,
    listState: LazyListState,
    followedGameIds: Set<Long>,
    onBellClick: (GameRelease) -> Unit,
    onOpenGame: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("releases_list"),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedReleases.forEach { (month, releases) ->
            item(key = "header_$month") {
                Text(
                    text = month,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(releases, key = { "${it.id}_${it.releaseDate.time}" }) { release ->
                ReleaseCard(
                    release = release,
                    isFollowed = release.id in followedGameIds,
                    onBellClick = { onBellClick(release) },
                    onClick = { onOpenGame(release.id) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        if (isLoadingMore) {
            item(key = "loading_more") {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FollowedGamesList(
    games: List<FollowedGame>,
    onUnfollow: (Long) -> Unit,
    onOpenGame: (Long) -> Unit,
    onBrowseReleases: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (games.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.followed_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onBrowseReleases) {
                Text(stringResource(R.string.followed_browse_releases))
            }
        }
        return
    }
    // Follows can be a year away or long released, so unlike release cards the year shows.
    val dateFormatter = remember {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val today = remember { LocalDate.now().toEpochDay() }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("followed_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(games, key = { it.gameId }) { game ->
            val isReleased = game.isReleased(today)
            val date = dateFormatter.format(Date(game.releaseEpochDay * MILLIS_PER_DAY))
            GameCard(
                name = game.name,
                coverUrl = game.coverUrl,
                // The label is stored joined by FollowGameUseCase; split it back into chips.
                platforms = game.platformsLabel.split(" · ").filter { it.isNotBlank() },
                dateLabel = if (isReleased) {
                    stringResource(R.string.followed_released_label, date)
                } else {
                    date
                },
                onClick = { onOpenGame(game.gameId) }
            ) {
                IconButton(
                    onClick = { onUnfollow(game.gameId) },
                    modifier = Modifier.testTag(
                        if (isReleased) "followed_remove_${game.gameId}"
                        else "followed_bell_${game.gameId}"
                    )
                ) {
                    Icon(
                        imageVector = if (isReleased) {
                            Icons.Default.Close
                        } else {
                            Icons.Filled.NotificationsActive
                        },
                        contentDescription = stringResource(
                            if (isReleased) R.string.followed_remove_cd
                            else R.string.releases_unfollow_cd,
                            game.name
                        ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReleaseCard(
    release: GameRelease,
    isFollowed: Boolean,
    onBellClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayFormatter = remember {
        SimpleDateFormat("MMM d", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    GameCard(
        name = release.name,
        coverUrl = release.coverUrl,
        platforms = release.platforms,
        // Approximate releases carry no meaningful day; show IGDB's own label instead.
        dateLabel = if (release.hasExactDate) {
            dayFormatter.format(release.releaseDate)
        } else {
            release.human.orEmpty()
        },
        onClick = onClick,
        modifier = modifier
    ) {
        // Follow bell only on day-precision cards; approximate cards get no trailing slot.
        if (release.hasExactDate) {
            IconButton(
                onClick = onBellClick,
                modifier = Modifier.testTag("release_bell_${release.id}")
            ) {
                Icon(
                    imageVector = if (isFollowed) {
                        Icons.Filled.NotificationsActive
                    } else {
                        Icons.Outlined.Notifications
                    },
                    contentDescription = stringResource(
                        if (isFollowed) R.string.releases_unfollow_cd
                        else R.string.releases_follow_cd,
                        release.name
                    ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Card body shared by the Releases and Followed lists; [trailing] is the bell/remove slot. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameCard(
    name: String,
    coverUrl: String?,
    platforms: List<String>,
    dateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.forEach { platform ->
                        PlatformChip(platform)
                    }
                }
            }
            trailing()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun PlatformChip(platform: String) {
    Text(
        text = platform,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

private val ReleasesError.messageResource: Int
    get() = when (this) {
        ReleasesError.TOKEN_ACQUISITION -> R.string.releases_token_error
        ReleasesError.IGDB_REJECTION -> R.string.releases_auth_rejected_error
        ReleasesError.REFRESH -> R.string.releases_refresh_error
        ReleasesError.PAGINATION -> R.string.releases_pagination_error
        ReleasesError.SELECTION_SAVE -> R.string.releases_selection_save_error
    }

private const val LOAD_MORE_THRESHOLD = 5
private const val RELEASES_TAB = 0
private const val FOLLOWED_TAB = 1
private const val MILLIS_PER_DAY = 86_400_000L

/** How long the unfollow/Undo snackbar stays up before dismissing itself. */
private const val UNDO_SNACKBAR_TIMEOUT_MS = 3_000L
