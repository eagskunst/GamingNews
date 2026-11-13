package com.eagskunst.emmanuel.gamingnews.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.core.domain.model.NewsArticle
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.ui.gamedetail.GameDetailScreen
import com.eagskunst.emmanuel.gamingnews.ui.gamedetail.GameDetailViewModel
import com.eagskunst.emmanuel.gamingnews.ui.main.MainActivityViewModel

import com.eagskunst.emmanuel.gamingnews.ui.news.NewsScreen
import com.eagskunst.emmanuel.gamingnews.ui.news.NewsViewModel
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesScreen
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesViewModel
import com.eagskunst.emmanuel.gamingnews.ui.reviews.ReviewsScreen
import com.eagskunst.emmanuel.gamingnews.ui.reviews.ReviewsViewModel
import com.eagskunst.emmanuel.gamingnews.ui.saved.SavedScreen
import com.eagskunst.emmanuel.gamingnews.ui.saved.SavedViewModel
import com.eagskunst.emmanuel.gamingnews.ui.theme.GamingNewsTheme
import com.eagskunst.emmanuel.gamingnews.utility.shareArticle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { viewModel.maybeRequestReview(this@MainActivity) }
    }

    /** Game a release notification asked to open; cleared once the detail page is shown. */
    private val pendingGameId = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A recreated activity still carries the launch intent; only a fresh start may open it.
        if (savedInstanceState == null) pendingGameId.value = intent.gameIdExtra()
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
            val articleOpenMode by viewModel.articleOpenMode.collectAsStateWithLifecycle(initialValue = ArticleOpenMode.CUSTOM_TAB)
            GamingNewsTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                MainScreen(
                    activity = this,
                    onOpenArticle = { url -> viewModel.openArticle(url, articleOpenMode) },
                    onOpenArticleWithMode = viewModel::openArticleWithMode,
                    onShareArticle = { article -> shareArticle(article.title, article.link) },
                    onShareGame = { name, url -> shareArticle(name, url) },
                    onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onManageMutedWords = {
                        startActivity(
                            Intent(this, SettingsActivity::class.java)
                                .putExtra(SettingsActivity.EXTRA_OPEN_MUTED_WORDS, true)
                        )
                    },
                    pendingGameId = pendingGameId,
                    onPendingGameOpened = { pendingGameId.value = null }
                )
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.gameIdExtra()?.let { pendingGameId.value = it }
    }

    private fun Intent.gameIdExtra(): Long? =
        getLongExtra(EXTRA_OPEN_GAME_ID, NO_GAME_ID).takeIf { it != NO_GAME_ID }

    companion object {
        /** IGDB game ID a release notification wants opened on its detail page. */
        const val EXTRA_OPEN_GAME_ID = "com.eagskunst.emmanuel.gamingnews.extra.OPEN_GAME_ID"
        private const val NO_GAME_ID = -1L
    }

}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun MainScreen(
    activity: ComponentActivity,
    onOpenArticle: (String) -> Unit,
    onOpenArticleWithMode: (String, ArticleOpenMode) -> Unit,
    onShareArticle: (NewsArticle) -> Unit,
    onShareGame: (name: String, url: String) -> Unit,
    onSettingsClick: () -> Unit,
    onManageMutedWords: () -> Unit,
    pendingGameId: StateFlow<Long?>,
    onPendingGameOpened: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass(activity)
    val navController = rememberNavController()
    val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val gameToOpen by pendingGameId.collectAsStateWithLifecycle()
    LaunchedEffect(gameToOpen) {
        gameToOpen?.let { gameId ->
            // Land on Releases first so Back from the detail page returns somewhere related.
            navController.navigateToTopLevel(BottomNavRoute.Releases.route)
            navController.navigateToGame(gameId)
            onPendingGameOpened()
        }
    }
    // The detail page is full screen: no bottom bar or rail while it is on top.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showNavigation = backStackEntry?.destination?.route != GAME_ROUTE

    var newsScrollToTopEvent by remember { mutableIntStateOf(0) }
    var savedScrollToTopEvent by remember { mutableIntStateOf(0) }
    var releasesScrollToTopEvent by remember { mutableIntStateOf(0) }
    var reviewsScrollToTopEvent by remember { mutableIntStateOf(0) }

    fun onTabReselected(route: String) {
        when (route) {
            BottomNavRoute.News.route -> newsScrollToTopEvent++
            BottomNavRoute.Saved.route -> savedScrollToTopEvent++
            BottomNavRoute.Releases.route -> releasesScrollToTopEvent++
            BottomNavRoute.Reviews.route -> reviewsScrollToTopEvent++
        }
    }

    Scaffold(
        // No topBar here: each screen renders its own TopAppBar which already
        // consumes the status bar inset. Reserving it again here would push
        // the toolbar down twice, leaving a big gap under the status bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!useNavigationRail && showNavigation) {
                BottomNavigationBar(navController, onTabReselected = ::onTabReselected)
            }
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding)) {
            if (useNavigationRail && showNavigation) {
                NavigationSideBar(
                    navController = navController,
                    onTabReselected = ::onTabReselected,
                    modifier = Modifier.fillMaxHeight()
                )
            }
            NavHost(
                navController = navController,
                startDestination = BottomNavRoute.News.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(BottomNavRoute.News.route) {
                    val viewModel = hiltViewModel<NewsViewModel>()
                    NewsScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onManageMutedWords = onManageMutedWords,
                        onOpenArticle = onOpenArticle,
                        onOpenArticleWithMode = onOpenArticleWithMode,
                        onShareArticle = onShareArticle,
                        scrollToTopSignal = newsScrollToTopEvent
                    )
                }
                composable(BottomNavRoute.Saved.route) {
                    val viewModel = hiltViewModel<SavedViewModel>()
                    SavedScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onOpenArticle = onOpenArticle,
                        onOpenArticleWithMode = onOpenArticleWithMode,
                        onShareArticle = onShareArticle,
                        scrollToTopSignal = savedScrollToTopEvent
                    )
                }
                composable(BottomNavRoute.Releases.route) {
                    val viewModel = hiltViewModel<ReleasesViewModel>()
                    ReleasesScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onOpenGame = navController::navigateToGame,
                        scrollToTopSignal = releasesScrollToTopEvent
                    )
                }
                composable(BottomNavRoute.Reviews.route) {
                    val viewModel = hiltViewModel<ReviewsViewModel>()
                    ReviewsScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onManageMutedWords = onManageMutedWords,
                        onOpenArticle = onOpenArticle,
                        onOpenArticleWithMode = onOpenArticleWithMode,
                        onShareArticle = onShareArticle,
                        scrollToTopSignal = reviewsScrollToTopEvent
                    )
                }
                composable(
                    route = GAME_ROUTE,
                    arguments = listOf(
                        navArgument(GameDetailViewModel.GAME_ID_ARG) { type = NavType.LongType }
                    )
                ) {
                    val viewModel = hiltViewModel<GameDetailViewModel>()
                    GameDetailScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onShare = onShareGame,
                        onOpenGame = navController::navigateToGame
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    onTabReselected: (String) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        BottomNavRoute.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = selected,
                onClick = {
                    if (selected) {
                        onTabReselected(item.route)
                    } else {
                        navController.navigateToTopLevel(item.route)
                    }
                }
            )
        }
    }
}

