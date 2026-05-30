package com.easyssh.terminal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.InputType
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.easyssh.BuildConfig

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TerminalWebView(
    bridge: TerminalBridge,
    handle: TerminalWebViewHandle,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        onDispose {
            handle.clear()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> handle.resume()
                Lifecycle.Event.ON_PAUSE -> handle.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            TerminalInputWebView(context).apply {
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

private class TerminalInputWebView(context: Context) : WebView(context) {
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs)
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        outAttrs.imeOptions = outAttrs.imeOptions or
            EditorInfo.IME_FLAG_NO_EXTRACT_UI or
            EditorInfo.IME_ACTION_NONE
        outAttrs.privateImeOptions = "com.google.android.inputmethod.latin.noMicrophoneKey"
        return connection
    }
}

private const val TERMINAL_URL = "file:///android_asset/terminal/index.html"
