# EasySSH Architecture

## Overview

EasySSH is a local-first Android app. There is no backend in the MVP; data stays on the device and SSH connects directly to the target VPS.

## Layers

- `ui`: Jetpack Compose screens, dialogs, and `MainViewModel`.
- `domain`: profile models, IP mode, and validation.
- `data`: Room entity, DAO, database, and repository.
- `core.crypto`: Android Keystore AES-GCM encryption and encrypted private-key storage.
- `ssh`: SSHJ connection manager, host fingerprinting, PTY shell, and terminal session handle.
- `terminal`: restricted WebView bridge around local `xterm.js` assets.

## Data

Room stores only profile metadata:

- alias
- SSH user
- host or rotating-IP mode
- port
- encrypted private-key file name
- display name of the imported key
- trusted host fingerprint

The private key itself is encrypted and stored as an app-private file under `files/private_keys`.

## SSH Flow

1. UI asks for host if the profile uses rotating IP.
2. ViewModel decrypts the selected key into memory.
3. SSHJ receives the key through a temporary file because its key loader expects a file path.
4. The app replaces Android's old `BC` provider with the bundled modern BouncyCastle provider before SSHJ starts.
5. Host key verifier compares the current fingerprint with the stored one.
6. On first connection, the UI prompts for Trust On First Use confirmation.
7. A PTY shell opens and streams bytes to `xterm.js`.
8. Terminal input flows back through the WebView JavaScript bridge to SSHJ.

## Non-MVP Items

- Password login.
- PEM passphrase support.
- PuTTY `.ppk` conversion.
- Multiple simultaneous terminal tabs.
- Saved command buttons and Ctrl/Shift shortcut bar.
- Release signing and Play Store packaging.
