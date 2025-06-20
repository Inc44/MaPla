package inc44.mapla.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import inc44.mapla.drive.DriveServiceHelper
import inc44.mapla.ui.drive.DrivePickerScreen
import inc44.mapla.ui.login.SignInScreen
import inc44.mapla.ui.playlist.CreatePlaylistScreen

@Composable
fun AppNavGraph(navController: NavHostController, activity: Activity) {
    val account = GoogleSignIn.getLastSignedInAccount(activity)
    var driveHelper = remember(account) { account?.let { DriveServiceHelper(activity, it) } }

    val start = if (account == null) Routes.SignIn.route else Routes.Picker.route

    NavHost(navController, start) {
        composable(Routes.SignIn.route) {
            SignInScreen(activity) { acc ->
                driveHelper = DriveServiceHelper(activity, acc)
                navController.navigate(Routes.Picker.route) {
                    popUpTo(Routes.SignIn.route) { inclusive = true }
                }
            }
        }
        composable(Routes.Picker.route) {
            if (driveHelper == null) {
                navController.navigate(Routes.SignIn.route)
            } else {
                DrivePickerScreen(driveHelper) { picked ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("files", picked)
                    navController.navigate(Routes.Editor.route)
                }
            }
        }
        composable(Routes.Editor.route) {
            val files =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<List<inc44.mapla.drive.DriveFile>>("files")
            if (driveHelper != null && files != null) {
                CreatePlaylistScreen(driveHelper, files) {
                    navController.popBackStack(Routes.Picker.route, false)
                }
            } else {
                navController.popBackStack()
            }
        }
    }
}
