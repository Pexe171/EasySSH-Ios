package com.easyssh.terminal

import android.util.Base64
import android.webkit.WebView

class TerminalWebViewHandle {
    private var webView: WebView? = null

    fun attach(webView: WebView) {
        this.webView = webView
    }

    fun detach(webView: WebView) {
        if (this.webView === webView) {
            this.webView = null
        }
    }

    fun write(bytes: ByteArray) {
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        evaluate("window.easysshWrite('$encoded')")
    }

    fun clear() {
        evaluate("window.easysshClear()")
    }

    private fun evaluate(script: String) {
        webView?.post {
            webView?.evaluateJavascript(script, null)
        }
    }
}

