package com.easyssh.terminal

interface TerminalInputSink {
    fun onTerminalInput(data: String)
    fun onTerminalResize(columns: Int, rows: Int)
}

