package com.codegarden.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codegarden.app.ui.screens.MainScaffold
import com.codegarden.app.ui.screens.SplashScreen
import com.codegarden.app.ui.screens.add.AddPlantScreen
import com.codegarden.app.ui.screens.detail.PlantDetailScreen
import com.codegarden.app.ui.screens.trash.TrashScreen
import com.codegarden.app.viewmodel.MainViewModel

/** 路由：启动页 → 主页面（4 Tab）→ 添加/详情。离线单机，无需登录 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val vm: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onFinished = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScaffold(
                vm = vm,
                onAddPlant = { navController.navigate("add") },
                onAddSpecies = { id -> navController.navigate("add?speciesId=$id") },
                onOpenPlant = { id -> navController.navigate("detail/$id") },
                onOpenTrash = { navController.navigate("trash") }
            )
        }
        composable("trash") {
            TrashScreen(vm = vm, onBack = { navController.popBackStack() })
        }
        composable(
            "add?speciesId={speciesId}",
            arguments = listOf(navArgument("speciesId") { type = NavType.IntType; defaultValue = -1 })
        ) { entry ->
            val speciesId = entry.arguments?.getInt("speciesId")?.takeIf { it > 0 }
            AddPlantScreen(
                vm = vm,
                speciesId = speciesId,
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.navigate("detail/$id") {
                        popUpTo("add") { inclusive = true }
                    }
                }
            )
        }
        composable("detail/{plantId}") { entry ->
            PlantDetailScreen(
                vm = vm,
                plantId = entry.arguments?.getString("plantId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
