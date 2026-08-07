package com.eagskunst.emmanuel.gamingnews.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.ui.news.NewsScreen
import com.eagskunst.emmanuel.gamingnews.ui.news.NewsViewModel
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesScreen
import com.eagskunst.emmanuel.gamingnews.ui.releases.ReleasesViewModel
import com.eagskunst.emmanuel.gamingnews.ui.theme.GamingNewsTheme
import com.eagskunst.emmanuel.gamingnews.ui.topics.TopicsScreen
import com.eagskunst.emmanuel.gamingnews.ui.topics.TopicsViewModel
import com.eagskunst.emmanuel.gamingnews.utility.openCustomTab
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamingNewsTheme {
                MainScreen(
                    onOpenArticle = { url -> openCustomTab(url.toUri()) },
                    onOpenGameUrl = { url -> openCustomTab(url.toUri()) },
                    onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
private fun MainScreen(
    onOpenArticle: (String) -> Unit,
    onOpenGameUrl: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.News.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavRoute.News.route) {
                val viewModel = hiltViewModel<NewsViewModel>()
                NewsScreen(
                    viewModel = viewModel,
                    onSettingsClick = onSettingsClick,
                    onOpenArticle = onOpenArticle
                )
            }
            composable(BottomNavRoute.Releases.route) {
                val viewModel = hiltViewModel<ReleasesViewModel>()
                ReleasesScreen(
                    viewModel = viewModel,
                    onOpenGameUrl = onOpenGameUrl
                )
            }
            composable(BottomNavRoute.Topics.route) {
                val viewModel = hiltViewModel<TopicsViewModel>()
                TopicsScreen(viewModel = viewModel)
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
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
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
    Releases(
        route = "releases",
        labelRes = R.string.nextReleases,
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    ),
    Topics(
        route = "topics",
        labelRes = R.string.notification,
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )
}