@Composable
private fun NavigationSideBar(
    navController: NavHostController,
    onTabReselected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationRail(modifier = modifier) {
        BottomNavRoute.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationRailItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = selected,
                onClick = {
                    if (selected) {
                        onTabReselected(item.route)
                    } else {
                        navController.navigateToTopLevel(item.route)
                    }
                }
            )
        }
    }
}

private const val GAME_ROUTE = "game/{${GameDetailViewModel.GAME_ID_ARG}}"

private fun NavHostController.navigateToGame(gameId: Long) {
    // launchSingleTop would swallow a similar-game tap (same route, new ID), so only skip
    // when this exact game is already on top, e.g. a double tap.
    val current = currentBackStackEntry
    val isAlreadyOpen = current?.destination?.route == GAME_ROUTE &&
        current.arguments?.getLong(GameDetailViewModel.GAME_ID_ARG) == gameId
    if (!isAlreadyOpen) navigate("game/$gameId")
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private enum class BottomNavRoute(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    News(
        route = "news",
        labelRes = R.string.app_name,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    Saved(
        route = "saved",
        labelRes = R.string.saved,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    ),
    Releases(
        route = "releases",
        labelRes = R.string.nextReleases,
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    ),
    Reviews(
        route = "reviews",
        labelRes = R.string.reviews,
        selectedIcon = Icons.Filled.Star,
        unselectedIcon = Icons.Outlined.StarBorder
    )
}
