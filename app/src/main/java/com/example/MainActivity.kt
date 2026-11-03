package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.security.SecureStorage
import com.example.ui.navigation.MainAppContainer
import com.example.ui.theme.DarkBg
import com.example.ui.theme.RepoManagerProTheme
import com.example.updates.UpdateChecker
import com.example.updates.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleOAuthIntent(intent)

        setContent {
            RepoManagerProTheme {
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

                LaunchedEffect(Unit) {
                    updateInfo = withContext(Dispatchers.IO) {
                        UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    color = DarkBg
                ) {
                    MainAppContainer()
                }

                updateInfo?.let { update ->
                    AlertDialog(
                        onDismissRequest = { updateInfo = null },
                        title = { Text("Update available") },
                        text = {
                            Text(
                                "Repo Manager Pro ${update.versionName} is available. " +
                                    "Would you like to download and install the update?"
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    updateInfo = null
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(update.apkUrl)
                                    )
                                    startActivity(intent)
                                }
                            ) {
                                Text("Update")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateInfo = null }) {
                                Text("Later")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "repomanager" && data.host == "oauth-callback") {
            val code = data.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                val secureStorage = SecureStorage.getInstance(this)
                secureStorage.setGitHubToken(code)
                secureStorage.setDemoMode(false)
            }
        }
    }
}
