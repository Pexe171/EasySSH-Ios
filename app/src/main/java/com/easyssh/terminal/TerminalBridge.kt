package com.easyssh.terminal

import android.webkit.JavascriptInterface

class TerminalBridge(
    private val sink: TerminalInputSink
) {
    @JavascriptInterface
    fun onInput(data: String) {
        sink.onTerminalInput(data)
    }

    @JavascriptInterface
    fun onResize(columns: Int, rows: Int) {
        sink.onTerminalResize(columns, rows)
    }
}

