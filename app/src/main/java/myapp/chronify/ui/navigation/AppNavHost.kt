package myapp.chronify.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import myapp.chronify.ui.element.screen.EditNifeScreen
import myapp.chronify.ui.element.screen.EditNifeScreenRoute
import myapp.chronify.ui.element.screen.MarkerScreen
import myapp.chronify.ui.element.screen.MarkerScreenRoute
import myapp.chronify.ui.element.screen.SettingsScreen
import myapp.chronify.ui.element.screen.SettingsScreenRoute
import myapp.chronify.ui.element.screen.StatisticsScreen
import myapp.chronify.ui.element.screen.StatisticsScreenRoute

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    NavHost(
        navController = navController,
        startDestination = MarkerScreenRoute.route,
        modifier = modifier
    ) {

        composable(route = MarkerScreenRoute.route){
            // RemindScreen(navigateToAddScreen = { navController.navigate(AddScheduleScreenRoute.route) })
            MarkerScreen(
                navigateToEdit = { navController.navigate("${EditNifeScreenRoute.route}/$it") }
            )
        }

        composable(
            route = EditNifeScreenRoute.routeWithArgs,
            arguments = listOf(navArgument(EditNifeScreenRoute.itemIdArg) {
                type =
                    NavType.IntType
            })
        ) {
            EditNifeScreen(navigateBack = { navController.navigateUp() })
        }

        composable(route = StatisticsScreenRoute.route) {
            StatisticsScreen(
                navigateToEdit = { navController.navigate("${EditNifeScreenRoute.route}/$it") }
            )
        }

        composable(route = SettingsScreenRoute.route) {
            SettingsScreen()
        }
    }
}