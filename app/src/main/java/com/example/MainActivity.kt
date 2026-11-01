package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.security.SecureStorage
import com.example.ui.navigation.MainAppContainer
import com.example.ui.theme.DarkBg
import com.example.ui.theme.RepoManagerProTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleOAuthIntent(intent)

        setContent {
            RepoManagerProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    MainAppContainer()
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
                // If OAuth code received from browser authorization
                val secureStorage = SecureStorage.getInstance(this)
                secureStorage.setGitHubToken(code)
                secureStorage.setDemoMode(false)
            }
        }
    }
}
