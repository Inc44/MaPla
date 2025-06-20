package inc44.mapla.ui.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

private const val TAG = "SignInScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(activity: Activity, onSignedIn: (GoogleSignInAccount) -> Unit) {
    var error by remember { mutableStateOf<String?>(null) }
    var accepted by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

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
            runCatching {
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
            }
                .onSuccess { onSignedIn(it) }
                .onFailure {
                    error = "Sign-in failed"
                    Log.e(TAG, "Error", it)
                }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MaPla") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { menuExpanded = false /* TODO open settings */ }
                        )
                        DropdownMenuItem(
                            text = { Text("Help") },
                            onClick = { menuExpanded = false /* TODO open help */ }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!accepted) {
                Surface(shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "By using this app you agree to Google Drive access",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { accepted = true }) { Text("Agree") }
                            TextButton(onClick = { activity.finish() }) { Text("Cancel") }
                        }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Access Your Music",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp)
            )
            Spacer(Modifier.height(32.dp))
            Button(
                enabled = accepted,
                shape = RoundedCornerShape(50),
                onClick = { launcher.launch(client.signInIntent) }
            ) {
                Text("Connect to Google Drive")
            }
            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
