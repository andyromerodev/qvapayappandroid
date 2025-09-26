package com.example.qvapayappandroid.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qvapayappandroid.R
import com.example.qvapayappandroid.navigation.AppDestinations

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.P2P,
        BottomNavItem.Templates,
        BottomNavItem.WebView,
        BottomNavItem.Settings
    )
    
    NavigationBar(
        containerColor = colorResource(id = R.color.qvapay_surface_medium),
        contentColor = colorResource(id = R.color.qvapay_purple_primary),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(id = R.color.qvapay_purple_primary),
                    selectedTextColor = colorResource(id = R.color.qvapay_purple_primary),
                    indicatorColor = colorResource(id = R.color.qvapay_background_primary),
                    unselectedIconColor = colorResource(id = R.color.qvapay_purple_text),
                    unselectedTextColor = colorResource(id = R.color.qvapay_purple_text)
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = AppDestinations.Home.route,
        title = "Inicio",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object P2P : BottomNavItem(
        route = AppDestinations.P2P.route,
        title = "P2P",
        selectedIcon = Icons.Filled.AccountBox,
        unselectedIcon = Icons.Outlined.AccountBox
    )
    
    object Templates : BottomNavItem(
        route = AppDestinations.Templates.route,
        title = "Plantillas",
        selectedIcon = Icons.Filled.BookmarkAdd,
        unselectedIcon = Icons.Outlined.BookmarkAdd
    )
    
    object WebView : BottomNavItem(
        route = AppDestinations.WebView.route,
        title = "Web",
        selectedIcon = Icons.Filled.Language,
        unselectedIcon = Icons.Outlined.Language
    )
    
    object Settings : BottomNavItem(
        route = AppDestinations.Settings.route,
        title = "Ajustes",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}