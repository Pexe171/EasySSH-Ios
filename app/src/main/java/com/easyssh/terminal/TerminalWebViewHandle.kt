package com.easyssh.terminal

import android.util.Base64
import android.webkit.WebView

class TerminalWebViewHandle {
    private var webView: WebView? = null
    private var fontSize = DEFAULT_FONT_SIZE
    private var lineHeight = DEFAULT_LINE_HEIGHT

    fun attach(webView: WebView) {
        this.webView = webView
        applyDisplaySettings()
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

    fun configureDisplay(fontSize: Int, lineHeight: Float) {
        this.fontSize = fontSize.coerceIn(9, 18)
        this.lineHeight = lineHeight.coerceIn(1f, 1.5f)
        applyDisplaySettings()
    }

    private fun applyDisplaySettings() {
        evaluate(
            "if (window.easysshSetDisplay) " +
                "window.easysshSetDisplay($fontSize, $lineHeight);"
        )
    }

    private fun evaluate(script: String) {
        webView?.post {
            webView?.evaluateJavascript(script, null)
        }
    }

    private companion object {
        const val DEFAULT_FONT_SIZE = 11
        const val DEFAULT_LINE_HEIGHT = 1.08f
    }
}
