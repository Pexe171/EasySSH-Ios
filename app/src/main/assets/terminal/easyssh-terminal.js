(function () {
    const terminalElement = document.getElementById('terminal');
    const terminal = new Terminal({
        allowProposedApi: false,
        convertEol: true,
        cursorBlink: true,
        fontFamily: 'monospace',
        fontSize: 14,
        scrollback: 6000,
        theme: {
            background: '#050505',
            foreground: '#F8FAFC',
            cursor: '#22C55E',
            selectionBackground: '#334155'
        }
    });

    terminal.open(terminalElement);
    terminal.focus();

    terminal.onData(function (data) {
        if (window.EasySSH && window.EasySSH.onInput) {
            window.EasySSH.onInput(data);
        }
    });

    function fitTerminal() {
        const width = Math.max(terminalElement.clientWidth - 20, 80);
        const height = Math.max(terminalElement.clientHeight - 20, 80);
        const columns = Math.max(Math.floor(width / 8.4), 20);
        const rows = Math.max(Math.floor(height / 17), 8);
        terminal.resize(columns, rows);
        if (window.EasySSH && window.EasySSH.onResize) {
            window.EasySSH.onResize(columns, rows);
        }
    }

    window.easysshWrite = function (base64) {
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let index = 0; index < binary.length; index += 1) {
            bytes[index] = binary.charCodeAt(index);
        }
        terminal.write(bytes);
    };

    window.easysshClear = function () {
        terminal.clear();
    };

    window.addEventListener('resize', fitTerminal);
    setTimeout(fitTerminal, 50);
    terminal.writeln('EasySSH');
})();

