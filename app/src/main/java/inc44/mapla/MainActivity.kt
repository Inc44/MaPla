package inc44.mapla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import inc44.mapla.navigation.AppNavGraph
import inc44.mapla.ui.theme.MaPlaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaPlaTheme {
                val navController = rememberNavController()
                AppNavGraph(navController, this)
            }
        }
    }
}
