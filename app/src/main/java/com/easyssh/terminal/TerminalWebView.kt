package com.easyssh.terminal

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.easyssh.BuildConfig

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TerminalWebView(
    bridge: TerminalBridge,
    handle: TerminalWebViewHandle,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        onDispose {
            handle.clear()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            WebView(context).apply {
                setBackgroundColor(Color.BLACK)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.allowContentAccess = false
                settings.allowFileAccess = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.databaseEnabled = false
                settings.mediaPlaybackRequiresUserGesture = true
                addJavascriptInterface(bridge, "EasySSH")
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean = !isAllowedLocalAsset(request.url)
                }
                handle.attach(this)
                loadUrl(TERMINAL_URL)
            }
        },
        update = { webView ->
            handle.attach(webView)
        },
        onRelease = { webView ->
            handle.detach(webView)
            webView.removeJavascriptInterface("EasySSH")
            webView.destroy()
        }
    )
}

private fun isAllowedLocalAsset(uri: Uri): Boolean {
    return uri.scheme == "file" && uri.path.orEmpty().contains("/android_asset/terminal/")
}

private const val TERMINAL_URL = "file:///android_asset/terminal/index.html"

