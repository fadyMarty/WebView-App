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
import android.view.ViewGroup
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fadymarty.webviewapp.ui.theme.WebViewAppTheme
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

                // Замените ссылку "https://example.com" на нужную вам
                val url = "https://www.file.io/"

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        factory = {
                            WebView(it).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        }, update = {
                            it.loadUrl(url)
                            it.setInitialScale(0);
                            it.getSettings().javaScriptEnabled = true
                            it.getSettings().domStorageEnabled = true
                            it.getSettings().allowContentAccess = true
                            it.getSettings().allowFileAccess = true
                            it.getSettings().allowUniversalAccessFromFileURLs = true
                            it.getSettings().allowFileAccessFromFileURLs = true
                            it.getSettings().javaScriptCanOpenWindowsAutomatically = true
                            it.getSettings().loadWithOverviewMode = true
                            it.getSettings().useWideViewPort = true
                            it.getSettings().setSupportMultipleWindows(true)
                            it.getSettings().databaseEnabled = true
                            it.getSettings().javaScriptCanOpenWindowsAutomatically = true;
                            it.getSettings().setGeolocationEnabled(true)
                            it.getSettings().javaScriptEnabled = true
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
                            it.webChromeClient = object : WebChromeClient() {
                                override fun onShowFileChooser(
                                    webView: WebView,
                                    filePathCallback: ValueCallback<Array<Uri>>,
                                    fileChooserParams: FileChooserParams
                                ): Boolean {
                                    uploadMessage = filePathCallback
                                    try {
                                        startActivityForResult(
                                            fileChooserParams.createIntent(),
                                            100
                                        )
                                        return true
                                    } catch (e: ActivityNotFoundException) {
                                        e.printStackTrace()
                                        uploadMessage = null
                                        return false
                                    }
                                }
                            }
                        }
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
        if (requestCode == 100) {
            uploadMessage?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    intent
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
        val multiplePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
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