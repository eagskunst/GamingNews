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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ArticleOpenMode
import com.eagskunst.emmanuel.gamingnews.ui.main.MainActivityViewModel
import com.eagskunst.emmanuel.gamingnews.ui.news.NewsScreen
import com.eagskunst.emmanuel.gamingnews.ui.news.NewsViewModel
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesScreen
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesViewModel
import com.eagskunst.emmanuel.gamingnews.ui.saved.SavedScreen
import com.eagskunst.emmanuel.gamingnews.ui.saved.SavedViewModel
import com.eagskunst.emmanuel.gamingnews.ui.theme.GamingNewsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme by viewModel.darkThemeEnabled.collectAsStateWithLifecycle(initialValue = isSystemInDarkTheme())
            GamingNewsTheme(darkTheme = darkTheme) {
                MainScreen(
                    activity = this,
                    onOpenArticle = viewModel::openArticle,
                    onOpenArticleWithMode = viewModel::openArticleWithMode,
                    onShareArticle = { url -> shareArticle(url) },
                    onOpenGameUrl = { url -> viewModel.openArticleWithMode(url, ArticleOpenMode.CUSTOM_TAB) },
                    onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) }
                )
            }
        }
    }

    private fun shareArticle(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, null))
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun MainScreen(
    activity: ComponentActivity,
    onOpenArticle: (String) -> Unit,
    onOpenArticleWithMode: (String, ArticleOpenMode) -> Unit,
    onShareArticle: (String) -> Unit,
    onOpenGameUrl: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass(activity)
    val navController = rememberNavController()
    val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        bottomBar = {
            if (!useNavigationRail) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding)) {
            if (useNavigationRail) {
                NavigationSideBar(
                    navController = navController,
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
                        onOpenArticle = onOpenArticle,
                        onOpenArticleWithMode = onOpenArticleWithMode,
                        onShareArticle = onShareArticle
                    )
                }
                composable(BottomNavRoute.Saved.route) {
                    val viewModel = hiltViewModel<SavedViewModel>()
                    SavedScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onOpenArticle = onOpenArticle,
                        onOpenArticleWithMode = onOpenArticleWithMode,
                        onShareArticle = onShareArticle
                    )
                }
                composable(BottomNavRoute.Releases.route) {
                    val viewModel = hiltViewModel<ReleasesViewModel>()
                    ReleasesScreen(
                        viewModel = viewModel,
                        onSettingsClick = onSettingsClick,
                        onOpenGameUrl = onOpenGameUrl
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
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
                onClick = { navController.navigateToTopLevel(item.route) }
            )
        }
    }
}

@Composable
private fun NavigationSideBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationRail(modifier = modifier) {
        BottomNavRoute.entries.forEach { item ->
            NavigationRailItem(
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = currentRoute == item.route,
                onClick = { navController.navigateToTopLevel(item.route) }
            )
        }
    }
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
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    ),
    Releases(
        route = "releases",
        labelRes = R.string.nextReleases,
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
}
