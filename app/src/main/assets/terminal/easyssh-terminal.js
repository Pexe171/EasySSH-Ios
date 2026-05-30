(function () {
    const terminalElement = document.getElementById('terminal');
    let fontSize = 11;
    let lineHeight = 1.08;

    const terminal = new Terminal({
        allowProposedApi: false,
        convertEol: true,
        cursorBlink: true,
        fontFamily: 'monospace',
        fontSize: fontSize,
        lineHeight: lineHeight,
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

    function configureInputElement() {
        const textarea = terminal.textarea;
        if (!textarea) {
            return;
        }
        textarea.setAttribute('autocomplete', 'off');
        textarea.setAttribute('autocorrect', 'off');
        textarea.setAttribute('autocapitalize', 'off');
        textarea.setAttribute('spellcheck', 'false');
    }

    configureInputElement();

    terminal.onData(function (data) {
        if (window.EasySSH && window.EasySSH.onInput) {
            window.EasySSH.onInput(data);
        }
    });

    function measureCell() {
        const probe = document.createElement('span');
        probe.textContent = 'W';
        probe.style.fontFamily = 'monospace';
        probe.style.fontSize = `${fontSize}px`;
        probe.style.lineHeight = String(lineHeight);
        probe.style.position = 'absolute';
        probe.style.visibility = 'hidden';
        terminalElement.appendChild(probe);
        const bounds = probe.getBoundingClientRect();
        terminalElement.removeChild(probe);
        return {
            width: Math.max(bounds.width, 6),
            height: Math.max(bounds.height, fontSize * lineHeight)
        };
    }

    function fitTerminal() {
        const cell = measureCell();
        const width = Math.max(terminalElement.clientWidth - 16, 80);
        const height = Math.max(terminalElement.clientHeight - 16, 80);
        const columns = Math.max(Math.floor(width / cell.width), 20);
        const rows = Math.max(Math.floor(height / cell.height), 8);
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

    window.easysshSetDisplay = function (nextFontSize, nextLineHeight) {
        fontSize = Math.max(9, Math.min(18, Number(nextFontSize) || 11));
        lineHeight = Math.max(1, Math.min(1.5, Number(nextLineHeight) || 1.08));
        terminal.options.fontSize = fontSize;
        terminal.options.lineHeight = lineHeight;
        setTimeout(fitTerminal, 30);
    };

    window.easysshRefresh = function () {
        configureInputElement();
        requestAnimationFrame(function () {
            fitTerminal();
            terminal.refresh(0, Math.max(terminal.rows - 1, 0));
            terminal.focus();
        });
    };

    document.addEventListener('visibilitychange', function () {
        if (!document.hidden) {
            window.easysshRefresh();
        }
    });

    window.addEventListener('focus', window.easysshRefresh);
    window.addEventListener('resize', fitTerminal);
    setTimeout(fitTerminal, 50);
    terminal.writeln('EasySSH');
})();
