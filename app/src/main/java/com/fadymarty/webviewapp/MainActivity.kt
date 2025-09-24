package com.fadymarty.webviewapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.fadymarty.webviewapp.ui.theme.WebViewAppTheme
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebViewAppTheme {

                // Замените ссылку "https://example.com" на нужную вам
                val url = "https://example.com"

                val state = rememberWebViewState(url)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        state = state
                    )
                }
            }
        }
    }
}