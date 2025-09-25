package com.fadymarty.webviewapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fadymarty.webviewapp.ui.theme.WebViewAppTheme
import com.kevinnzou.web.AccompanistWebChromeClient
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewState
import java.io.File


class MainActivity : ComponentActivity() {

    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        checkMultiplePermission()
        enableEdgeToEdge()
        setContent {
            WebViewAppTheme {

                // Замените ссылку на нужную вам
                val url = "https://filebin.net/"
                val state = rememberWebViewState(url)
                val chromeClient = remember {
                    object : AccompanistWebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            if (uploadMessage != null) {
                                uploadMessage!!.onReceiveValue(null)
                                uploadMessage = null
                            }
                            uploadMessage = filePathCallback
                            try {
                                startActivityForResult(
                                    fileChooserParams!!.createIntent(),
                                    100
                                )
                                return true
                            } catch (e: ActivityNotFoundException) {
                                uploadMessage = null
                                e.printStackTrace()
                                return false
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        state = state,
                        onCreated = {
                            it.setInitialScale(0)
                            it.settings.javaScriptEnabled = true
                            it.settings.domStorageEnabled = true
                            it.settings.allowContentAccess = true
                            it.settings.allowFileAccess = true
                            it.settings.allowUniversalAccessFromFileURLs = true
                            it.settings.allowFileAccessFromFileURLs = true
                            it.settings.javaScriptCanOpenWindowsAutomatically = true
                            it.settings.loadWithOverviewMode = true
                            it.settings.useWideViewPort = true
                            it.settings.setSupportMultipleWindows(true)
                            it.settings.databaseEnabled = true
                            it.settings.javaScriptCanOpenWindowsAutomatically = true
                            it.settings.setGeolocationEnabled(true)
                            it.settings.javaScriptEnabled = true
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
                        },
                        chromeClient = chromeClient
                    )
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && uploadMessage != null) {
            uploadMessage!!.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            )
            uploadMessage = null
        }
    }

    private fun download(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
    ) {
        val folder = File(
            "${Environment.getExternalStorageDirectory()}/Download"
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
        val multiplePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(
                Manifest.permission.POST_NOTIFICATIONS
            )
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