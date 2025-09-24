package com.fadymarty.webviewapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.fadymarty.webviewapp.ui.theme.WebViewAppTheme
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewState
import java.io.File


class MainActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkMultiplePermission()
        enableEdgeToEdge()
        setContent {
            WebViewAppTheme {

                // Замените ссылку "https://example.com" на нужную вам
                val url =
                    "https://www.cleanpng.com/png-skull-with-snake-and-cracks-8538323/download-png.html"

                val state = rememberWebViewState(url)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        state = state,
                        onCreated = {
                            it.settings.javaScriptEnabled = true
                            it.settings.allowFileAccess = true
                            it.settings.domStorageEnabled = true
                            it.settings.javaScriptCanOpenWindowsAutomatically = true
                            it.settings.supportMultipleWindows()
                            it.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                                if (checkMultiplePermission()) {
                                    download(
                                        url.trim(),
                                        userAgent,
                                        contentDisposition,
                                        mimeType
                                    )
                                }

                            }
                        }
                    )
                }
            }
        }
    }

    private fun download(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
    ) {
        val folder = File(
            Environment.getExternalStorageDirectory().toString() + "/Download"
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val request = DownloadManager.Request(url.toUri())
        request.setMimeType(mimeType)
        val cookie = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader("cookie", cookie)
        request.addRequestHeader("User-Agent", userAgent)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        request.setTitle(fileName)
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

    }

    private fun checkMultiplePermission(): Boolean {
        val multiplePermissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayListOf(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayListOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
        val neededPermissions = arrayListOf<String>()
        for (permission in multiplePermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(permission)
            }
        }
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                neededPermissions.toTypedArray(),
                0
            )
            return false
        }
        return true
    }
}