package inc44.mapla.ui.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

private const val TAG = "SignInScreen"

@Composable
fun SignInScreen(activity: Activity, onSignedIn: (GoogleSignInAccount) -> Unit) {
    var error by remember { mutableStateOf<String?>(null) }

    val options = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE), Scope(DriveScopes.DRIVE_FILE))
            .build()
    }

    val client = remember { GoogleSignIn.getClient(activity, options) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
            ->
            if (result.resultCode != Activity.RESULT_OK) {
                error = "Sign-in canceled"
                Log.w(TAG, "Canceled: ${result.resultCode}")
                return@rememberLauncherForActivityResult
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.i(TAG, "Signed in: ${account.email}")
                onSignedIn(account)
            } catch (e: ApiException) {
                error = "Sign-in failed: ${e.statusCode}"
                Log.e(TAG, "Error", e)
            }
        }

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Button(onClick = { launcher.launch(client.signInIntent) }) { Text("Sign in with Google") }
    }

    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
