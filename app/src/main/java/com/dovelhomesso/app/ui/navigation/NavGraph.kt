package com.dovelhomesso.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dovelhomesso.app.ui.screens.*
import com.dovelhomesso.app.ui.viewmodels.MainViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddItem : Screen("add_item?spotId={spotId}") {
        fun createRoute(spotId: Long? = null): String = 
            if (spotId != null) "add_item?spotId=$spotId" else "add_item"
    }
    data object AddDocument : Screen("add_document?spotId={spotId}") {
        fun createRoute(spotId: Long? = null): String = 
            if (spotId != null) "add_document?spotId=$spotId" else "add_document"
    }
    data object RoomsList : Screen("rooms")
    data object RoomDetail : Screen("room/{roomId}") {
        fun createRoute(roomId: Long): String = "room/$roomId"
    }
    data object ContainerDetail : Screen("container/{containerId}") {
        fun createRoute(containerId: Long): String = "container/$containerId"
    }
    data object SpotDetail : Screen("spot/{spotId}") {
        fun createRoute(spotId: Long): String = "spot/$spotId"
    }
    data object ItemDetail : Screen("item/{itemId}") {
        fun createRoute(itemId: Long): String = "item/$itemId"
    }
    data object DocumentDetail : Screen("document/{documentId}") {
        fun createRoute(documentId: Long): String = "document/$documentId"
    }
    data object Settings : Screen("settings")
}

// Animated transitions
private fun enterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

private fun exitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
}

private fun popEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

private fun popExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
}

// Vertical slide for modals (add screens)
private fun modalEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(350))
}

private fun modalExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
}

// Scale + fade for detail screens
private fun detailEnterTransition(): EnterTransition {
    return scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

private fun detailExitTransition(): ExitTransition {
    return scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() }
    ) {
        // Home - special fade in
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { exitTransition() },
            popEnterTransition = { 
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            }
        ) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddItem = { navController.navigate(Screen.AddItem.createRoute()) },
                onNavigateToAddDocument = { navController.navigate(Screen.AddDocument.createRoute()) },
                onNavigateToRooms = { navController.navigate(Screen.RoomsList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToItemDetail = { itemId -> 
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onNavigateToDocumentDetail = { documentId -> 
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                },
                onNavigateToSpotDetail = { spotId ->
                    navController.navigate(Screen.SpotDetail.createRoute(spotId))
                }
            )
        }
        
        // Add Item - vertical slide modal
        composable(
            route = Screen.AddItem.route,
            arguments = listOf(
                navArgument("spotId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            ),
            enterTransition = { modalEnterTransition() },
            exitTransition = { modalExitTransition() },
            popExitTransition = { modalExitTransition() }
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId")
            AddItemScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                initialSpotId = if (spotId != null && spotId > 0) spotId else null
            )
        }
        
        // Add Document - vertical slide modal
        composable(
            route = Screen.AddDocument.route,
            arguments = listOf(
                navArgument("spotId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            ),
            enterTransition = { modalEnterTransition() },
            exitTransition = { modalExitTransition() },
            popExitTransition = { modalExitTransition() }
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId")
            AddDocumentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                initialSpotId = if (spotId != null && spotId > 0) spotId else null
            )
        }
        
        // Rooms List
        composable(
            route = Screen.RoomsList.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            RoomsListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoom = { roomId ->
                    navController.navigate(Screen.RoomDetail.createRoute(roomId))
                }
            )
        }
        
        // Room Detail
        composable(
            route = Screen.RoomDetail.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.LongType }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: return@composable
            RoomDetailScreen(
                viewModel = viewModel,
                roomId = roomId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToContainer = { containerId ->
                    navController.navigate(Screen.ContainerDetail.createRoute(containerId))
                }
            )
        }
        
        // Container Detail
        composable(
            route = Screen.ContainerDetail.route,
            arguments = listOf(
                navArgument("containerId") { type = NavType.LongType }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getLong("containerId") ?: return@composable
            ContainerDetailScreen(
                viewModel = viewModel,
                containerId = containerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSpot = { spotId ->
                    navController.navigate(Screen.SpotDetail.createRoute(spotId))
                }
            )
        }
        
        // Spot Detail
        composable(
            route = Screen.SpotDetail.route,
            arguments = listOf(
                navArgument("spotId") { type = NavType.LongType }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: return@composable
            SpotDetailScreen(
                viewModel = viewModel,
                spotId = spotId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddItem = { spotIdArg ->
                    navController.navigate(Screen.AddItem.createRoute(spotIdArg))
                },
                onNavigateToAddDocument = { spotIdArg ->
                    navController.navigate(Screen.AddDocument.createRoute(spotIdArg))
                },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onNavigateToDocumentDetail = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                }
            )
        }
        
        // Item Detail - scale in
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.LongType }
            ),
            enterTransition = { detailEnterTransition() },
            exitTransition = { detailExitTransition() },
            popExitTransition = { detailExitTransition() }
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            ItemDetailScreen(
                viewModel = viewModel,
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Document Detail - scale in
        composable(
            route = Screen.DocumentDetail.route,
            arguments = listOf(
                navArgument("documentId") { type = NavType.LongType }
            ),
            enterTransition = { detailEnterTransition() },
            exitTransition = { detailExitTransition() },
            popExitTransition = { detailExitTransition() }
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: return@composable
            DocumentDetailScreen(
                viewModel = viewModel,
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Settings - slide from right
        composable(
            route = Screen.Settings.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
